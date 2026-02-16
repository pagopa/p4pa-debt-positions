package it.gov.pagopa.pu.debtpositions.mapper.pii;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ReceiptPIIMapperTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private DataCipherService dataCipherServiceMock;

  @Mock
  private PersonalDataService personalDataServiceMock;

  @InjectMocks
  private ReceiptPIIMapper mapper;

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      dataCipherServiceMock,
      personalDataServiceMock);
  }

  @Test
  void givenValidReceiptWhenMapThenReturnPairReceiptNoPIIandPII() {
    //given
    ReceiptDTO receipt = podamFactory.manufacturePojo(ReceiptDTO.class);
    byte[] fiscalCodeHash = "FISCAL_CODE_HASH".getBytes(StandardCharsets.UTF_8);
    Mockito.when(dataCipherServiceMock.hash(receipt.getDebtor().getFiscalCode())).thenReturn(fiscalCodeHash);

    //when
    Pair<ReceiptNoPII, ReceiptPIIDTO> response = mapper.map(receipt);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertNotNull(response.getFirst());
    TestUtils.reflectionEqualsByName(receipt, response.getFirst(), "debtor", "payer");
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
    ReceiptDTO response = mapper.map(receipt);

    //verify
    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(receipt, response, "debtor", "payer");
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getDebtor(), response.getDebtor());
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getPayer(), response.getPayer());
    TestUtils.checkNotNullFields(response);
  }


  @Test
  void testMapAll() {
    //given
    ReceiptNoPII noPii1 = podamFactory.manufacturePojo(ReceiptNoPII.class);
    ReceiptNoPII noPii2 = podamFactory.manufacturePojo(ReceiptNoPII.class);
    List<ReceiptNoPII> noPiiDtos = List.of(noPii1, noPii2);

    ReceiptPIIDTO piiDto1 = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    ReceiptPIIDTO piiDto2 = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    Mockito.when(personalDataServiceMock.getAll(Set.of(noPii1.getPersonalDataId(), noPii2.getPersonalDataId()), ReceiptPIIDTO.class))
      .thenReturn(Map.of(
        noPii1.getPersonalDataId(), piiDto1,
        noPii2.getPersonalDataId(), piiDto2
      ));

    mapper = Mockito.spy(mapper);

    ReceiptDTO expectedFullDto1 = podamFactory.manufacturePojo(ReceiptDTO.class);
    Mockito.doReturn(expectedFullDto1)
      .when(mapper)
      .map(noPii1, piiDto1);

    ReceiptDTO expectedFullDto2 = podamFactory.manufacturePojo(ReceiptDTO.class);
    Mockito.doReturn(expectedFullDto2)
      .when(mapper)
      .map(noPii2, piiDto2);

    //when
    List<ReceiptDTO> result = mapper.mapAll(noPiiDtos);
    //then
    assertEquals(
      List.of(expectedFullDto1, expectedFullDto2),
      result
    );
  }
}
