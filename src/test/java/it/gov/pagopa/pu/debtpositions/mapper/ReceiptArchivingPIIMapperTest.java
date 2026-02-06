package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptArchivingView;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
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

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  ReceiptArchivingPIIMapper receiptArchivingPIIMapper;

  @BeforeEach
  void setUp() {
    receiptArchivingPIIMapper = new ReceiptArchivingPIIMapper(personalDataServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      personalDataServiceMock);
  }

  @Test
  void givenValidReceiptArchivingNoPIIView_whenMapToReceiptArchivingView_thenReturnReceiptArchivingView() {
    //given
    ReceiptArchivingNoPIIView receiptArchivingNoPIIView = podamFactory.manufacturePojo(ReceiptArchivingNoPIIView.class);
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    InstallmentPIIDTO installmentPIIDTO = podamFactory.manufacturePojo(InstallmentPIIDTO.class);

    Mockito.when(personalDataServiceMock.get(receiptArchivingNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);
    Mockito.when(personalDataServiceMock.get(receiptArchivingNoPIIView.getInstallmentPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    //when
    ReceiptArchivingView result = receiptArchivingPIIMapper.map(receiptArchivingNoPIIView);
    //then
    Assertions.assertNotNull(result);
    Assertions.assertEquals(installmentPIIDTO.getOriginalRemittanceInformation(), result.getOriginalRemittanceInformation());
    TestUtils.reflectionEqualsByName(receiptArchivingNoPIIView, result);
    TestUtils.checkNotNullFields(result);
  }

  @Test
  void givenValidReceiptArchivingNoPIIViewWithoutPayer_whenMapToReceiptArchivingView_thenReturnReceiptArchivingView() {
    //given
    ReceiptArchivingNoPIIView receiptArchivingNoPIIView = podamFactory.manufacturePojo(ReceiptArchivingNoPIIView.class);
    InstallmentPIIDTO installmentPIIDTO = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    installmentPIIDTO.setOriginalRemittanceInformation(null);
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    receiptPIIDTO.setPayer(null);

    Mockito.when(personalDataServiceMock.get(receiptArchivingNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);
    Mockito.when(personalDataServiceMock.get(receiptArchivingNoPIIView.getInstallmentPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    //when
    ReceiptArchivingView result = receiptArchivingPIIMapper.map(receiptArchivingNoPIIView);
    //then
    Assertions.assertNotNull(result);
    Assertions.assertNull(result.getPayer());
    Assertions.assertNull(result.getOriginalRemittanceInformation());
    TestUtils.reflectionEqualsByName(receiptArchivingNoPIIView, result);
    TestUtils.checkNotNullFields(result, "payer", "originalRemittanceInformation");
  }
}
