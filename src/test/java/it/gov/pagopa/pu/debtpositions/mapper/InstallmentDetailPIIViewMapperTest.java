package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentDetailNoPIIView;
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
class InstallmentDetailPIIViewMapperTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  private PersonalDataService personalDataServiceMock;

  @Spy
  private PersonMapper personMapperSpy;

  @InjectMocks
  private InstallmentDetailPIIViewMapper installmentDetailPIIViewMapper;

  @Test
  void givenValidInstallmentNoPIIWhenMapToInstallmentDTOThenReturnInstallmentDTO() {
    InstallmentDetailNoPIIView installmentDetailNoPIIView = podamFactory.manufacturePojo(InstallmentDetailNoPIIView.class);
    InstallmentPIIDTO installmentPIIDTO = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    Mockito.when(personalDataServiceMock.get(installmentDetailNoPIIView.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personalDataServiceMock.get(installmentDetailNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);
    InstallmentDetailDTO response = installmentDetailPIIViewMapper.mapToInstallmentDetailDTO(installmentDetailNoPIIView);

    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(installmentDetailNoPIIView, response, "debtor", "payer");
    TestUtils.reflectionEqualsByName(installmentPIIDTO.getDebtor(), response.getDebtor());
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getPayer(), response.getPayer());
    TestUtils.checkNotNullFields(response);
    Mockito.verify(personalDataServiceMock).get(installmentDetailNoPIIView.getPersonalDataId(), InstallmentPIIDTO.class);
    Mockito.verify(personalDataServiceMock).get(installmentDetailNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class);
    Mockito.verify(personMapperSpy).mapToDto(installmentPIIDTO.getDebtor());
    Mockito.verify(personMapperSpy).mapToDto(receiptPIIDTO.getPayer());
  }

  @Test
  void givenNoPayerAndDebtorWhenMapToInstallmentDTOThenReturnInstallmentDTOWithNoPayerAndDebtor() {
    InstallmentDetailNoPIIView installmentDetailNoPIIView = podamFactory.manufacturePojo(InstallmentDetailNoPIIView.class);
    InstallmentPIIDTO installmentPIIDTO = new InstallmentPIIDTO();
    ReceiptPIIDTO receiptPIIDTO = new ReceiptPIIDTO();
    Mockito.when(personalDataServiceMock.get(installmentDetailNoPIIView.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personalDataServiceMock.get(installmentDetailNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);
    InstallmentDetailDTO response = installmentDetailPIIViewMapper.mapToInstallmentDetailDTO(installmentDetailNoPIIView);

    Assertions.assertNotNull(response);
    TestUtils.reflectionEqualsByName(installmentDetailNoPIIView, response, "debtor", "payer");
    TestUtils.checkNotNullFields(response, "payer", "debtor");
    Mockito.verify(personalDataServiceMock).get(installmentDetailNoPIIView.getPersonalDataId(), InstallmentPIIDTO.class);
    Mockito.verify(personalDataServiceMock).get(installmentDetailNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class);
    Mockito.verifyNoInteractions(personMapperSpy);
  }
}
