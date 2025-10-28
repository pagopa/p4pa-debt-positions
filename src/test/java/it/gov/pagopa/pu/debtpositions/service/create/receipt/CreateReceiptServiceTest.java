package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
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

  private CreateReceiptService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init(){
    service = new CreateReceiptServiceImpl(
      receiptNoPIIRepositoryMock,
      receiptPIIRepositoryMock,
      primaryOrgPaymentHandlerServiceMock,
      secondaryOrgPaymentHandlerServiceMock,
      mixedDpPaymentHandlerServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      receiptNoPIIRepositoryMock,
      receiptPIIRepositoryMock,
      primaryOrgPaymentHandlerServiceMock,
      secondaryOrgPaymentHandlerServiceMock,
      mixedDpPaymentHandlerServiceMock
    );
  }

  @Test
  void givenNewReceiptWhenCreateReceiptThenOk(){
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    String accessToken = "ACCESSTOKEN";

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(null);

    // When, Then
    testProcessedReceiptAndDpHandling(receiptDTO, accessToken);
  }

  private void testProcessedReceiptAndDpHandling(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    // Given
    DebtPosition primaryOrgDp = new DebtPosition();
    ReceiptDTO storedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    storedReceipt.setReceiptId(-1L);

    Mockito.when(receiptPIIRepositoryMock.save(Mockito.same(receiptDTO)))
      .thenReturn(storedReceipt);
    Mockito.when(primaryOrgPaymentHandlerServiceMock.handle(Mockito.same(receiptDTO), Mockito.same(accessToken)))
      .thenReturn(primaryOrgDp);

    // When
    ReceiptDTO result = service.createReceipt(receiptDTO, accessToken);

    // Then
    Assertions.assertSame(receiptDTO, result);
    Assertions.assertSame(receiptDTO.getReceiptId(), storedReceipt.getReceiptId());
    Assertions.assertSame(receiptDTO.getNoPII(), storedReceipt.getNoPII());

    Mockito.verify(secondaryOrgPaymentHandlerServiceMock)
      .handle(Mockito.same(primaryOrgDp), Mockito.same(receiptDTO.getTransfers()), Mockito.same(accessToken));
    Mockito.verify(mixedDpPaymentHandlerServiceMock)
      .handle(Mockito.same(primaryOrgDp));
  }

  @Test
  void givenExistentReceiptAndNotManualImportWhenCreateReceiptThenDoNothing(){
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setIud(null);
    String accessToken = "ACCESSTOKEN";
    ReceiptNoPII receiptInDb = podamFactory.manufacturePojo(ReceiptNoPII.class);
    receiptInDb.setReceiptId(-1L);
    receiptInDb.setReceiptOrigin(receiptDTO.getReceiptOrigin());

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(receiptInDb);

    // When
    ReceiptDTO result = service.createReceipt(receiptDTO, accessToken);

    // Then
    Assertions.assertSame(receiptDTO, result);
    Assertions.assertSame(receiptDTO.getReceiptId(), receiptInDb.getReceiptId());
  }

  @Test
  void givenExistentReceiptAndManualImportAndUnexpectedOriginWhenCreateReceiptThenConflict(){
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    String accessToken = "ACCESSTOKEN";
    ReceiptNoPII receiptInDb = podamFactory.manufacturePojo(ReceiptNoPII.class);
    receiptInDb.setReceiptId(-1L);
    receiptInDb.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(receiptInDb);

    // When
    Assertions.assertThrows(ConflictErrorException.class, () -> service.createReceipt(receiptDTO, accessToken));
  }

  @Test
  void givenExistentReceiptAndDifferentOriginWhenCreateReceiptThenConflict(){
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    String accessToken = "ACCESSTOKEN";
    ReceiptNoPII receiptInDb = podamFactory.manufacturePojo(ReceiptNoPII.class);
    receiptInDb.setReceiptId(-1L);
    receiptInDb.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(receiptInDb);

    // When
    Assertions.assertThrows(ConflictErrorException.class, () -> service.createReceipt(receiptDTO, accessToken));
  }

  @Test
  void givenExistentReceiptAndManualImportWhenCreateReceiptThenOk(){
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    String accessToken = "ACCESSTOKEN";
    ReceiptNoPII receiptInDb = podamFactory.manufacturePojo(ReceiptNoPII.class);
    receiptInDb.setReceiptId(-1L);
    receiptInDb.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);

    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId()))
      .thenReturn(receiptInDb);

    testProcessedReceiptAndDpHandling(receiptDTO, accessToken);
  }
}
