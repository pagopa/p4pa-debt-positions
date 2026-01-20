package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptDetailPIIViewMapperTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private PersonalDataService personalDataServiceMock;

  @InjectMocks
  private ReceiptDetailPIIViewMapper receiptDetailPIIViewMapper;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
            personalDataServiceMock
    );
  }

  @Test
  void givenValidReceiptNoPIIWhenMapToReceiptDTOThenReturnReceiptDTO() {
    //given
    ReceiptDetailNoPIIView receiptDetailNoPIIView = podamFactory.manufacturePojo(ReceiptDetailNoPIIView.class);
    InstallmentPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    Mockito.when(personalDataServiceMock.get(receiptDetailNoPIIView.getDebtorPersonalDataId(),InstallmentPIIDTO.class)).thenReturn(receiptPIIDTO);
    //when
    ReceiptDetailDTO response = receiptDetailPIIViewMapper.mapToReceiptDetailDTO(receiptDetailNoPIIView);

    //verify
    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(receiptDetailNoPIIView, response);
    TestUtils.checkNotNullFields(response);
  }

  @Test
  void givenNoDebtorWhenMapToReceiptDTOThenReturnReceiptDTOWithNoDebtor() {
    //given
    ReceiptDetailNoPIIView receiptDetailNoPIIView = podamFactory.manufacturePojo(ReceiptDetailNoPIIView.class);
    InstallmentPIIDTO installmentPIIDTO = new InstallmentPIIDTO();
    installmentPIIDTO.setOriginalRemittanceInformation("originalRemittanceInformation");
    Mockito.when(personalDataServiceMock.get(receiptDetailNoPIIView.getDebtorPersonalDataId(),InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    //when
    ReceiptDetailDTO response = receiptDetailPIIViewMapper.mapToReceiptDetailDTO(receiptDetailNoPIIView);

    //verify
    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(receiptDetailNoPIIView, response);
    TestUtils.checkNotNullFields(response, "debtor");
    Assertions.assertEquals(installmentPIIDTO.getOriginalRemittanceInformation(),response.getOriginalRemittanceInformation());
  }
}
