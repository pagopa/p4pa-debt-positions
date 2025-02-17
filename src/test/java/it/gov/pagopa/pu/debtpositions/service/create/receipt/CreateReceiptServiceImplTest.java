package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class CreateReceiptServiceImplTest {

  @Mock
  private ReceiptPIIRepository receiptPIIRepositoryMock;
  @Mock
  private ReceiptNoPIIRepository receiptNoPIIRepositoryMock;
  @Mock
  private ReceiptMapper receiptMapperMock;
  @Mock
  private UpdatePaidDebtPositionService updatePaidDebtPositionServiceMock;
  @Mock
  private CreatePaidTechnicalDebtPositionsService createPaidTechnicalDebtPositionsServiceMock;

  @InjectMocks
  private CreateReceiptServiceImpl receiptService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private String accessToken;
  private ReceiptWithAdditionalNodeDataDTO receipt;
  private Receipt receiptModel;
  private long receiptId;

  @BeforeEach
  void setup(TestInfo info) {
    accessToken = "ACCESS_TOKEN";

    receipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptModel = podamFactory.manufacturePojo(Receipt.class);
    receiptId = receiptModel.getReceiptId();
    receiptModel.setReceiptId(null);

    if (!info.getTags().contains("alreadyHandled")) {
      Mockito.when(receiptPIIRepositoryMock.save(receiptModel)).thenReturn(receiptId);
      Mockito.when(receiptMapperMock.mapToModel(receipt)).thenReturn(receiptModel);
      Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receipt.getPaymentReceiptId())).thenReturn(null);
    }
  }

  @Test
  @Tag("alreadyHandled")
  void givenReceiptAlreadyHandledWhenCreateReceiptThenOk() {
    //given
    receipt.setPaymentReceiptId("NEW_PAYMENT_RECEIPT_ID");
    ReceiptNoPII receiptNoPII = podamFactory.manufacturePojo(ReceiptNoPII.class);
    receiptNoPII.setPaymentReceiptId(receipt.getPaymentReceiptId());
    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receipt.getPaymentReceiptId())).thenReturn(receiptNoPII);

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptNoPII.getReceiptId(), response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verifyNoInteractions(receiptPIIRepositoryMock, receiptMapperMock, updatePaidDebtPositionServiceMock);
  }

  @Test
  void givenValidReceiptWhenCreateReceiptThenOk() {
    //given
    boolean primaryOrgFound = true;
    Mockito.when(updatePaidDebtPositionServiceMock.handleReceiptReceived(receipt, accessToken)).thenReturn(primaryOrgFound);
    Mockito.doNothing().when(createPaidTechnicalDebtPositionsServiceMock).createPaidTechnicalDebtPositionsFromReceipt(receipt, !primaryOrgFound, accessToken);

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptId, response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verify(receiptPIIRepositoryMock, Mockito.times(1)).save(receiptModel);
    Mockito.verify(receiptMapperMock, Mockito.times(1)).mapToModel(receipt);
    Mockito.verify(updatePaidDebtPositionServiceMock, Mockito.times(1)).handleReceiptReceived(receipt, accessToken);
    Mockito.verify(createPaidTechnicalDebtPositionsServiceMock, Mockito.times(1)).createPaidTechnicalDebtPositionsFromReceipt(receipt, primaryOrgFound, accessToken);
  }

}
