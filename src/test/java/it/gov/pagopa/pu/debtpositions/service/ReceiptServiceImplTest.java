package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailPIIViewRepository;
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
  private ReceiptDetailPIIViewRepository receiptDetailPIIViewRepositoryMock;

  @InjectMocks
  private ReceiptServiceImpl receiptService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void whenGetReceiptDetailThenOk() {
    //given
    Long receiptId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    ReceiptDetailDTO receipt = podamFactory.manufacturePojo(ReceiptDetailDTO.class);

    Mockito.when(receiptDetailPIIViewRepositoryMock.getReceiptDetail(receiptId,operatorExternalUserId)).thenReturn(receipt);

    //when
    ReceiptDetailDTO response = receiptService.getReceiptDetail(receiptId,operatorExternalUserId);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receipt, response);

    Mockito.verify(receiptDetailPIIViewRepositoryMock).getReceiptDetail(receiptId,operatorExternalUserId);
  }
}
