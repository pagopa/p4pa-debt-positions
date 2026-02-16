package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.common.pii.mapper.Base2PIIMapperTest;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.view.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentPaidViewFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class InstallmentPaidPIIMapperTest extends Base2PIIMapperTest<InstallmentPaidViewDTO, InstallmentPaidViewNoPII, InstallmentPIIDTO, ReceiptPIIDTO> {

  private InstallmentPaidPIIMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new InstallmentPaidPIIMapper(personalDataServiceMock);
  }

  @Override
  protected InstallmentPaidPIIMapper getMapper() {
    return mapper;
  }

  @Test
  void givenValidInstallmentPaidViewNoPII_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    ReceiptPIIDTO receiptPIIDTO = podamFactory.manufacturePojo(ReceiptPIIDTO.class);
    InstallmentPIIDTO installmentPIIDTO = podamFactory.manufacturePojo(InstallmentPIIDTO.class);

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getReceiptPersonalDataId(), ReceiptPIIDTO.class)).thenReturn(receiptPIIDTO);
    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);

    //when
    InstallmentPaidViewDTO result = mapper.map(installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    TestUtils.reflectionEqualsByName(installmentPaidViewNoPII, result);
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getDebtor(), result.getDebtor());
    TestUtils.reflectionEqualsByName(receiptPIIDTO.getPayer(), result.getPayer());
    Assertions.assertEquals(installmentPIIDTO.getOriginalRemittanceInformation(), result.getOriginalRemittanceInformation());
    TestUtils.checkNotNullFields(result);
  }
}
