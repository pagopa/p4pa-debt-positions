package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.ReceiptBasedTechnicalDpHandlerService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
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

import java.util.*;

@ExtendWith(MockitoExtension.class)
class PrimaryOrgPaymentHandlerServiceTest {

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
      installmentRetrieverServiceMock,
      installmentPaymentHandlerServiceMock,
      technicalDpCreationServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
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
    Broker broker = new Broker();

    // When
    Optional<DebtPosition> result = service.handlePayment(null, receiptDTO, broker, accessToken);

    // Then
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void givenReceiptOnExistentInstallmentWhenHandlePaymentThenOk(){
    // Given
    String accessToken = "ACCESSTOKEN";
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    Organization organization = new Organization();
    Broker broker = new Broker();
    broker.setFlagDelegate(false);
    InstallmentNoPII installment = new InstallmentNoPII();
    DebtPosition dp = new DebtPosition();

    Mockito.when(installmentRetrieverServiceMock.retrieve(Mockito.same(organization), Mockito.same(receiptDTO.getNoticeNumber()), Mockito.same(receiptDTO.getIud())))
      .thenReturn(Optional.of(installment));
    Mockito.when(installmentPaymentHandlerServiceMock.handlePayment(Mockito.same(installment), Mockito.same(receiptDTO), Mockito.same(organization), Mockito.same(accessToken)))
      .thenReturn(dp);

    // When
    Optional<DebtPosition> result = service.handlePayment(organization, receiptDTO, broker, accessToken);

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
    Broker broker = new Broker();
    DebtPosition dp = new DebtPosition();

    Mockito.when(installmentRetrieverServiceMock.retrieve(Mockito.same(organization), Mockito.same(receiptDTO.getNoticeNumber()), Mockito.same(receiptDTO.getIud())))
      .thenReturn(Optional.empty());
    Mockito.when(technicalDpCreationServiceMock.createAndPublishTechDp(Mockito.same(organization), Mockito.same(receiptDTO)))
      .thenReturn(dp);

    // When
    Optional<DebtPosition> result = service.handlePayment(organization, receiptDTO, broker, accessToken);

    // Then
    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(dp, result.get());
  }

  @Test
  void givenBrokerDelegateAndValidOrgWhenHandlePaymentThenOk() {
    String accessToken = "ACCESSTOKEN";
    String fiscalCode = "FISCAL_CODE_123";

    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setOrgFiscalCode(fiscalCode);

    Organization organization = new Organization();
    Broker broker = new Broker();
    broker.setFlagDelegate(true);

    Transfer transfer = new Transfer();
    transfer.setFlagOwner(true);
    transfer.setOrgFiscalCode(fiscalCode);

    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setTransfers(new TreeSet<>(Set.of(transfer)));

    DebtPosition dp = new DebtPosition();

    Mockito.when(installmentRetrieverServiceMock.retrieve(organization, receiptDTO.getNoticeNumber(), receiptDTO.getIud()))
      .thenReturn(Optional.of(installment));
    Mockito.when(installmentPaymentHandlerServiceMock.handlePayment(installment, receiptDTO, organization, accessToken))
      .thenReturn(dp);

    Optional<DebtPosition> result = service.handlePayment(organization, receiptDTO, broker, accessToken);

    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(dp, result.get());
  }

  @Test
  void givenBrokerDelegateAndInvalidOrgWhenHandlePaymentThenThrowInvalidValueException() {
    String accessToken = "ACCESSTOKEN";
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setOrgFiscalCode("EXPECTED_ORG");

    Organization organization = new Organization();
    Broker broker = new Broker();
    broker.setFlagDelegate(true);

    Transfer transfer = new Transfer();
    transfer.setFlagOwner(true);
    transfer.setOrgFiscalCode("DIFFERENT_ORG");

    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setTransfers(new TreeSet<>(Set.of(transfer)));

    Mockito.when(installmentRetrieverServiceMock.retrieve(organization, receiptDTO.getNoticeNumber(), receiptDTO.getIud()))
      .thenReturn(Optional.of(installment));

   Assertions.assertThrows(InvalidValueException.class, () ->
      service.handlePayment(organization, receiptDTO, broker, accessToken)
    );
  }
}
