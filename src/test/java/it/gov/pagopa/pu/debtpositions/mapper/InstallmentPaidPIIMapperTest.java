package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentPaidViewFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class InstallmentPaidPIIMapperTest {

  private InstallmentPaidPIIMapper installmentPaidPIIMapper;

  @Mock
  private PersonalDataService personalDataServiceMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    installmentPaidPIIMapper = new InstallmentPaidPIIMapper(personalDataServiceMock);
  }

  @Test
  void givenValidInstallmentPaidViewNoPII_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getReceiptPersonalDataId(), ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.map(installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    TestUtils.reflectionEqualsByName(installmentPaidViewNoPII, result);
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getDebtor(), result.getDebtor());
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getPayer(), result.getPayer());
    TestUtils.checkNotNullFields(result);
  }

}
