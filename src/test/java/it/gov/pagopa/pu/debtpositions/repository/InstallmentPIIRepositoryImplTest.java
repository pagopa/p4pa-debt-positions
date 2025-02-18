package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.Person;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
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
import java.util.Optional;

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

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      installmentNoPIIRepository,
      personalDataServiceMock,
      mapperMock);
  }

  @Test
  void givenNewInstallmentWhenSaveThenOk() {
    // Given
    Installment installment = new Installment();
    InstallmentNoPII newNoPII = new InstallmentNoPII();
    Pair<InstallmentNoPII, InstallmentPIIDTO> p = Pair.of(newNoPII, new InstallmentPIIDTO());
    Mockito.when(mapperMock.map(installment)).thenReturn(p);

    long piiId = -1L;
    Mockito.when(personalDataServiceMock.insert(p.getSecond(), PersonalDataType.INSTALLMENT)).thenReturn(piiId);

    long insertedId = -2L;
    Mockito.when(installmentNoPIIRepository.save(p.getFirst())).thenAnswer(i ->
    {
      newNoPII.setInstallmentId(insertedId);
      return newNoPII;
    });

    // When
    Installment insert = installmentPIIRepository.save(installment);

    // Then
    Assertions.assertSame(installment, insert);
    Assertions.assertEquals(insertedId, insert.getInstallmentId());
    Assertions.assertEquals(insertedId, installment.getInstallmentId());
    Assertions.assertEquals(installment.getNoPII().getInstallmentId(), installment.getInstallmentId());
    Assertions.assertEquals(piiId, p.getFirst().getPersonalDataId());
    Assertions.assertSame(p.getFirst(), installment.getNoPII());

  }

  @Test
  void givenAlreadyExistentInstallmentAndNoNewPIIWhenSaveThenSkipPIISave() {
    // Given
    long piiId = -1L;
    Installment installment = new Installment();
    InstallmentNoPII newNoPII = new InstallmentNoPII();
    newNoPII.setPersonalDataId(piiId);
    Pair<InstallmentNoPII, InstallmentPIIDTO> p = Pair.of(newNoPII, new InstallmentPIIDTO());
    Mockito.when(mapperMock.map(installment)).thenReturn(p);

    Mockito.when(personalDataServiceMock.get(-1, InstallmentPIIDTO.class)).thenReturn(new InstallmentPIIDTO());

    long insertedId = -2L;
    Mockito.when(installmentNoPIIRepository.save(p.getFirst())).thenAnswer(i ->
    {
      newNoPII.setInstallmentId(insertedId);
      return newNoPII;
    });

    // When
    Installment insert = installmentPIIRepository.save(installment);

    // Then
    Assertions.assertSame(installment, insert);
    Assertions.assertEquals(insertedId, insert.getInstallmentId());
    Assertions.assertEquals(insertedId, installment.getInstallmentId());
    Assertions.assertEquals(installment.getNoPII().getInstallmentId(), installment.getInstallmentId());
    Assertions.assertEquals(piiId, p.getFirst().getPersonalDataId());
    Assertions.assertSame(p.getFirst(), installment.getNoPII());

  }

  @Test
  void givenAlreadyExistentInstallmentAndNewPIIWhenSaveThenSkipPIISave() {
    // Given
    long oldPiiId = -1L;
    Installment installment = new Installment();
    InstallmentNoPII newNoPII = new InstallmentNoPII();
    newNoPII.setPersonalDataId(oldPiiId);
    InstallmentPIIDTO newPii = new InstallmentPIIDTO();
    newPii.setDebtor(new Person());
    Pair<InstallmentNoPII, InstallmentPIIDTO> p = Pair.of(newNoPII, newPii);
    Mockito.when(mapperMock.map(installment)).thenReturn(p);

    Mockito.when(personalDataServiceMock.get(-1, InstallmentPIIDTO.class)).thenReturn(new InstallmentPIIDTO());

    long newPiiId = -3L;
    Mockito.when(personalDataServiceMock.insert(p.getSecond(), PersonalDataType.INSTALLMENT)).thenReturn(newPiiId);

    long insertedId = -2L;
    Mockito.when(installmentNoPIIRepository.save(p.getFirst())).thenAnswer(i ->
    {
      newNoPII.setInstallmentId(insertedId);
      return newNoPII;
    });

    // When
    Installment insert = installmentPIIRepository.save(installment);

    // Then
    Assertions.assertSame(installment, insert);
    Assertions.assertEquals(insertedId, insert.getInstallmentId());
    Assertions.assertEquals(insertedId, installment.getInstallmentId());
    Assertions.assertEquals(installment.getNoPII().getInstallmentId(), installment.getInstallmentId());
    Assertions.assertEquals(newPiiId, p.getFirst().getPersonalDataId());
    Assertions.assertSame(p.getFirst(), installment.getNoPII());

    Mockito.verify(personalDataServiceMock)
      .delete(oldPiiId);

  }

  // Not fetched, means that the Mapped NoPII entity doesn't know about personalDataId
  @Test
  void givenAlreadyExistentInstallmentNotFetchedAndNewPIIWhenSaveThenSkipPIISave() {
    // Given
    long oldPiiId = -1L;
    Installment installment = new Installment();
    InstallmentNoPII newNoPII = new InstallmentNoPII();
    newNoPII.setInstallmentId(0L);
    InstallmentPIIDTO newPii = new InstallmentPIIDTO();
    newPii.setDebtor(new Person());
    Pair<InstallmentNoPII, InstallmentPIIDTO> p = Pair.of(newNoPII, newPii);
    Mockito.when(mapperMock.map(installment)).thenReturn(p);

    InstallmentNoPII oldNoPII = new InstallmentNoPII();
    oldNoPII.setPersonalDataId(oldPiiId);

    Mockito.when(installmentNoPIIRepository.findById(newNoPII.getInstallmentId()))
        .thenReturn(Optional.of(oldNoPII));

    Mockito.when(personalDataServiceMock.get(-1, InstallmentPIIDTO.class)).thenReturn(new InstallmentPIIDTO());

    long newPiiId = -3L;
    Mockito.when(personalDataServiceMock.insert(p.getSecond(), PersonalDataType.INSTALLMENT)).thenReturn(newPiiId);

    long insertedId = -2L;
    Mockito.when(installmentNoPIIRepository.save(p.getFirst())).thenAnswer(i ->
    {
      newNoPII.setInstallmentId(insertedId);
      return newNoPII;
    });

    // When
    Installment insert = installmentPIIRepository.save(installment);

    // Then
    Assertions.assertSame(installment, insert);
    Assertions.assertEquals(insertedId, insert.getInstallmentId());
    Assertions.assertEquals(insertedId, installment.getInstallmentId());
    Assertions.assertEquals(installment.getNoPII().getInstallmentId(), installment.getInstallmentId());
    Assertions.assertEquals(newPiiId, p.getFirst().getPersonalDataId());
    Assertions.assertSame(p.getFirst(), installment.getNoPII());

    Mockito.verify(personalDataServiceMock)
      .delete(oldPiiId);

  }

  @Test
  void givenValidOrganizationAndNavWhenGetByOrganizationIdAndNavThenOk() {
    // Given
    List<InstallmentNoPII> installmentDTOList = podamFactory.manufacturePojo(List.class, InstallmentNoPII.class);
    Mockito.when(installmentNoPIIRepository.getByOrganizationIdAndNav(1L, "NAV", null)).thenReturn(installmentDTOList);
    installmentDTOList.forEach(installmentNoPII -> Mockito.when(mapperMock.map(installmentNoPII)).thenReturn(Installment.builder().build()));

    // When
    List<Installment> result = installmentPIIRepository.getByOrganizationIdAndNav(1L, "NAV");

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertEquals(installmentDTOList.size(), result.size());
    installmentDTOList.forEach(installmentNoPII -> Mockito.verify(mapperMock, Mockito.times(1)).map(installmentNoPII));
  }

}
