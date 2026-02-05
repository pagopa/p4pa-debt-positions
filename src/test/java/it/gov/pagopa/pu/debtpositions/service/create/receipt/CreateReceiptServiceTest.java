package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.mixed.MixedDpPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.PrimaryOrgPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.secondaryorg.SecondaryOrgPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
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
      organizationServiceMock
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
      organizationServiceMock
    );
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenNewReceiptWhenCreateReceiptThenOk(boolean primaryOrgHandledByPu) {
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    String accessToken = "ACCESSTOKEN";
    Organization organization = new Organization();

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(null);

    // When, Then
    testProcessedReceiptAndDpHandling(organization, receiptDTO, accessToken, primaryOrgHandledByPu);
  }

  private void testProcessedReceiptAndDpHandling(Organization organization, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken, boolean primaryOrgHandledByPu) {
    // Given
    Optional<DebtPosition> primaryOrgDp = primaryOrgHandledByPu
      ? Optional.of(new DebtPosition())
      : Optional.empty();
    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptId(-1L);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(receiptPIIRepositoryMock.save(Mockito.same(receiptDTO)))
      .thenReturn(storedReceipt);
    Mockito.when(primaryOrgPaymentHandlerServiceMock.handlePayment(Mockito.same(organization), Mockito.same(receiptDTO), Mockito.same(accessToken)))
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

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(receiptInDb);

    testProcessedReceiptAndDpHandling(organization, receiptDTO, accessToken, primaryOrgHandledByPu);
  }

  @Test
  void givenIudNullWhenCreateReceiptThenOk() {
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setIud(null);
    String accessToken = "ACCESSTOKEN";
    Organization organization = new Organization();

    ReceiptNoPII existingReceiptNoPII = podamFactory.manufacturePojo(ReceiptNoPII.class);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId())).thenReturn(existingReceiptNoPII);
    Mockito.when(primaryOrgPaymentHandlerServiceMock.handlePayment(organization, receiptDTO, accessToken)).thenReturn(Optional.empty());

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

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(receiptInDb);
    Mockito.when(primaryOrgPaymentHandlerServiceMock.handlePayment(organization, receiptDTO, accessToken))
      .thenReturn(Optional.empty());

    ReceiptDTO result = service.createReceipt(receiptDTO, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(receiptInDb.getReceiptId(), result.getReceiptId());

    Mockito.verify(secondaryOrgPaymentHandlerServiceMock)
      .handle(Mockito.same(receiptDTO), Mockito.same(accessToken));
  }
}
