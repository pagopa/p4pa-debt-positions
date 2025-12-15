package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.utils.PaymentFlowOrchestratorService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class OrdinaryDPPaymentHandlerServiceTest {

  @Mock
  private DebtPositionMapper mapperMock;
  @Mock
  private DebtPositionSyncService syncServiceMock;
  @Mock
  private UpdateAndSynchronizeOrdinaryDp updateAndSynchronizeOrdinaryDpMock;
  @Mock
  private PaymentFlowOrchestratorService paymentFlowOrchestratorServiceMock;

  private OrdinaryDPPaymentHandlerService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init(){
    service = new OrdinaryDPPaymentHandlerService(
      mapperMock,
      syncServiceMock,
      updateAndSynchronizeOrdinaryDpMock,
      paymentFlowOrchestratorServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      mapperMock,
      syncServiceMock,
      updateAndSynchronizeOrdinaryDpMock,
      paymentFlowOrchestratorServiceMock
    );
  }

  @Test
  void givenUnpaidInstallmentWhenHandlePaymentThenPerformStandardUpdateAndSync() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.UNPAID);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    DebtPositionDTO dpDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);

    Mockito.when(mapperMock.mapToDto(dp)).thenReturn(dpDTO);

    // When
    service.handlePayment(dp, installment, receiptDTO, accessToken);

    // Then
    Mockito.verify(paymentFlowOrchestratorServiceMock)
      .performStandardUpdate(dp, installment, receiptDTO, accessToken);

    Mockito.verify(mapperMock).mapToDto(dp);

    Mockito.verify(syncServiceMock)
      .syncDebtPosition(
        Mockito.eq(dpDTO),
        Mockito.any(WfExecutionParameters.class),
        Mockito.eq(PaymentEventType.RT_RECEIVED),
        Mockito.eq("receiptId:" + receiptDTO.getReceiptId()),
        Mockito.eq(accessToken)
      );
  }

  @Test
  void givenPaidInstallmentWhenHandlePaymentThenDelegateToUpdateAndSynchronizeService() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.PAID);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);

    // When
    service.handlePayment(dp, installment, receiptDTO, accessToken);

    // Then
    Mockito.verify(updateAndSynchronizeOrdinaryDpMock)
      .handleOrdinaryDpAlreadyPaid(
        Mockito.eq(installment),
        Mockito.eq(receiptDTO),
        Mockito.eq(dp),
        Mockito.any(Runnable.class),
        Mockito.eq(accessToken)
      );
  }

  @Test
  void givenPaidInstallmentWhenDelegatedServiceRunsSyncActionThenInvokeWorkflow() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.PAID);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    DebtPositionDTO dpDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);

    Mockito.when(mapperMock.mapToDto(dp)).thenReturn(dpDTO);

    ArgumentCaptor<Runnable> syncCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    service.handlePayment(dp, installment, receiptDTO, accessToken);

    Mockito.verify(updateAndSynchronizeOrdinaryDpMock)
      .handleOrdinaryDpAlreadyPaid(
        Mockito.any(), Mockito.any(), Mockito.any(),
        syncCaptor.capture(),
        Mockito.anyString()
      );

    syncCaptor.getValue().run();

    Mockito.verify(mapperMock).mapToDto(dp);
    Mockito.verify(syncServiceMock)
      .syncDebtPosition(
        Mockito.eq(dpDTO),
        Mockito.any(WfExecutionParameters.class),
        Mockito.eq(PaymentEventType.RT_RECEIVED),
        Mockito.eq("receiptId:" + receiptDTO.getReceiptId()),
        Mockito.eq(accessToken)
      );
  }
}
