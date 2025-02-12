package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class InstallmentPIIRepositoryImplTest {

  @Mock(answer = Answers.RETURNS_MOCKS)
  private InstallmentNoPIIRepository installmentNoPIIRepository;
  @Mock
  private PersonalDataService personalDataServiceMock;
  @Mock
  private InstallmentPIIMapper mapperMock;

  private InstallmentPIIRepository installmentPIIRepository;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    installmentPIIRepository = new InstallmentPIIRepositoryImpl(mapperMock, personalDataServiceMock, installmentNoPIIRepository);
  }

  @Test
  void testInsert(){
    // Given
    Installment installment = new Installment();
    InstallmentNoPII installmentNoPII = new InstallmentNoPII();
    installmentNoPII.setInstallmentId(-2L);
    Pair<InstallmentNoPII, InstallmentPIIDTO> p = Pair.of(installmentNoPII, new InstallmentPIIDTO());
    Mockito.when(mapperMock.map(installment)).thenReturn(p);

    long piiId = -1L;
    Mockito.when(personalDataServiceMock.insert(p.getSecond(), PersonalDataType.INSTALLMENT)).thenReturn(piiId);

    long insertedId = -2L;
    Mockito.when(installmentNoPIIRepository.save(p.getFirst())).thenReturn(installmentNoPII);

    // When
    InstallmentNoPII insert = installmentPIIRepository.save(installment);

    // Then
    Assertions.assertEquals(insertedId, insert.getInstallmentId());
    Assertions.assertEquals(insertedId, installment.getInstallmentId());
    Assertions.assertEquals(installment.getNoPII().getInstallmentId(), installment.getInstallmentId());
    Assertions.assertEquals(piiId, p.getFirst().getPersonalDataId());
    Assertions.assertSame(p.getFirst(), installment.getNoPII());

  }

  @Test
  void givenValidOrganizationAndNavWhenGetByOrganizationIdAndNavThenOk() {
    // Given
    List<InstallmentNoPII> installmentDTOList = podamFactory.manufacturePojo(List.class, InstallmentNoPII.class);
    Mockito.when(installmentNoPIIRepository.getByOrganizationIdAndNav(1L, "NAV")).thenReturn(installmentDTOList);
    installmentDTOList.forEach(installmentNoPII -> Mockito.when(mapperMock.map(installmentNoPII)).thenReturn(Installment.builder().build()));

    // When
    List<Installment> result = installmentPIIRepository.getByOrganizationIdAndNav(1L, "NAV");

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertEquals(installmentDTOList.size(), result.size());
    installmentDTOList.forEach(installmentNoPII -> Mockito.verify(mapperMock, Mockito.times(1)).map(installmentNoPII));
  }

}
