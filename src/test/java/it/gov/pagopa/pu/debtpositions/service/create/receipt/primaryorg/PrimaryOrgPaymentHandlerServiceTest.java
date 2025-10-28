package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.ReceiptBasedTechnicalDpHandlerService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
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
class PrimaryOrgPaymentHandlerServiceTest {

  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private PrimaryOrgInstallmentRetrieverService installmentRetrieverServiceMock;
  @Mock
  private PrimaryOrgInstallmentPaymentHandlerService installmentPaymentHandlerServiceMock;
  @Mock
  private ReceiptBasedTechnicalDpHandlerService technicalDpCreationServiceMock;

  private PrimaryOrgPaymentHandlerService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init(){
    service = new PrimaryOrgPaymentHandlerService(
      organizationServiceMock,
      installmentRetrieverServiceMock,
      installmentPaymentHandlerServiceMock,
      technicalDpCreationServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      organizationServiceMock,
      installmentRetrieverServiceMock,
      installmentPaymentHandlerServiceMock,
      technicalDpCreationServiceMock
    );
  }

  @Test
  void givenNotHandledPrimaryOrgWhenHandlePaymentThenReturnEmpty(){
    // Given
    String accessToken = "ACCESSTOKEN";
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken))
      .thenReturn(Optional.empty());

    // When
    Optional<DebtPosition> result = service.handlePayment(receiptDTO, accessToken);

    // Then
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void givenReceiptOnExistentInstallmentWhenHandlePaymentThenOk(){
    // Given
    String accessToken = "ACCESSTOKEN";
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    Organization organization = new Organization();
    InstallmentNoPII installment = new InstallmentNoPII();
    DebtPosition dp = new DebtPosition();

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(installmentRetrieverServiceMock.retrieve(Mockito.same(organization), Mockito.same(receiptDTO.getNoticeNumber()), Mockito.same(receiptDTO.getIud())))
      .thenReturn(Optional.of(installment));
    Mockito.when(installmentPaymentHandlerServiceMock.handlePayment(Mockito.same(installment), Mockito.same(receiptDTO)))
      .thenReturn(dp);

    // When
    Optional<DebtPosition> result = service.handlePayment(receiptDTO, accessToken);

    // Then
    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(dp, result.get());
  }

  @Test
  void givenReceiptOnNotExistentInstallmentWhenHandlePaymentThenOk(){
    // Given
    String accessToken = "ACCESSTOKEN";
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    Organization organization = new Organization();
    DebtPosition dp = new DebtPosition();

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(installmentRetrieverServiceMock.retrieve(Mockito.same(organization), Mockito.same(receiptDTO.getNoticeNumber()), Mockito.same(receiptDTO.getIud())))
      .thenReturn(Optional.empty());
    Mockito.when(technicalDpCreationServiceMock.createAndPublishTechDp(Mockito.same(organization), Mockito.same(receiptDTO)))
      .thenReturn(dp);

    // When
    Optional<DebtPosition> result = service.handlePayment(receiptDTO, accessToken);

    // Then
    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(dp, result.get());
  }
}
