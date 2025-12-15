package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class StandardPaymentUpdateServiceTest {

  @Mock
  private OrdinaryInstallmentPaymentHandlerService installmentPaymentHandlerServiceMock;
  @Mock
  private OrdinaryPaidDPHierarchyUpdateService hierarchyUpdateServiceMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;

  private StandardPaymentUpdateService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    service = new StandardPaymentUpdateService(
      installmentPaymentHandlerServiceMock,
      hierarchyUpdateServiceMock,
      debtPositionServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      installmentPaymentHandlerServiceMock,
      hierarchyUpdateServiceMock,
      debtPositionServiceMock
    );
  }

  @Test
  void whenPerformStandardUpdateThenInvokeHandlersAndSave() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    String accessToken = "ACCESSTOKEN";

    // When
    service.performStandardUpdate(dp, installment, receiptDTO, accessToken);

    // Then
    Mockito.verify(installmentPaymentHandlerServiceMock)
      .updateInstallment(Mockito.same(installment), Mockito.same(receiptDTO), Mockito.same(accessToken));

    Mockito.verify(hierarchyUpdateServiceMock)
      .updateHierarchy(Mockito.same(dp), Mockito.same(installment));

    Mockito.verify(debtPositionServiceMock)
      .saveDebtPosition(Mockito.same(dp));
  }
}
