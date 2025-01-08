package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

@ExtendWith(MockitoExtension.class)
class InstallmentPIIRepositoryImplTest {

  @Mock(answer = Answers.RETURNS_MOCKS)
  private InstallmentNoPIIRepository installmentNoPIIRepository;
  @Mock
  private PersonalDataService personalDataServiceMock;
  @Mock
  private InstallmentPIIMapper mapperMock;

  private InstallmentPIIRepository installmentPIIRepository;

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
    long insert = installmentPIIRepository.save(installment);

    // Then
    Assertions.assertEquals(insertedId, insert);
    Assertions.assertEquals(insertedId, installment.getInstallmentId());
    Assertions.assertEquals(installment.getNoPII().getInstallmentId(), installment.getInstallmentId());
    Assertions.assertEquals(piiId, p.getFirst().getPersonalDataId());
    Assertions.assertSame(p.getFirst(), installment.getNoPII());

  }
}
