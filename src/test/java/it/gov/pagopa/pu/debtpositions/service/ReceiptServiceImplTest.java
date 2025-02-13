package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
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

  @InjectMocks
  private ReceiptServiceImpl receiptService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void whenGetReceiptDetailThenOk() {
    //given
    Long receiptId = 1L;
    ReceiptDTO expectedReceipt = podamFactory.manufacturePojo(ReceiptDTO.class);

    Mockito.when(receiptPIIRepositoryMock.getReceiptDetail(receiptId)).thenReturn(expectedReceipt);

    //when
    ReceiptDTO response = receiptService.getReceiptDetail(receiptId);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(expectedReceipt, response);

    Mockito.verify(receiptPIIRepositoryMock).getReceiptDetail(receiptId);
  }
}
