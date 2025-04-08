package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptArchivingView;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptArchivingPIIMapperTest {

  @Mock
  private PersonalDataService personalDataServiceMock;
  @Mock
  private PersonMapper personMapperMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  ReceiptArchivingPIIMapper receiptArchivingPIIMapper;

  @BeforeEach
  void setUp() {
    receiptArchivingPIIMapper = new ReceiptArchivingPIIMapper(personalDataServiceMock, personMapperMock);
  }

  @Test
  void givenValidReceiptArchivingNoPIIView_whenMapToReceiptArchivingView_thenReturnReceiptArchivingView() {
    //given
    ReceiptArchivingNoPIIView receiptArchivingNoPIIView = podamFactory.manufacturePojo(ReceiptArchivingNoPIIView.class);
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    PersonDTO personDTO = podamFactory.manufacturePojo(PersonDTO.class);

    Mockito.when(personalDataServiceMock.get(receiptArchivingNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);
    Mockito.when(personMapperMock.mapToDto(receiptPIIDTO.getDebtor())).thenReturn(personDTO);
    Mockito.when(personMapperMock.mapToDto(receiptPIIDTO.getPayer())).thenReturn(personDTO);
    //when
    ReceiptArchivingView result = receiptArchivingPIIMapper.map(receiptArchivingNoPIIView);
    //then
    Assertions.assertNotNull(result);
    TestUtils.reflectionEqualsByName(receiptArchivingNoPIIView, result);
    TestUtils.reflectionEqualsByName(personDTO, result.getDebtor());
    TestUtils.reflectionEqualsByName(personDTO, result.getPayer());
    TestUtils.checkNotNullFields(result);
  }
}
