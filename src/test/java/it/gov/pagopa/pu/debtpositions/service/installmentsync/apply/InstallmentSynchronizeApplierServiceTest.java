package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.CategoryResolverService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper.InstallmentSynchronizeMapper;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildSyncDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeApplierServiceTest {

  @Mock
  private InstallmentSynchronizeMapper installmentSynchronizeMapperMock;
  @Mock
  private InstallmentSynchronizeDebtPositionApplierService applierDebtPositionServiceMock;
  @Mock
  private InstallmentSynchronizeInstallmentApplierService applierInstallmentServiceMock;
  @Mock
  private InstallmentSynchronizePaymentOptionApplierService applierPaymentOptionServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private CategoryResolverService categoryResolverServiceMock;

  private InstallmentSynchronizeApplierService installmentSynchronizeApplierService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeApplierService = new InstallmentSynchronizeApplierService(installmentSynchronizeMapperMock,
      applierDebtPositionServiceMock, applierInstallmentServiceMock, applierPaymentOptionServiceMock, debtPositionTypeOrgRepositoryMock,
      organizationServiceMock, categoryResolverServiceMock);
  }

  @Test
  void testApplyWhenPONullThenOk() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    DebtPositionDTO syncDebtPositionDTO = buildSyncDebtPositionDTO();
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(applierDebtPositionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
    Mockito.when(installmentSynchronizeMapperMock.map2PaymentOptionDTO(installmentSynchronizeDTO))
      .thenReturn(syncDebtPositionDTO.getPaymentOptions().getFirst());

    InstallmentDTO result = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO, null, null, accessToken).getRight();

    assertEquals(result, syncDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    verify(applierPaymentOptionServiceMock, times(0)).merge(any(), any());
  }

  @Test
  void testApplyWhenInstallmentNullThenOk() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    DebtPositionDTO syncDebtPositionDTO = buildSyncDebtPositionDTO();
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(applierDebtPositionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
    Mockito.doNothing().when(applierPaymentOptionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst());
    Mockito.when(installmentSynchronizeMapperMock.map2Installment(installmentSynchronizeDTO))
      .thenReturn(syncDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    InstallmentDTO result = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO, debtPositionDTO.getPaymentOptions().getFirst(), null, accessToken).getRight();

    assertEquals(result, syncDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    verify(applierInstallmentServiceMock, times(0)).merge(any(), any());
  }

  @Test
  void testApplyThenOk() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    Mockito.when(organizationServiceMock.getOrganizationById(installmentSynchronizeDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(categoryResolverServiceMock.resolveCategory(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getLegacyPaymentMetadata(),
      debtPositionTypeOrg.getDebtPositionTypeId(), organization.getOrgTypeCode())).thenReturn("category");
    Mockito.doNothing().when(applierDebtPositionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
    Mockito.doNothing().when(applierPaymentOptionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst());
    Mockito.doNothing().when(applierInstallmentServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    InstallmentDTO result = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO,
      debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken).getRight();

    assertEquals(result, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    Mockito.verifyNoInteractions(installmentSynchronizeMapperMock);
  }

  @Test
  void testApplyWhenFirstTransferPopulatedThenOk() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.addAdditionalTransfersItem(TransferSynchronizeDTO.builder().transferIndex(1).build());

    Mockito.when(organizationServiceMock.getOrganizationById(installmentSynchronizeDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(applierDebtPositionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
    Mockito.doNothing().when(applierPaymentOptionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst());
    Mockito.doNothing().when(applierInstallmentServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    InstallmentDTO result = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO,
      debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken).getRight();

    assertEquals(result, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    Mockito.verifyNoInteractions(installmentSynchronizeMapperMock);
    Mockito.verifyNoInteractions(categoryResolverServiceMock);
  }

  @Test
  void testApplyWithLegacyMetadataNotNullAndValidThenOk() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setCategory("9/abc123/");

    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setLegacyPaymentMetadata("9/abc123/xx");

    Mockito.when(organizationServiceMock.getOrganizationById(installmentSynchronizeDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(applierDebtPositionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
    Mockito.doNothing().when(applierPaymentOptionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst());
    Mockito.doNothing().when(applierInstallmentServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    InstallmentDTO result = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO,
      debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken).getRight();

    assertEquals(result, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    Mockito.verifyNoInteractions(installmentSynchronizeMapperMock);
  }

  @Test
  void testApplyOrgNotFoundThenException() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().getFirst();
    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();

    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(applierDebtPositionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
    Mockito.doNothing().when(applierPaymentOptionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst());

    Mockito.when(organizationServiceMock.getOrganizationById(installmentSynchronizeDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.empty());

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () ->
      installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO,
        paymentOptionDTO, installmentDTO, accessToken));
    assertEquals("INVALID_ORGANIZATION", invalidValueException.getCode());
    assertEquals(String.format("Provided organization with id %s not found", installmentSynchronizeDTO.getOrganizationId()), invalidValueException.getMessage());
  }

  @Test
  void testApplyDebtPositionTypeOrgNotFoundThenException() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().getFirst();
    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();

    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.empty());

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () ->
      installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO,
        paymentOptionDTO, installmentDTO, accessToken));
    assertEquals("INVALID_DEBT_POSITION_TYPE_ORG_CODE", invalidValueException.getCode());
    assertEquals(String.format("The debt position type org with code %s is not valid for this organizationId %s", installmentSynchronizeDTO.getDebtPositionTypeCode(), installmentSynchronizeDTO.getOrganizationId()),
      invalidValueException.getMessage());
  }

  @Test
  void givenStoredDebtPositionNullWhenApplyThenOk() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO newDebtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));

      Mockito.when(installmentSynchronizeMapperMock.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg))
      .thenReturn(newDebtPositionDTO);

    Pair<DebtPositionDTO, InstallmentDTO> result = installmentSynchronizeApplierService.apply(
      installmentSynchronizeDTO,
      null,
      null,
      null,
      accessToken
    );

    assertEquals(newDebtPositionDTO, result.getLeft());
    assertEquals(newDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), result.getRight());
    Mockito.verify(installmentSynchronizeMapperMock, times(1))
      .map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg);
    Mockito.verifyNoInteractions(applierDebtPositionServiceMock, applierPaymentOptionServiceMock, applierInstallmentServiceMock);

  }
}
