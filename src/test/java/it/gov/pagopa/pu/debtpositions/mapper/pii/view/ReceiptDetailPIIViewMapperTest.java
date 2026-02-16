package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.common.pii.mapper.BasePIIMapperTest;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReceiptDetailPIIViewMapperTest extends BasePIIMapperTest<ReceiptDetailDTO, ReceiptDetailNoPIIView, InstallmentPIIDTO> {

  @InjectMocks
  private ReceiptDetailPIIViewMapper mapper;

  @Override
  protected ReceiptDetailPIIViewMapper getMapper() {
    return mapper;
  }

  @Test
  void givenValidReceiptNoPIIWhenMapToReceiptDTOThenReturnReceiptDTO() {
    //given
    ReceiptDetailNoPIIView receiptDetailNoPIIView = podamFactory.manufacturePojo(ReceiptDetailNoPIIView.class);
    InstallmentPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    Mockito.when(personalDataServiceMock.get(receiptDetailNoPIIView.getDebtorPersonalDataId(),InstallmentPIIDTO.class)).thenReturn(receiptPIIDTO);
    //when
    ReceiptDetailDTO response = mapper.map(receiptDetailNoPIIView);

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
    ReceiptDetailDTO response = mapper.map(receiptDetailNoPIIView);

    //verify
    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(receiptDetailNoPIIView, response);
    TestUtils.checkNotNullFields(response, "debtor");
    Assertions.assertEquals(installmentPIIDTO.getOriginalRemittanceInformation(),response.getOriginalRemittanceInformation());
  }
}
