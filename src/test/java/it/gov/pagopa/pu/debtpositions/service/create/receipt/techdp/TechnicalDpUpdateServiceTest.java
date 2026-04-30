package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
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

import java.util.List;
import java.util.TreeSet;

@ExtendWith(MockitoExtension.class)
class TechnicalDpUpdateServiceTest {

  @Mock
  private ReceiptWithAdditionalInfoMapper receiptMapperMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;

  private TechnicalDpUpdateService service;

  @BeforeEach
  void init() {
    service = new TechnicalDpUpdateService(
      receiptMapperMock,
      debtPositionServiceMock
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
  void whenThenMapAndUpdateIt() {
    // Given
    Long dpTypeOrgId = 10L;
    DebtPosition dp = new DebtPosition();
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    Organization organization = new Organization();
    DebtPositionDTO expectedResult = new DebtPositionDTO();

    Mockito.when(receiptMapperMock.mapToDebtPosition(Mockito.same(receiptDTO), Mockito.same(organization), Mockito.same(dpTypeOrgId), Mockito.any()))
      .thenReturn(expectedResult);

    service = Mockito.spy(service);
    Mockito.doNothing()
      .when(service)
      .updateDp(Mockito.same(dp), Mockito.same(expectedResult));

    // When
    DebtPositionDTO result = service.updateDp(dp, receiptDTO, organization, dpTypeOrgId);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenExistingDebtPositionWhenUpdateDebtPositionThenPropagatesIdsAndSaves() {
    // Given
    Long dpId = 10L;
    Long poId = 20L;
    Integer poIndex = 1;
    String iud = "IUD-001";
    Long instId = 30L;
    Integer trIndex = 0;
    Long trId = 40L;

    Transfer trEntity = new Transfer();
    trEntity.setTransferIndex(trIndex);
    trEntity.setTransferId(trId);

    InstallmentNoPII instEntity = new InstallmentNoPII();
    instEntity.setIud(iud);
    instEntity.setInstallmentId(instId);
    instEntity.setTransfers(new TreeSet<>(List.of(trEntity)));

    PaymentOption poEntity = new PaymentOption();
    poEntity.setPaymentOptionId(poId);
    poEntity.setPaymentOptionIndex(poIndex);
    poEntity.setInstallments(new TreeSet<>(List.of(instEntity)));

    DebtPosition entity = new DebtPosition();
    entity.setDebtPositionId(dpId);
    entity.setPaymentOptions(new TreeSet<>(List.of(poEntity)));

    TransferDTO trDTO = new TransferDTO();
    trDTO.setTransferIndex(trIndex);

    InstallmentDTO instDTO = new InstallmentDTO();
    instDTO.setIud(iud);
    instDTO.setTransfers(List.of(trDTO));

    PaymentOptionDTO poDTO = new PaymentOptionDTO();
    poDTO.setPaymentOptionIndex(poIndex);
    poDTO.setInstallments(List.of(instDTO));

    DebtPositionDTO dpDTO = new DebtPositionDTO();
    dpDTO.setPaymentOptions(List.of(poDTO));

    // When
    service.updateDp(entity, dpDTO);

    // Then
    ArgumentCaptor<DebtPositionDTO> captor = ArgumentCaptor.forClass(DebtPositionDTO.class);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(captor.capture());
    DebtPositionDTO saved = captor.getValue();

    Assertions.assertEquals(dpId, saved.getDebtPositionId());

    PaymentOptionDTO savedPo = saved.getPaymentOptions().getFirst();
    Assertions.assertEquals(poId, savedPo.getPaymentOptionId());
    Assertions.assertEquals(poIndex, savedPo.getPaymentOptionIndex());

    InstallmentDTO savedInst = savedPo.getInstallments().getFirst();
    Assertions.assertEquals(instId, savedInst.getInstallmentId());
    Assertions.assertEquals(iud, savedInst.getIud());

    TransferDTO savedTr = savedInst.getTransfers().getFirst();
    Assertions.assertEquals(trId, savedTr.getTransferId());
    Assertions.assertEquals(trIndex, savedTr.getTransferIndex());
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
