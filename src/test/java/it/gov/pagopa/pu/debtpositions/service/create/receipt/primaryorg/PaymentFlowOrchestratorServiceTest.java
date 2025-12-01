package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.OrdinaryInstallmentPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.UnknownDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PaymentFlowOrchestratorServiceTest {

  @Mock
  private ReceiptService receiptServiceMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private OrdinaryInstallmentPaymentHandlerService ordinaryInstallmentPaymentHandlerServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverServiceMock;

  private PaymentFlowOrchestratorService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    service = new PaymentFlowOrchestratorService(
      receiptServiceMock,
      debtPositionServiceMock,
      ordinaryInstallmentPaymentHandlerServiceMock,
      debtPositionTypeOrgRepositoryMock,
      unknownDebtPositionTypeOrgRetrieverServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      receiptServiceMock,
      debtPositionServiceMock,
      ordinaryInstallmentPaymentHandlerServiceMock,
      debtPositionTypeOrgRepositoryMock,
      unknownDebtPositionTypeOrgRetrieverServiceMock
    );
  }

  @Test
  void givenStoredPagoPaAndIncomingPagoPaWhenHandleAlreadyPaidLogicThenSkipEverything() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    Runnable fullUpdateAction = Mockito.mock(Runnable.class);
    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleAlreadyPaidLogic(installment, incomingReceipt, dp, fullUpdateAction, syncWorkflowAction, accessToken);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(fullUpdateAction, Mockito.never()).run();
    Mockito.verify(syncWorkflowAction, Mockito.never()).run();
  }

  @Test
  void givenStoredPagoPaAndIncomingFileWhenHandleAlreadyPaidLogicThenUpdateBalanceAndSync() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    incomingReceipt.setDebtPositionTypeOrgCode("CODE");
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionTypeOrgId(100L);
    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    DebtPositionTypeOrg dpTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Runnable fullUpdateAction = Mockito.mock(Runnable.class);
    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(dp.getOrganizationId(), "CODE"))
      .thenReturn(Optional.of(dpTypeOrg));

    // When
    service.handleAlreadyPaidLogic(installment, incomingReceipt, dp, fullUpdateAction, syncWorkflowAction, accessToken);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(debtPositionTypeOrgRepositoryMock).findByOrganizationIdAndCode(dp.getOrganizationId(), "CODE");
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, accessToken);
    Mockito.verify(fullUpdateAction, Mockito.never()).run();
    Mockito.verify(syncWorkflowAction).run();
  }

  @Test
  void givenStoredFileAndIncomingPagoPaWhenHandleAlreadyPaidLogicThenFullUpdateAndSync() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    Runnable fullUpdateAction = Mockito.mock(Runnable.class);
    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleAlreadyPaidLogic(installment, incomingReceipt, dp, fullUpdateAction, syncWorkflowAction, accessToken);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(fullUpdateAction).run();
    Mockito.verify(syncWorkflowAction).run();
  }

  @Test
  void givenStoredFileAndIncomingFileWhenHandleAlreadyPaidLogicThenUpdateBalanceAndSync() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    incomingReceipt.setDebtPositionTypeOrgCode("CODE");
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionTypeOrgId(100L);
    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    DebtPositionTypeOrg dpTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Runnable fullUpdateAction = Mockito.mock(Runnable.class);
    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(dp.getOrganizationId(), "CODE"))
      .thenReturn(Optional.of(dpTypeOrg));

    // When
    service.handleAlreadyPaidLogic(installment, incomingReceipt, dp, fullUpdateAction, syncWorkflowAction, accessToken);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(debtPositionTypeOrgRepositoryMock).findByOrganizationIdAndCode(dp.getOrganizationId(), "CODE");
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, accessToken);
    Mockito.verify(fullUpdateAction, Mockito.never()).run();
    Mockito.verify(syncWorkflowAction).run();
  }

  @Test
  void givenDpTypeOrgIdUnsetAndRepoFoundWhenUpdateBalanceAndMetaThenSaveDp() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionTypeOrgId(-1L);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    String code = "CODE";
    DebtPositionTypeOrg dpTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(dp.getOrganizationId(), code))
      .thenReturn(Optional.of(dpTypeOrg));

    // When
    service.updateBalanceAndMeta(dp, installment, code, accessToken);

    // Then
    Assertions.assertEquals(dpTypeOrg.getDebtPositionTypeOrgId(), dp.getDebtPositionTypeOrgId());
    Mockito.verify(debtPositionTypeOrgRepositoryMock).findByOrganizationIdAndCode(dp.getOrganizationId(), code);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(dp);
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, accessToken);
  }

  @Test
  void givenDpTypeOrgIdUnsetAndRepoNotFoundWhenUpdateBalanceAndMetaThenUseUnknownAndSaveDp() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionTypeOrgId(-1L);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    String code = "CODE";
    DebtPositionTypeOrg unknownDpTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(dp.getOrganizationId(), code))
      .thenReturn(Optional.empty());
    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(dp.getOrganizationId()))
      .thenReturn(unknownDpTypeOrg);

    // When
    service.updateBalanceAndMeta(dp, installment, code, accessToken);

    // Then
    Assertions.assertEquals(unknownDpTypeOrg.getDebtPositionTypeOrgId(), dp.getDebtPositionTypeOrgId());
    Mockito.verify(debtPositionTypeOrgRepositoryMock).findByOrganizationIdAndCode(dp.getOrganizationId(), code);
    Mockito.verify(unknownDebtPositionTypeOrgRetrieverServiceMock).getUnknownDebtPositionTypeOrg(dp.getOrganizationId());
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(dp);
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, accessToken);
  }

  @Test
  void givenDpTypeOrgIdSetWhenUpdateBalanceAndMetaThenSkipSaveDp() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionTypeOrgId(123L);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    String code = "CODE";
    DebtPositionTypeOrg dpTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(dp.getOrganizationId(), code))
      .thenReturn(Optional.of(dpTypeOrg));

    // When
    service.updateBalanceAndMeta(dp, installment, code, accessToken);

    // Then
    Assertions.assertEquals(123L, dp.getDebtPositionTypeOrgId());
    Mockito.verify(debtPositionTypeOrgRepositoryMock).findByOrganizationIdAndCode(dp.getOrganizationId(), code);
    Mockito.verify(debtPositionServiceMock, Mockito.never()).saveDebtPosition(dp);
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, accessToken);
  }
}
