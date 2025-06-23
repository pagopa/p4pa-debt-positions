package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptMapperTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @InjectMocks
  private ReceiptMapper receiptMapper;

  @Test
  void givenValidReceiptDTOWhenMapToModelThenReturnReceipt() {
    //given
    ReceiptDTO receiptDTO = podamFactory.manufacturePojo(ReceiptDTO.class);

    //when
    Receipt response = receiptMapper.mapToModel(receiptDTO);

    //verify
    TestUtils.reflectionEqualsByName(receiptDTO, response);
    TestUtils.checkNotNullFields(response, "updateOperatorExternalId", "updateTraceId", "noPII");
  }

  @Test
  void givenValidReceiptWhenMapToDtoThenReturnReceiptDTO() {
    //given
    Receipt receipt = podamFactory.manufacturePojo(Receipt.class);

    //when
    ReceiptDTO response = receiptMapper.mapToDto(receipt);

    //verify
    TestUtils.reflectionEqualsByName(receipt, response);
    TestUtils.checkNotNullFields(response);
  }

}
