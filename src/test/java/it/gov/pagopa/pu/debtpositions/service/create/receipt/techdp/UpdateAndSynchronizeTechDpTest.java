package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.utils.PaymentFlowOrchestratorService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class UpdateAndSynchronizeTechDpTest {

  @Mock
  private ReceiptService receiptServiceMock;
  @Mock
  private PaymentFlowOrchestratorService paymentFlowOrchestratorServiceMock;
  @Mock
  private TechnicalDpUpdateService technicalDpUpdateServiceMock;
  @Mock
  private PaymentsProducerService paymentsProducerServiceMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  @InjectMocks
  private UpdateAndSynchronizeTechDp service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final String accessToken = "ACCESSTOKEN";


  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      receiptServiceMock,
      paymentFlowOrchestratorServiceMock,
      technicalDpUpdateServiceMock,
      paymentsProducerServiceMock,
      debtPositionMapperMock
    );
  }

  @Test
  void givenStoredPagoPaAndIncomingFileWhenHandleTechDpAlreadyPaidThenUpdateBalanceAndPublish() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    DebtPosition storedDp = podamFactory.manufacturePojo(DebtPosition.class);
    Organization organization = new Organization();

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    DebtPositionDTO mappedDto = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPosition expectedResult = new DebtPosition();

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);
    Mockito.when(debtPositionMapperMock.mapToDto(storedDp)).thenReturn(mappedDto);
    Mockito.when(debtPositionMapperMock.mapToModel(mappedDto)).thenReturn(expectedResult);

    // When
    DebtPosition result = service.handleTechDpAlreadyPaid(installment, incomingReceipt, storedDp, organization, accessToken);

    // Then
    Assertions.assertSame(storedDp, result);

    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(paymentFlowOrchestratorServiceMock).updateBalanceAndMeta(storedDp, installment, incomingReceipt, accessToken);
    Mockito.verify(debtPositionMapperMock).mapToDto(storedDp);
    Mockito.verify(paymentsProducerServiceMock).notifyPaymentsEvent(mappedDto, PaymentEventType.RT_RECEIVED, "receiptId:" + incomingReceipt.getReceiptId());
    Mockito.verify(debtPositionMapperMock).mapToModel(mappedDto);
  }

  @Test
  void givenStoredPagoPaAndIncomingPagoPaWhenHandleTechDpAlreadyPaidThenSkipUpdates() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    DebtPosition storedDp = podamFactory.manufacturePojo(DebtPosition.class);
    Organization organization = new Organization();

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    DebtPosition result = service.handleTechDpAlreadyPaid(installment, incomingReceipt, storedDp, organization, accessToken);

    // Then
    Assertions.assertSame(storedDp, result);

    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(paymentFlowOrchestratorServiceMock, Mockito.never()).updateBalanceAndMeta(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
    Mockito.verify(paymentFlowOrchestratorServiceMock, Mockito.never()).performStandardUpdate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
    Mockito.verify(technicalDpUpdateServiceMock, Mockito.never()).updateDp(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyLong());
    Mockito.verify(paymentsProducerServiceMock, Mockito.never()).notifyPaymentsEvent(Mockito.any(), Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenStoredFileAndIncomingPagoPaWhenHandleTechDpAlreadyPaidThenPerformStandardUpdateAndPublish() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    DebtPosition storedDp = podamFactory.manufacturePojo(DebtPosition.class);
    Organization organization = new Organization();

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    DebtPositionDTO mappedDto = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPosition expectedResult = new DebtPosition();

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);
    Mockito.when(debtPositionMapperMock.mapToDto(storedDp)).thenReturn(mappedDto);
    Mockito.when(debtPositionMapperMock.mapToModel(mappedDto)).thenReturn(expectedResult);

    // When
    DebtPosition result = service.handleTechDpAlreadyPaid(installment, incomingReceipt, storedDp, organization, accessToken);

    // Then
    Assertions.assertSame(storedDp, result);

    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(paymentFlowOrchestratorServiceMock).performStandardUpdate(storedDp, installment, incomingReceipt, accessToken);
    Mockito.verify(debtPositionMapperMock).mapToDto(storedDp);
    Mockito.verify(paymentsProducerServiceMock).notifyPaymentsEvent(mappedDto, PaymentEventType.RT_RECEIVED, "receiptId:" + incomingReceipt.getReceiptId());
    Mockito.verify(debtPositionMapperMock).mapToModel(mappedDto);
  }

  @Test
  void givenStoredFileAndIncomingFileWhenHandleTechDpAlreadyPaidThenResolveTypeOrgAndUpdateTechDpAndPublish() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    incomingReceipt.setDebtPositionTypeOrgCode("CODE");

    Organization organization = new Organization();
    organization.setOrganizationId(1L);

    DebtPosition storedDp = podamFactory.manufacturePojo(DebtPosition.class);
    storedDp.setDebtPositionTypeOrgId(100L);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    Long resolvedTypeOrgId = 200L;
    DebtPositionDTO updatedDto = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPosition mappedModel = new DebtPosition();

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    Mockito.when(paymentFlowOrchestratorServiceMock.resolveDebtPositionTypeOrgId(
      organization.getOrganizationId(),
      incomingReceipt.getDebtPositionTypeOrgCode(),
      storedDp.getDebtPositionTypeOrgId()
    )).thenReturn(resolvedTypeOrgId);

    Mockito.when(technicalDpUpdateServiceMock.updateDp(
      Mockito.same(storedDp),
      Mockito.same(incomingReceipt),
      Mockito.same(organization),
      Mockito.eq(resolvedTypeOrgId)
    )).thenReturn(updatedDto);

    Mockito.when(debtPositionMapperMock.mapToModel(updatedDto)).thenReturn(mappedModel);

    // When
    DebtPosition result = service.handleTechDpAlreadyPaid(installment, incomingReceipt, storedDp, organization, accessToken);

    // Then
    Assertions.assertSame(storedDp, result);

    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(paymentFlowOrchestratorServiceMock).resolveDebtPositionTypeOrgId(1L, "CODE", 100L);
    Mockito.verify(technicalDpUpdateServiceMock).updateDp(storedDp, incomingReceipt, organization, resolvedTypeOrgId);
    Mockito.verify(paymentsProducerServiceMock).notifyPaymentsEvent(updatedDto, PaymentEventType.RT_RECEIVED, "receiptId:" + incomingReceipt.getReceiptId());
    Mockito.verify(debtPositionMapperMock).mapToModel(updatedDto);
  }

  @Test
  void whenPublishTechDpThenNotifyAndMap() {
    // Given
    DebtPositionDTO dpDto = podamFactory.manufacturePojo(DebtPositionDTO.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    DebtPosition expectedResult = new DebtPosition();

    Mockito.when(debtPositionMapperMock.mapToModel(dpDto)).thenReturn(expectedResult);

    // When
    DebtPosition result = service.publishTechDp(dpDto, receiptDTO);

    // Then
    Assertions.assertSame(expectedResult, result);
    Mockito.verify(paymentsProducerServiceMock).notifyPaymentsEvent(dpDto, PaymentEventType.RT_RECEIVED, "receiptId:" + receiptDTO.getReceiptId());
    Mockito.verify(debtPositionMapperMock).mapToModel(dpDto);
  }
}
