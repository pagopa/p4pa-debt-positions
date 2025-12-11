package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
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
  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;

  private PaymentFlowOrchestratorService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    service = new PaymentFlowOrchestratorService(
      receiptServiceMock,
      debtPositionRepositoryMock,
      installmentNoPIIRepositoryMock,
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
      unknownDebtPositionTypeOrgRetrieverServiceMock,
      debtPositionRepositoryMock,
      installmentNoPIIRepositoryMock
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
    Runnable partialUpdateAction = Mockito.mock(Runnable.class);
    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleAlreadyPaidLogic(installment, incomingReceipt, dp, fullUpdateAction, partialUpdateAction, syncWorkflowAction);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(fullUpdateAction, Mockito.never()).run();
    Mockito.verify(partialUpdateAction, Mockito.never()).run();
    Mockito.verify(syncWorkflowAction, Mockito.never()).run();
  }

  @Test
  void givenStoredPagoPaAndIncomingFileWhenHandleAlreadyPaidLogicThenUpdateBalanceAndSync() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    incomingReceipt.setBalance("NEW_BALANCE");
    incomingReceipt.setDebtPositionTypeOrgCode("CODE");

    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    Runnable fullUpdateAction = Mockito.mock(Runnable.class);
    Runnable partialUpdateAction = Mockito.mock(Runnable.class);
    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleAlreadyPaidLogic(installment, incomingReceipt, dp, fullUpdateAction, partialUpdateAction, syncWorkflowAction);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(partialUpdateAction).run();
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
    Runnable partialUpdateAction = Mockito.mock(Runnable.class);
    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleAlreadyPaidLogic(installment, incomingReceipt, dp, fullUpdateAction, partialUpdateAction, syncWorkflowAction);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(fullUpdateAction).run();
    Mockito.verify(partialUpdateAction, Mockito.never()).run();
    Mockito.verify(syncWorkflowAction).run();
  }

  @Test
  void givenStoredFileAndIncomingFileWhenHandleAlreadyPaidLogicThenUpdateBalanceAndSync() {
    // Given
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    incomingReceipt.setBalance("NEW_BALANCE");
    incomingReceipt.setDebtPositionTypeOrgCode("CODE");

    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    Runnable fullUpdateAction = Mockito.mock(Runnable.class);
    Runnable partialUpdateAction = Mockito.mock(Runnable.class);
    Runnable syncWorkflowAction = Mockito.mock(Runnable.class);

    Mockito.when(receiptServiceMock.getReceipt(installment.getReceiptId())).thenReturn(storedReceipt);

    // When
    service.handleAlreadyPaidLogic(installment, incomingReceipt, dp, fullUpdateAction, partialUpdateAction, syncWorkflowAction);

    // Then
    Mockito.verify(receiptServiceMock).getReceipt(installment.getReceiptId());
    Mockito.verify(partialUpdateAction).run();
    Mockito.verify(fullUpdateAction, Mockito.never()).run();
    Mockito.verify(syncWorkflowAction).run();
  }

  @Test
  void givenDpTypeOrgIdIsUnknownAndSpecificFoundWhenUpdateBalanceAndMetaThenDirectUpdateAndOverloadedResolve() {
    // Given
    DebtPositionTypeOrg unknownTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionTypeOrgId(unknownTypeOrg.getDebtPositionTypeOrgId());
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    String code = "CODE";
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setDebtPositionTypeOrgCode(code);
    incomingReceipt.setBalance("BAL");

    DebtPositionTypeOrg specificTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(dp.getOrganizationId()))
      .thenReturn(unknownTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(dp.getOrganizationId(), code))
      .thenReturn(Optional.of(specificTypeOrg));

    // When
    service.updateBalanceAndMeta(dp, installment, incomingReceipt, accessToken);

    // Then
    Assertions.assertEquals(specificTypeOrg.getDebtPositionTypeOrgId(), dp.getDebtPositionTypeOrgId());
    Assertions.assertEquals("BAL", installment.getBalance());
    Mockito.verify(debtPositionRepositoryMock).updateDebtPositionTypeOrgId(dp.getDebtPositionId(), specificTypeOrg.getDebtPositionTypeOrgId());
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, specificTypeOrg, accessToken);
    Mockito.verify(installmentNoPIIRepositoryMock).updateBalance(installment.getInstallmentId(), "BAL");
  }

  @Test
  void givenDpTypeOrgIdIsUnknownAndSpecificNotFoundWhenUpdateBalanceAndMetaThenUseUnknownForResolve() {
    // Given
    DebtPositionTypeOrg unknownTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionTypeOrgId(unknownTypeOrg.getDebtPositionTypeOrgId());
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    String code = "CODE";
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setDebtPositionTypeOrgCode(code);
    incomingReceipt.setBalance("BAL");

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(dp.getOrganizationId()))
      .thenReturn(unknownTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(dp.getOrganizationId(), code))
      .thenReturn(Optional.empty());

    // When
    service.updateBalanceAndMeta(dp, installment, incomingReceipt, accessToken);

    // Then
    Assertions.assertEquals(unknownTypeOrg.getDebtPositionTypeOrgId(), dp.getDebtPositionTypeOrgId());
    Assertions.assertEquals("BAL", installment.getBalance());
    Mockito.verify(debtPositionRepositoryMock, Mockito.never()).updateDebtPositionTypeOrgId(Mockito.anyLong(), Mockito.anyLong());
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, unknownTypeOrg, accessToken);
    Mockito.verify(installmentNoPIIRepositoryMock).updateBalance(installment.getInstallmentId(), "BAL");
  }

  @Test
  void givenDpTypeOrgIdIsSetDifferentFromUnknownWhenUpdateBalanceAndMetaThenStandardResolve() {
    // Given
    DebtPositionTypeOrg unknownTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    unknownTypeOrg.setDebtPositionTypeOrgId(999L);
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionTypeOrgId(123L);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO incomingReceipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    incomingReceipt.setBalance("BAL");

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(dp.getOrganizationId()))
      .thenReturn(unknownTypeOrg);

    // When
    service.updateBalanceAndMeta(dp, installment, incomingReceipt, accessToken);

    // Then
    Assertions.assertEquals("BAL", installment.getBalance());
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.never()).findByOrganizationIdAndCode(Mockito.anyLong(), Mockito.anyString());
    Mockito.verify(debtPositionRepositoryMock, Mockito.never()).updateDebtPositionTypeOrgId(Mockito.anyLong(), Mockito.anyLong());
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, accessToken);
    Mockito.verify(installmentNoPIIRepositoryMock).updateBalance(installment.getInstallmentId(), "BAL");
  }
}
