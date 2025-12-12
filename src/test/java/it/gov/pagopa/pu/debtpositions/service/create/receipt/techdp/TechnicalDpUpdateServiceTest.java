package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.UnknownDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

@ExtendWith(MockitoExtension.class)
class TechnicalDpUpdateServiceTest {

  @Mock
  private ReceiptWithAdditionalInfoMapper receiptMapperMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private UnknownDebtPositionTypeOrgRetrieverService unknownRetrieverServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository typeOrgRepositoryMock;

  private TechnicalDpUpdateService service;

  @BeforeEach
  void init() {
    service = new TechnicalDpUpdateService(
      receiptMapperMock,
      debtPositionServiceMock,
      unknownRetrieverServiceMock,
      typeOrgRepositoryMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      receiptMapperMock,
      debtPositionServiceMock
    );
  }

  @Test
  void whenUpdateDpThenResolveTypeOrgMapAndSave() {
    // Given
    DebtPosition dp = new DebtPosition();
    dp.setPaymentOptions(new TreeSet<>());

    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setDebtPositionTypeOrgCode("CODE");
    Organization organization = new Organization();
    organization.setOrganizationId(1L);

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setDebtPositionTypeOrgId(100L);

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    expectedResult.setPaymentOptions(new ArrayList<>());

    Mockito.when(typeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "CODE"))
      .thenReturn(Optional.of(typeOrg));

    Mockito.when(receiptMapperMock.mapToDebtPosition(Mockito.same(receiptDTO), Mockito.same(organization), Mockito.eq(100L)))
      .thenReturn(expectedResult);

    // When
    DebtPositionDTO result = service.updateDp(dp, receiptDTO, organization);

    // Then
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(expectedResult);
  }

  @Test
  void givenPreserveTypeOrgTrueWhenUpdateDpThenUseExistingId() {
    // Given
    DebtPosition dp = new DebtPosition();
    dp.setDebtPositionTypeOrgId(55L);
    dp.setPaymentOptions(new TreeSet<>());

    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    Organization organization = new Organization();

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    expectedResult.setPaymentOptions(new ArrayList<>());

    Mockito.when(receiptMapperMock.mapToDebtPosition(Mockito.same(receiptDTO), Mockito.same(organization), Mockito.eq(55L)))
      .thenReturn(expectedResult);

    // When
    DebtPositionDTO result = service.updateDp(dp, receiptDTO, organization, true, null);

    // Then
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(expectedResult);
    Mockito.verify(typeOrgRepositoryMock, Mockito.never()).findByOrganizationIdAndCode(Mockito.any(), Mockito.any());
  }

  @Test
  void givenUnknownTypeWhenUpdateDpThenUseItAsFallback() {
    // Given
    DebtPosition dp = new DebtPosition();
    dp.setPaymentOptions(new TreeSet<>());

    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setDebtPositionTypeOrgCode("CODE");
    Organization organization = new Organization();
    organization.setOrganizationId(1L);

    DebtPositionTypeOrg unknownTypeOrg = new DebtPositionTypeOrg();
    unknownTypeOrg.setDebtPositionTypeOrgId(99L);

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    expectedResult.setPaymentOptions(new ArrayList<>());

    Mockito.when(typeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "CODE"))
      .thenReturn(Optional.empty());

    Mockito.when(receiptMapperMock.mapToDebtPosition(Mockito.same(receiptDTO), Mockito.same(organization), Mockito.eq(99L)))
      .thenReturn(expectedResult);

    // When
    DebtPositionDTO result = service.updateDp(dp, receiptDTO, organization, false, unknownTypeOrg);

    // Then
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(expectedResult);
    Mockito.verify(unknownRetrieverServiceMock, Mockito.never()).getUnknownDebtPositionTypeOrg(Mockito.any());
  }

  @Test
  void whenGetDebtPositionDTOThenResolveAndMap() {
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setDebtPositionTypeOrgCode("CODE");
    Organization organization = new Organization();
    organization.setOrganizationId(1L);

    DebtPositionTypeOrg unknownTypeOrg = new DebtPositionTypeOrg();
    unknownTypeOrg.setDebtPositionTypeOrgId(77L);

    DebtPositionDTO expectedResult = new DebtPositionDTO();

    Mockito.when(typeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "CODE"))
      .thenReturn(Optional.empty());
    Mockito.when(unknownRetrieverServiceMock.getUnknownDebtPositionTypeOrg(1L))
      .thenReturn(unknownTypeOrg);

    Mockito.when(receiptMapperMock.mapToDebtPosition(Mockito.same(receiptDTO), Mockito.same(organization), Mockito.eq(77L)))
      .thenReturn(expectedResult);

    // When
    DebtPositionDTO result = service.getDebtPositionDTO(receiptDTO, organization);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenDtoAlreadyHasIdsWhenUpdateDebtPositionThenDoesNotOverwrite() {
    // Given
    Long dpId = 1L;
    Long existingDpIdInDto = 111L;
    Long poIdEntity = 2L;
    Long poIdDto = 222L;
    String iud = "IUD-X";
    Long instIdEntity = 3L;
    Long instIdDto = 333L;
    Integer trIndex = 7;
    Long trIdEntity = 4L;
    Long trIdDto = 444L;

    Transfer trEntity = new Transfer();
    trEntity.setTransferIndex(trIndex);
    trEntity.setTransferId(trIdEntity);

    InstallmentNoPII instEntity = new InstallmentNoPII();
    instEntity.setIud(iud);
    instEntity.setInstallmentId(instIdEntity);
    instEntity.setTransfers(new TreeSet<>(List.of(trEntity)));

    PaymentOption poEntity = new PaymentOption();
    poEntity.setPaymentOptionIndex(5);
    poEntity.setPaymentOptionId(poIdEntity);
    poEntity.setInstallments(new TreeSet<>(List.of(instEntity)));

    DebtPosition entity = new DebtPosition();
    entity.setDebtPositionId(dpId);
    entity.setPaymentOptions(new TreeSet<>(List.of(poEntity)));

    TransferDTO trDTO = new TransferDTO();
    trDTO.setTransferIndex(trIndex);
    trDTO.setTransferId(trIdDto);

    InstallmentDTO instDTO = new InstallmentDTO();
    instDTO.setIud(iud);
    instDTO.setInstallmentId(instIdDto);
    instDTO.setTransfers(List.of(trDTO));

    PaymentOptionDTO poDTO = new PaymentOptionDTO();
    poDTO.setPaymentOptionIndex(5);
    poDTO.setPaymentOptionId(poIdDto);
    poDTO.setInstallments(List.of(instDTO));

    DebtPositionDTO dpDTO = new DebtPositionDTO();
    dpDTO.setDebtPositionId(existingDpIdInDto);
    dpDTO.setPaymentOptions(List.of(poDTO));

    // When
    service.updateDp(entity, dpDTO);

    // Then
    ArgumentCaptor<DebtPositionDTO> captor = ArgumentCaptor.forClass(DebtPositionDTO.class);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(captor.capture());
    DebtPositionDTO saved = captor.getValue();

    Assertions.assertEquals(dpId, saved.getDebtPositionId());
    Assertions.assertEquals(poIdEntity, saved.getPaymentOptions().getFirst().getPaymentOptionId());
    Assertions.assertEquals(instIdEntity, saved.getPaymentOptions().getFirst().getInstallments().getFirst().getInstallmentId());
    Assertions.assertEquals(trIdEntity, saved.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().getTransferId());
  }
}
