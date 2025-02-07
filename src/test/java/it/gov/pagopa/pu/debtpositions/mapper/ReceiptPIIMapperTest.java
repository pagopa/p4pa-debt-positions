package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import uk.co.jemos.podam.api.PodamFactory;

import java.nio.charset.StandardCharsets;

@ExtendWith(MockitoExtension.class)
class ReceiptPIIMapperTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private DataCipherService dataCipherServiceMock;

  @Mock
  private PersonalDataService personalDataServiceMock;

  @InjectMocks
  private ReceiptPIIMapper receiptPIIMapper;

  @Test
  void givenValidReceiptWhenMapThenReturnPairReceiptNoPIIandPII() {
    //given
    Receipt receipt = podamFactory.manufacturePojo(Receipt.class);
    byte[] fiscalCodeHash = "FISCAL_CODE_HASH".getBytes(StandardCharsets.UTF_8);
    Mockito.when(dataCipherServiceMock.hash(receipt.getDebtor().getFiscalCode())).thenReturn(fiscalCodeHash);

    //when
    Pair<ReceiptNoPII, ReceiptPIIDTO> response = receiptPIIMapper.map(receipt);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertNotNull(response.getFirst());
    TestUtils.reflectionEqualsByName(receipt, response.getFirst(), "debtor", "payer", "updateOperatorExternalId");
    TestUtils.checkNotNullFields(response.getFirst());
    Assertions.assertNotNull(response.getSecond());
    TestUtils.reflectionEqualsByName(receipt.getDebtor(), response.getSecond().getDebtor());
    TestUtils.reflectionEqualsByName(receipt.getPayer(), response.getSecond().getPayer());
    TestUtils.checkNotNullFields(response.getSecond());
  }

  @Test
  void givenValidReceiptNoPIIWhenMapThenReturnReceipt() {
    //given
    ReceiptNoPII receipt = podamFactory.manufacturePojo(ReceiptNoPII.class);
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    Mockito.when(personalDataServiceMock.get(receipt.getPersonalDataId(),ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);

    //when
    Receipt response = receiptPIIMapper.map(receipt);

    //verify
    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(receipt, response, "debtor", "payer");
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getDebtor(), response.getDebtor());
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getPayer(), response.getPayer());
    TestUtils.checkNotNullFields(response);
    Mockito.verify(personalDataServiceMock, Mockito.times(1)).get(receipt.getPersonalDataId(),ReceiptPIIDTO.class);
  }

}
