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
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class InstallmentPaidPIIMapperTest {

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
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

  @Test
  void givenValidInstallmentPaidViewDTO_whenExtractNoPiiEntity_thenReturnInstallmentPaidViewNoPII(){
    //given
    InstallmentPaidViewDTO installmentPaidViewDTO = podamFactory.manufacturePojo(InstallmentPaidViewDTO.class);

    //when
    InstallmentPaidViewNoPII result = installmentPaidPIIMapper.extractNoPiiEntity(installmentPaidViewDTO);

    //then
    TestUtils.checkNotNullFields(result,"personalDataId");
    TestUtils.reflectionEqualsByName(installmentPaidViewDTO, result);
  }

  @Test
  void givenValidInstallmentPIIDTO_whenExtractPIIDTO_thenReturnInstallmentPIIDTO(){
    //given
    InstallmentPaidViewDTO installmentPaidViewDTO = podamFactory.manufacturePojo(InstallmentPaidViewDTO.class);

    //when
    InstallmentPIIDTO result = installmentPaidPIIMapper.extractPiiDto(installmentPaidViewDTO);

    //then
    TestUtils.checkNotNullFields(result);
    TestUtils.reflectionEqualsByName(installmentPaidViewDTO, result);
  }

}
