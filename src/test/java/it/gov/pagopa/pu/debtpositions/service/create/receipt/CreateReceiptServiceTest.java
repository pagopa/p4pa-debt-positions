package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.pii.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.mixed.MixedDpPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.PrimaryOrgPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.secondaryorg.SecondaryOrgPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CreateReceiptServiceTest {

  @Mock
  private ReceiptPIIRepository receiptPIIRepositoryMock;
  @Mock
  private ReceiptNoPIIRepository receiptNoPIIRepositoryMock;
  @Mock
  private PrimaryOrgPaymentHandlerService primaryOrgPaymentHandlerServiceMock;
  @Mock
  private SecondaryOrgPaymentHandlerService secondaryOrgPaymentHandlerServiceMock;
  @Mock
  private MixedDpPaymentHandlerService mixedDpPaymentHandlerServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private BrokerService brokerServiceMock;

  private CreateReceiptService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    service = new CreateReceiptServiceImpl(
      receiptNoPIIRepositoryMock,
      receiptPIIRepositoryMock,
      primaryOrgPaymentHandlerServiceMock,
      secondaryOrgPaymentHandlerServiceMock,
      mixedDpPaymentHandlerServiceMock,
      organizationServiceMock,
      brokerServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      receiptNoPIIRepositoryMock,
      receiptPIIRepositoryMock,
      primaryOrgPaymentHandlerServiceMock,
      secondaryOrgPaymentHandlerServiceMock,
      mixedDpPaymentHandlerServiceMock,
      organizationServiceMock,
      brokerServiceMock
    );
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenNewReceiptWhenCreateReceiptThenOk(boolean primaryOrgHandledByPu) {
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    String accessToken = "ACCESSTOKEN";
    Organization organization = new Organization();
    organization.setOrganizationId(receiptDTO.getOrganizationId());
    organization.setOrgFiscalCode(receiptDTO.getOrgFiscalCode());
    organization.setBrokerId(1L);
    Broker broker = new Broker();
    broker.setFlagDelegate(false);

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(null);

    // When, Then
    testProcessedReceiptAndDpHandling(organization, broker, receiptDTO, accessToken, primaryOrgHandledByPu);
  }

  private void testProcessedReceiptAndDpHandling(Organization organization, Broker broker, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken, boolean primaryOrgHandledByPu) {
    // Given
    Optional<DebtPosition> primaryOrgDp = primaryOrgHandledByPu
      ? Optional.of(new DebtPosition())
      : Optional.empty();
    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptId(-1L);

    Mockito.when(organizationServiceMock.getOrganizationById(receiptDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(brokerServiceMock.findById(organization.getBrokerId(), accessToken))
      .thenReturn(broker);
    Mockito.when(receiptPIIRepositoryMock.save(Mockito.same(receiptDTO)))
      .thenReturn(storedReceipt);
    Mockito.when(primaryOrgPaymentHandlerServiceMock.handlePayment(Mockito.same(organization), Mockito.same(receiptDTO), Mockito.same(broker), Mockito.same(accessToken)))
      .thenReturn(primaryOrgDp);

    // When
    ReceiptDTO result = service.createReceipt(receiptDTO, accessToken);

    // Then
    Assertions.assertSame(receiptDTO, result);
    Assertions.assertSame(receiptDTO.getReceiptId(), storedReceipt.getReceiptId());
    Assertions.assertSame(receiptDTO.getNoPII(), storedReceipt.getNoPII());

    Mockito.verify(secondaryOrgPaymentHandlerServiceMock)
      .handle(Mockito.same(receiptDTO), Mockito.same(accessToken));
    if (primaryOrgHandledByPu) {
      Mockito.verify(mixedDpPaymentHandlerServiceMock)
        .handle(Mockito.same(primaryOrgDp.get()), Mockito.same(receiptDTO), Mockito.same(accessToken));
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenExistentReceiptAndManualImportWhenCreateReceiptThenOk(boolean primaryOrgHandledByPu) {
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    String accessToken = "ACCESSTOKEN";
    ReceiptNoPII receiptInDb = podamFactory.manufacturePojo(ReceiptNoPII.class);
    receiptInDb.setReceiptId(-1L);
    receiptInDb.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    Organization organization = new Organization();
    organization.setOrganizationId(receiptDTO.getOrganizationId());
    organization.setOrgFiscalCode(receiptDTO.getOrgFiscalCode());
    organization.setBrokerId(1L);
    Broker broker = new Broker();
    broker.setFlagDelegate(false);

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(receiptInDb);

    testProcessedReceiptAndDpHandling(organization, broker, receiptDTO, accessToken, primaryOrgHandledByPu);
  }

  @Test
  void givenIudNullWhenCreateReceiptThenOk() {
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setIud(null);
    String accessToken = "ACCESSTOKEN";
    Organization organization = new Organization();
    organization.setOrganizationId(receiptDTO.getOrganizationId());
    organization.setOrgFiscalCode(receiptDTO.getOrgFiscalCode());
    organization.setBrokerId(1L);
    Broker broker = new Broker();
    broker.setFlagDelegate(false);

    ReceiptNoPII existingReceiptNoPII = podamFactory.manufacturePojo(ReceiptNoPII.class);

    Mockito.when(organizationServiceMock.getOrganizationById(receiptDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(brokerServiceMock.findById(organization.getBrokerId(), accessToken))
      .thenReturn(broker);
    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId())).thenReturn(existingReceiptNoPII);
    Mockito.when(primaryOrgPaymentHandlerServiceMock.handlePayment(organization, receiptDTO, broker, accessToken)).thenReturn(Optional.empty());

    ReceiptDTO result = service.createReceipt(receiptDTO, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(existingReceiptNoPII.getReceiptId(), result.getReceiptId());

    Mockito.verify(secondaryOrgPaymentHandlerServiceMock)
      .handle(Mockito.same(receiptDTO), Mockito.same(accessToken));
  }

  @Test
  void givenManualExistingReceiptWhenCreateReceiptThenOk() {
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    String accessToken = "ACCESSTOKEN";
    ReceiptNoPII receiptInDb = podamFactory.manufacturePojo(ReceiptNoPII.class);
    receiptInDb.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    Organization organization = new Organization();
    organization.setBrokerId(1L);
    Broker broker = new Broker();
    broker.setFlagDelegate(true);

    Mockito.when(organizationServiceMock.getOrganizationById(receiptDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(brokerServiceMock.findById(organization.getBrokerId(), accessToken))
      .thenReturn(broker);
    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(receiptInDb);
    Mockito.when(primaryOrgPaymentHandlerServiceMock.handlePayment(organization, receiptDTO, broker, accessToken))
      .thenReturn(Optional.empty());

    ReceiptDTO result = service.createReceipt(receiptDTO, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(receiptInDb.getReceiptId(), result.getReceiptId());

    Mockito.verify(secondaryOrgPaymentHandlerServiceMock)
      .handle(Mockito.same(receiptDTO), Mockito.same(accessToken));
  }

  @Test
  void givenUnknownOrganizationWhenCreateReceiptThenUpdateFiscalCodeWithPrefix() {
    String originalFiscalCode = "12345678901";
    String expectedFiscalCode = "UNKNOWN_" + originalFiscalCode;
    String accessToken = "ACCESSTOKEN";

    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setOrgFiscalCode(originalFiscalCode);

    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptId(999L);

    Mockito.when(organizationServiceMock.getOrganizationById(receiptDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.empty());

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(null);

    Mockito.when(receiptPIIRepositoryMock.save(Mockito.same(receiptDTO)))
      .thenReturn(storedReceipt);

    Mockito.when(primaryOrgPaymentHandlerServiceMock.handlePayment(Mockito.isNull(), Mockito.same(receiptDTO), Mockito.isNull(),  Mockito.same(accessToken)))
      .thenReturn(Optional.empty());

    ReceiptDTO result = service.createReceipt(receiptDTO, accessToken);

    Assertions.assertEquals(expectedFiscalCode, receiptDTO.getOrgFiscalCode());
    Assertions.assertEquals(storedReceipt.getReceiptId(), result.getReceiptId());

    Mockito.verify(secondaryOrgPaymentHandlerServiceMock).handle(receiptDTO, accessToken);
  }

  @Test
  void givenOrgMismatchAndNotDelegateWhenCreateReceiptThenThrowInvalidValueException() {
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setOrgFiscalCode("FISCAL_CODE");
    String accessToken = "ACCESSTOKEN";

    Organization organization = new Organization();
    organization.setOrganizationId(receiptDTO.getOrganizationId());
    organization.setOrgFiscalCode("FISCAL_CODE2");
    organization.setBrokerId(1L);
    Broker broker = new Broker();
    broker.setFlagDelegate(false);

    Mockito.when(organizationServiceMock.getOrganizationById(receiptDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(brokerServiceMock.findById(organization.getBrokerId(), accessToken))
      .thenReturn(broker);

    Assertions.assertThrows(InvalidValueException.class,
      () -> service.createReceipt(receiptDTO, accessToken));
  }
}
