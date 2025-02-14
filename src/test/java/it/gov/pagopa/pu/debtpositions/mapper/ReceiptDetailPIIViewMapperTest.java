package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptDetailPIIViewMapperTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private PersonalDataService personalDataServiceMock;

  @Spy
  private PersonMapper personMapperSpy;

  @InjectMocks
  private ReceiptDetailPIIViewMapper receiptDetailPIIViewMapper;

  @Test
  void givenValidReceiptNoPIIWhenMapToReceiptDTOThenReturnReceiptDTO() {
    //given
    ReceiptDetailNoPIIView receiptDetailNoPIIView = podamFactory.manufacturePojo(ReceiptDetailNoPIIView.class);
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    Mockito.when(personalDataServiceMock.get(receiptDetailNoPIIView.getDebtorPersonalDataId(),ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);
    //when
    ReceiptDetailDTO response = receiptDetailPIIViewMapper.mapToReceiptDetailDTO(receiptDetailNoPIIView);

    //verify
    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(receiptDetailNoPIIView, response, "debtor", "payer");
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getDebtor(), response.getDebtor());
    TestUtils.checkNotNullFields(response,"payer");
    Mockito.verify(personalDataServiceMock).get(receiptDetailNoPIIView.getDebtorPersonalDataId(),ReceiptPIIDTO.class);
    Mockito.verify(personMapperSpy).mapToDto(receiptPIIDTO.getDebtor());
  }

}
