package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.PaymentFlowOrchestratorService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.StandardPaymentUpdateService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptBasedTechnicalDpHandlerServiceTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final String accessToken = "ACCESSTOKEN";

  @Mock
  private ReceiptWithAdditionalInfoMapper receiptMapperMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private PaymentsProducerService paymentsProducerServiceMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;
  @Mock
  private TechnicalDpUpdateService dpUpdateServiceMock;
  @Mock
  private StandardPaymentUpdateService standardPaymentUpdateServiceMock;
  @Mock
  private PaymentFlowOrchestratorService paymentFlowOrchestratorServiceMock;
  @Mock
  private ReceiptService receiptServiceMock;

  private ReceiptBasedTechnicalDpHandlerService service;

  @BeforeEach
  void init(){
    service = new ReceiptBasedTechnicalDpHandlerService(
      receiptMapperMock,
      debtPositionServiceMock,
      paymentsProducerServiceMock,
      debtPositionMapperMock,
      dpUpdateServiceMock,
      standardPaymentUpdateServiceMock,
      paymentFlowOrchestratorServiceMock,
      receiptServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      receiptMapperMock,
      debtPositionServiceMock,
      paymentsProducerServiceMock,
      debtPositionMapperMock,
      dpUpdateServiceMock
    );
  }

  @Test
  void whenCreateAndPublishTechDpThenOk(){
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    Organization organization = new Organization();
    DebtPositionDTO dpDto = new DebtPositionDTO();
    DebtPosition expectedResult = new DebtPosition();

    ReceiptBasedTechnicalDpHandlerService spyService = Mockito.spy(service);

    Mockito.when(receiptMapperMock.mapToDebtPosition(Mockito.same(receiptDTO), Mockito.same(organization)))
      .thenReturn(dpDto);

    Mockito.doReturn(expectedResult)
      .when(spyService)
      .publishTechDp(Mockito.same(dpDto), Mockito.same(receiptDTO));

    // When
    DebtPosition result = spyService.createAndPublishTechDp(organization, receiptDTO);

    // Then
    Assertions.assertSame(expectedResult, result);

    Mockito.verify(debtPositionServiceMock)
      .saveDebtPosition(Mockito.same(dpDto));
  }

  @Test
  void whenPublishTechDpThenOk(){
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setReceiptId(-1L);
    DebtPositionDTO dpDto = new DebtPositionDTO();
    DebtPosition expectedResult = new DebtPosition();

    Mockito.when(debtPositionMapperMock.mapToModel(Mockito.same(dpDto)))
      .thenReturn(expectedResult);

    // When
    DebtPosition result = service.publishTechDp(dpDto, receiptDTO);

    // Then
    Assertions.assertSame(expectedResult, result);

    Mockito.verify(paymentsProducerServiceMock)
      .notifyPaymentsEvent(Mockito.same(dpDto), Mockito.eq(PaymentEventType.RT_RECEIVED), Mockito.eq("receiptId:-1"));
  }

  @Test
  void whenUpdateAndPublishTechDpThenDelegateToOrchestrator(){
    // Given
    Organization organization = new Organization();
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);

    // When
    DebtPosition result = service.updateAndPublishTechDp(organization, dp, installment, receiptDTO, accessToken);

    // Then
    Assertions.assertSame(dp, result);

    Mockito.verify(paymentFlowOrchestratorServiceMock)
      .handleAlreadyPaidLogic(
        Mockito.eq(installment),
        Mockito.eq(receiptDTO),
        Mockito.eq(dp),
        Mockito.any(Runnable.class),
        Mockito.any(Runnable.class),
        Mockito.any(Runnable.class)
      );
  }

  @Test
  void whenOrchestratorExecutesFullUpdateThenPerformStandardUpdate() {
    // Given
    Organization organization = new Organization();
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);

    ArgumentCaptor<Runnable> fullUpdateCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    service.updateAndPublishTechDp(organization, dp, installment, receiptDTO, accessToken);

    Mockito.verify(paymentFlowOrchestratorServiceMock)
      .handleAlreadyPaidLogic(Mockito.any(), Mockito.any(), Mockito.any(), fullUpdateCaptor.capture(), Mockito.any(), Mockito.any());

    fullUpdateCaptor.getValue().run();

    // Then
    Mockito.verify(standardPaymentUpdateServiceMock).performStandardUpdate(dp, installment, receiptDTO, accessToken);
  }

  @Test
  void whenOrchestratorExecutesPartialUpdateAndLeftBranchThenUpdateBalanceAndMeta() {
    // Given
    Organization organization = new Organization();
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    ArgumentCaptor<Runnable> partialUpdateCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    service.updateAndPublishTechDp(organization, dp, installment, receiptDTO, accessToken);

    Mockito.verify(paymentFlowOrchestratorServiceMock)
      .handleAlreadyPaidLogic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), partialUpdateCaptor.capture(), Mockito.any());

    partialUpdateCaptor.getValue().run();

    // Then
    Mockito.verify(paymentFlowOrchestratorServiceMock)
      .updateBalanceAndMeta(dp, installment, receiptDTO, accessToken);
  }

  @Test
  void whenOrchestratorExecutesPartialUpdateAndRightBranchThenUpdateDp() {
    // Given
    Organization organization = new Organization();
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    ArgumentCaptor<Runnable> partialUpdateCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    service.updateAndPublishTechDp(organization, dp, installment, receiptDTO, accessToken);

    Mockito.verify(paymentFlowOrchestratorServiceMock)
      .handleAlreadyPaidLogic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), partialUpdateCaptor.capture(), Mockito.any());

    partialUpdateCaptor.getValue().run();

    // Then
    Mockito.verify(dpUpdateServiceMock)
      .updateDp(dp, receiptDTO, organization);
  }

  @Test
  void whenOrchestratorExecutesSyncActionThenPublishTechDp() {
    // Given
    Organization organization = new Organization();
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    DebtPositionDTO dpDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPosition mappedModel = podamFactory.manufacturePojo(DebtPosition.class);

    Mockito.when(debtPositionMapperMock.mapToDto(dp)).thenReturn(dpDTO);
    Mockito.when(debtPositionMapperMock.mapToModel(dpDTO)).thenReturn(mappedModel);

    ArgumentCaptor<Runnable> syncCaptor = ArgumentCaptor.forClass(Runnable.class);

    // When
    service.updateAndPublishTechDp(organization, dp, installment, receiptDTO, accessToken);

    Mockito.verify(paymentFlowOrchestratorServiceMock)
      .handleAlreadyPaidLogic(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), syncCaptor.capture());

    syncCaptor.getValue().run();

    // Then
    Mockito.verify(paymentsProducerServiceMock)
      .notifyPaymentsEvent(Mockito.same(dpDTO), Mockito.eq(PaymentEventType.RT_RECEIVED), Mockito.eq("receiptId:" + receiptDTO.getReceiptId()));
  }

  @Test
  void whenUpdateAndPublishTechDpWithThreeArgsThenOk() {
    // Given
    Organization organization = new Organization();
    DebtPosition dp = new DebtPosition();
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    DebtPositionDTO dpDto = new DebtPositionDTO();

    ReceiptBasedTechnicalDpHandlerService spyService = Mockito.spy(service);

    Mockito.when(dpUpdateServiceMock.updateDp(Mockito.same(dp), Mockito.same(receiptDTO), Mockito.same(organization)))
      .thenReturn(dpDto);

    Mockito.doReturn(dp)
      .when(spyService)
      .publishTechDp(Mockito.same(dpDto), Mockito.same(receiptDTO));

    // When
    spyService.updateAndPublishTechDp(organization, dp, receiptDTO);

    // Then
    Mockito.verify(dpUpdateServiceMock).updateDp(Mockito.same(dp), Mockito.same(receiptDTO), Mockito.same(organization));
    Mockito.verify(spyService).publishTechDp(Mockito.same(dpDto), Mockito.same(receiptDTO));
  }
}
