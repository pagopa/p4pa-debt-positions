package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceImplTest {

  @Mock
  private ReceiptPIIRepository receiptPIIRepositoryMock;
  @Mock
  private ReceiptMapper receiptMapperMock;

  @InjectMocks
  private ReceiptServiceImpl receiptService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenValidOrganizationAndNavWhGetReceiptsByOrganizationIdAndNavThenOk() {
    //given
    ReceiptWithAdditionalNodeDataDTO receipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    Receipt receiptModel = podamFactory.manufacturePojo(Receipt.class);
    long receiptId = receiptModel.getReceiptId();
    receiptModel.setReceiptId(null);

    Mockito.when(receiptMapperMock.mapToModel(receipt)).thenReturn(receiptModel);
    Mockito.when(receiptPIIRepositoryMock.save(receiptModel)).thenReturn(receiptId);

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptId, response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verify(receiptPIIRepositoryMock, Mockito.times(1)).save(receiptModel);
    Mockito.verify(receiptMapperMock, Mockito.times(1)).mapToModel(receipt);
  }
}
