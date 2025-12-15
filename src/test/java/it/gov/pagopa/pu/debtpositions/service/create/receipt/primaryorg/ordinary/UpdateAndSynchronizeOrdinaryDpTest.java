package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.utils.PaymentFlowOrchestratorService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class UpdateAndSynchronizeOrdinaryDpTest {

  @Mock
  private ReceiptService receiptServiceMock;
  @Mock
  private PaymentFlowOrchestratorService paymentFlowOrchestratorServiceMock;

  @InjectMocks
  private UpdateAndSynchronizeOrdinaryDp service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final String accessToken = "ACCESSTOKEN";

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      receiptServiceMock,
      paymentFlowOrchestratorServiceMock
    );
  }

  @Test
  void givenStoredPagoPaAndIncomingPagoPaWhenHandleOrdinaryDpAlreadyPaidThenSkipEverything() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleOrdinaryDpAlreadyPaid(installment, incomingReceipt, dp, syncWorkflowAction, accessToken);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(syncWorkflowAction, Mockito.never()).run();
    Mockito.verify(paymentFlowOrchestratorServiceMock, Mockito.never()).updateBalanceAndMeta(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
    Mockito.verify(paymentFlowOrchestratorServiceMock, Mockito.never()).performStandardUpdate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenStoredPagoPaAndIncomingFileWhenHandleOrdinaryDpAlreadyPaidThenUpdateBalanceAndSync() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleOrdinaryDpAlreadyPaid(installment, incomingReceipt, dp, syncWorkflowAction, accessToken);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(paymentFlowOrchestratorServiceMock).updateBalanceAndMeta(dp, installment, incomingReceipt, accessToken);
    Mockito.verify(syncWorkflowAction).run();
    Mockito.verify(paymentFlowOrchestratorServiceMock, Mockito.never()).performStandardUpdate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenStoredFileAndIncomingPagoPaWhenHandleOrdinaryDpAlreadyPaidThenFullUpdateAndSync() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleOrdinaryDpAlreadyPaid(installment, incomingReceipt, dp, syncWorkflowAction, accessToken);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(paymentFlowOrchestratorServiceMock).performStandardUpdate(dp, installment, incomingReceipt, accessToken);
    Mockito.verify(syncWorkflowAction).run();
    Mockito.verify(paymentFlowOrchestratorServiceMock, Mockito.never()).updateBalanceAndMeta(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenStoredFileAndIncomingFileWhenHandleOrdinaryDpAlreadyPaidThenUpdateBalanceAndSync() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleOrdinaryDpAlreadyPaid(installment, incomingReceipt, dp, syncWorkflowAction, accessToken);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(paymentFlowOrchestratorServiceMock).updateBalanceAndMeta(dp, installment, incomingReceipt, accessToken);
    Mockito.verify(syncWorkflowAction).run();
    Mockito.verify(paymentFlowOrchestratorServiceMock, Mockito.never()).performStandardUpdate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
  }
}
