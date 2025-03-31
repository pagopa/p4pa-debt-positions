package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
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
    InstallmentPIIDTO installmentPIIDTO = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    PersonDTO personDTO = podamFactory.manufacturePojo(PersonDTO.class);

    Mockito.when(personalDataServiceMock.get(receiptArchivingNoPIIView.getInstallmentPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personMapperMock.mapToDto(installmentPIIDTO.getDebtor())).thenReturn(personDTO);
    //when
    ReceiptArchivingView result = receiptArchivingPIIMapper.map(receiptArchivingNoPIIView);
    //then
    Assertions.assertNotNull(result);
    TestUtils.reflectionEqualsByName(receiptArchivingNoPIIView, result);
    TestUtils.reflectionEqualsByName(personDTO, result.getDebtor());
    TestUtils.checkNotNullFields(result);
  }
}
