package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrdinaryDPPaymentHandlerServiceTest {

  @Mock
  private OrdinaryInstallmentPaymentHandlerService installmentPaymentHandlerServiceMock;
  @Mock
  private OrdinaryPaidDPHierarchyUpdateService hierarchyUpdateServiceMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private DebtPositionMapper mapperMock;
  @Mock
  private DebtPositionSyncService syncServiceMock;

  private OrdinaryDPPaymentHandlerService service;

  @BeforeEach
  void init(){
    service = new OrdinaryDPPaymentHandlerService(
      installmentPaymentHandlerServiceMock,
      hierarchyUpdateServiceMock,
      debtPositionServiceMock,
      mapperMock,
      syncServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      installmentPaymentHandlerServiceMock,
      hierarchyUpdateServiceMock,
      debtPositionServiceMock,
      mapperMock,
      syncServiceMock
    );
  }

  @ParameterizedTest
  @EnumSource(InstallmentStatus.class)
  void whenHandlePaymentThenOk(InstallmentStatus installmentStatus){
    test(installmentStatus);
  }

  private void test(InstallmentStatus installmentStatus) {
    String accessToken = "ACCESSTOKEN";
    DebtPosition dp = new DebtPosition();
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(installmentStatus);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setReceiptId(-1L);
    DebtPositionDTO dpDTO = new DebtPositionDTO();

    boolean paidStatus = InstallmentUtils.PAID_STATUSES.contains(installmentStatus);

    Mockito.when(mapperMock.mapToDto(Mockito.same(dp)))
      .thenReturn(dpDTO);

    // When
    service.handlePayment(dp, installment, receiptDTO, accessToken);

    // Then
    if(!paidStatus){
      Mockito.verify(installmentPaymentHandlerServiceMock)
        .updateInstallment(Mockito.same(installment), Mockito.same(receiptDTO), Mockito.same(accessToken));

      Mockito.verify(hierarchyUpdateServiceMock)
        .updateHierarchy(Mockito.same(dp), Mockito.same(installment));
      Mockito.verify(debtPositionServiceMock)
        .saveDebtPosition(Mockito.same(dp));
    }

    Mockito.verify(syncServiceMock)
      .syncDebtPosition(
        Mockito.same(dpDTO),
        Mockito.eq(new WfExecutionParameters()),
        Mockito.eq(PaymentEventType.RT_RECEIVED),
        Mockito.eq("receiptId:-1"),
        Mockito.eq(accessToken));
  }
}
