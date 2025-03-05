package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentPaidViewFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class InstallmentPaidPIIMapperTest {

  private InstallmentPaidPIIMapper installmentPaidPIIMapper;

  @Mock
  private PersonalDataService personalDataServiceMock;

  @BeforeEach
  void setUp() {
    installmentPaidPIIMapper = new InstallmentPaidPIIMapper(personalDataServiceMock);
  }

  @Test
  void givenValidInstallmentPaidViewNoPII_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    InstallmentPIIDTO installmentPIIDTO = InstallmentFaker.buildInstallmentPIIDTO();

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.map(installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    TestUtils.reflectionEqualsByName(installmentPaidViewNoPII, result);
    TestUtils.reflectionEqualsByName(installmentPIIDTO.getDebtor(), result.getDebtor());
    TestUtils.checkNotNullFields(result);
  }
}
