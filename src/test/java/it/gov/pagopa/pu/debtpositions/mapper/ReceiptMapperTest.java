package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Person;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptMapperTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private PersonMapper personMapperMock;

  @InjectMocks
  private ReceiptMapper receiptMapper;

  @Test
  void givenValidReceiptDTOWhenMapToModelThenReturnReceipt() {
    //given
    ReceiptDTO receiptDTO = podamFactory.manufacturePojo(ReceiptDTO.class);
    Person debtor = podamFactory.manufacturePojo(Person.class);
    Person payer = podamFactory.manufacturePojo(Person.class);
    Mockito.when(personMapperMock.mapToModel(receiptDTO.getDebtor())).thenReturn(debtor);
    Mockito.when(personMapperMock.mapToModel(receiptDTO.getPayer())).thenReturn(payer);

    //when
    Receipt response = receiptMapper.mapToModel(receiptDTO);

    //verify
    TestUtils.reflectionEqualsByName(receiptDTO, response, "debtor", "payer");
    TestUtils.checkNotNullFields(response, "updateOperatorExternalId", "noPII");
    Mockito.verify(personMapperMock, Mockito.times(1)).mapToModel(receiptDTO.getDebtor());
    Mockito.verify(personMapperMock, Mockito.times(1)).mapToModel(receiptDTO.getPayer());
  }

  @Test
  void givenValidReceiptWhenMapToDtoThenReturnReceiptDTO() {
    //given
    Receipt receipt = podamFactory.manufacturePojo(Receipt.class);
    PersonDTO debtor = podamFactory.manufacturePojo(PersonDTO.class);
    PersonDTO payer = podamFactory.manufacturePojo(PersonDTO.class);
    Mockito.when(personMapperMock.mapToDto(receipt.getDebtor())).thenReturn(debtor);
    Mockito.when(personMapperMock.mapToDto(receipt.getPayer())).thenReturn(payer);

    //when
    ReceiptDTO response = receiptMapper.mapToDto(receipt);

    //verify
    TestUtils.reflectionEqualsByName(receipt, response, "debtor", "payer");
    TestUtils.checkNotNullFields(response);
    Mockito.verify(personMapperMock, Mockito.times(1)).mapToDto(receipt.getDebtor());
    Mockito.verify(personMapperMock, Mockito.times(1)).mapToDto(receipt.getPayer());
  }

}
