package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentDetailNoPIIView;
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
class InstallmentDetailPIIViewMapperTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private PersonalDataService personalDataServiceMock;

  @InjectMocks
  private InstallmentDetailPIIViewMapper installmentDetailPIIViewMapper;

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      personalDataServiceMock);
  }

  @Test
  void givenValidInstallmentNoPIIWhenMapToInstallmentDTOThenReturnInstallmentDTO() {
    InstallmentDetailNoPIIView installmentDetailNoPIIView = podamFactory.manufacturePojo(InstallmentDetailNoPIIView.class);
    InstallmentPIIDTO installmentPIIDTO = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    Mockito.when(personalDataServiceMock.get(installmentDetailNoPIIView.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personalDataServiceMock.get(installmentDetailNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);
    InstallmentDetailDTO response = installmentDetailPIIViewMapper.mapToInstallmentDetailDTO(installmentDetailNoPIIView);

    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(installmentDetailNoPIIView, response);
    TestUtils.checkNotNullFields(response);
    Assertions.assertEquals(installmentPIIDTO.getOriginalRemittanceInformation(),response.getOriginalRemittanceInformation());
  }

  @Test
  void givenNoPayerWhenMapToInstallmentDTOThenReturnInstallmentDTOWithNoPayer() {
    InstallmentDetailNoPIIView installmentDetailNoPIIView = podamFactory.manufacturePojo(InstallmentDetailNoPIIView.class);
    InstallmentPIIDTO installmentPIIDTO = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);

    receiptPIIDTO.setPayer(null);

    Mockito.when(personalDataServiceMock.get(installmentDetailNoPIIView.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personalDataServiceMock.get(installmentDetailNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);

    InstallmentDetailDTO response = installmentDetailPIIViewMapper.mapToInstallmentDetailDTO(installmentDetailNoPIIView);

    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(installmentDetailNoPIIView, response);
    TestUtils.checkNotNullFields(response, "payer");
    Assertions.assertEquals(installmentPIIDTO.getOriginalRemittanceInformation(),response.getOriginalRemittanceInformation());
  }
}
