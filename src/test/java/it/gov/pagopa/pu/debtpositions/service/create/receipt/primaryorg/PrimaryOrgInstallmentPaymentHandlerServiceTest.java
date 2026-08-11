package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.OrdinaryDPPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.ReceiptBasedTechnicalDpHandlerService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrimaryOrgInstallmentPaymentHandlerServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;

  @Mock
  private OrdinaryDPPaymentHandlerService ordinaryDPPaymentHandlerServiceMock;

  @Mock
  private ReceiptBasedTechnicalDpHandlerService technicalDpHandlerServiceMock;

  private PrimaryOrgInstallmentPaymentHandlerService service;

  @BeforeEach
  void init(){
    service = new PrimaryOrgInstallmentPaymentHandlerService(
      debtPositionRepositoryMock,
      ordinaryDPPaymentHandlerServiceMock,
      technicalDpHandlerServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      debtPositionRepositoryMock,
      ordinaryDPPaymentHandlerServiceMock,
      technicalDpHandlerServiceMock
    );
  }

  @Test
  void givenNotExistentDPWhenHandlePaymentThrow() {
    // Given
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setInstallmentId(-1L);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    Organization organization = new Organization();

    when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(installment.getInstallmentId()))
      .thenReturn(null);

    // When, Then
    Assertions.assertThrows(NotFoundException.class, () -> service.handlePayment(installment, receiptDTO, organization, accessToken));
  }

  @Test
  void givenOrdinaryDpWhenHandlePaymentInvokeItsHandler() {
    // Given
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setInstallmentId(-1L);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    DebtPosition expectedResult = new DebtPosition();
    expectedResult.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    Organization organization = new Organization();

    when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(installment.getInstallmentId()))
      .thenReturn(expectedResult);

    // When
    DebtPosition result = service.handlePayment(installment, receiptDTO, organization, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);

    verify(ordinaryDPPaymentHandlerServiceMock)
      .handlePayment(Mockito.same(expectedResult), Mockito.same(installment), Mockito.same(receiptDTO), Mockito.same(accessToken));
  }

  @Test
  void givenTechDpWhenHandlePaymentInvokeItsHandler() {
    // Given
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setInstallmentId(-1L);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    DebtPosition fetchedDp = new DebtPosition();
    fetchedDp.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_FILE);
    Organization organization = new Organization();
    DebtPosition expectedResult = new DebtPosition();

    when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(installment.getInstallmentId()))
      .thenReturn(fetchedDp);

    when(technicalDpHandlerServiceMock.updateAndPublishTechDp(
        Mockito.same(organization),
        Mockito.same(fetchedDp),
        Mockito.same(installment),
        Mockito.same(receiptDTO),
        Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    DebtPosition result = service.handlePayment(installment, receiptDTO, organization, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
