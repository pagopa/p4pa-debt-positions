package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper.InstallmentSynchronizeMapper;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  private InstallmentSynchronizeApplierService installmentSynchronizeApplierService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeApplierService = new InstallmentSynchronizeApplierService(installmentSynchronizeMapperMock,
      applierDebtPositionServiceMock, applierInstallmentServiceMock, applierPaymentOptionServiceMock, debtPositionTypeOrgRepositoryMock);
  }

  @Test
  void testApplyWhenDPNullThenOk(){
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    DebtPositionDTO syncDebtPositionDTO = buildSyncDebtPositionDTO();
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(installmentSynchronizeMapperMock.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId()))
      .thenReturn(syncDebtPositionDTO);

    InstallmentDTO result = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, null, null, null).getRight();

    assertEquals(result, syncDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    verify(applierDebtPositionServiceMock, times(0)).merge(Mockito.any(), Mockito.any(), Mockito.any());
    verify(applierPaymentOptionServiceMock, times(0)).merge(Mockito.any(), Mockito.any());
    verify(applierInstallmentServiceMock, times(0)).merge(Mockito.any(), Mockito.any());
  }

  @Test
  void testApplyWhenPONullThenOk(){
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    DebtPositionDTO syncDebtPositionDTO = buildSyncDebtPositionDTO();
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(applierDebtPositionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
    Mockito.when(installmentSynchronizeMapperMock.map2PaymentOptionDTO(installmentSynchronizeDTO))
      .thenReturn(syncDebtPositionDTO.getPaymentOptions().getFirst());

    InstallmentDTO result = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO, null, null).getRight();

    assertEquals(result, syncDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    verify(applierPaymentOptionServiceMock, times(0)).merge(Mockito.any(), Mockito.any());
    verify(applierInstallmentServiceMock, times(0)).merge(Mockito.any(), Mockito.any());
  }

  @Test
  void testApplyWhenInstallmentNullThenOk(){
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

    InstallmentDTO result = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO, debtPositionDTO.getPaymentOptions().getFirst(), null).getRight();

    assertEquals(result, syncDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    verify(applierInstallmentServiceMock, times(0)).merge(Mockito.any(), Mockito.any());
  }

  @Test
  void testApplyThenOk(){
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(applierDebtPositionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
    Mockito.doNothing().when(applierPaymentOptionServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst());
    Mockito.doNothing().when(applierInstallmentServiceMock).merge(installmentSynchronizeDTO, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    InstallmentDTO result = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO,
      debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()).getRight();

    assertEquals(result, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    Mockito.verifyNoInteractions(installmentSynchronizeMapperMock);
  }

  @Test
  void testApplyDebtPositionTypeOrgNotFoundThenException(){
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().getFirst();
    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();

    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode()))
      .thenReturn(Optional.empty());

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () ->
      installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, debtPositionDTO,
        paymentOptionDTO, installmentDTO));
    assertEquals(String.format("The debt position type code %s is not valid for this organizationId %s", installmentSynchronizeDTO.getDebtPositionTypeCode(), installmentSynchronizeDTO.getOrganizationId()),
      invalidValueException.getMessage());
  }

}
