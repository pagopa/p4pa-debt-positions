package it.gov.pagopa.pu.debtpositions.citizen.service;

import it.gov.pagopa.pu.debtpositions.citizen.Constants;
import it.gov.pagopa.pu.debtpositions.citizen.model.PersonalData;
import it.gov.pagopa.pu.debtpositions.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalDataServiceTest {

  @Mock
  private PersonalDataRepository repositoryMock;
  @Mock
  private DataCipherService cipherServiceMock;

  private PersonalDataService service;

  @BeforeEach
  void init() {
    service = new PersonalDataService(repositoryMock, cipherServiceMock);
  }

  @AfterEach
  void verifyNotMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      repositoryMock,
      cipherServiceMock);
  }

  @Test
  void testInsert() {
    // Given
    InstallmentPIIDTO pii = new InstallmentPIIDTO();

    byte[] cipherData = new byte[0];
    Mockito.when(cipherServiceMock.encryptObj(pii)).thenReturn(cipherData);
    PersonalData personalDataInput = PersonalData.builder()
      .type("INSTALLMENT")
      .data(cipherData)
      .build();

    long piiId = -1L;
    PersonalData personalDataOutput = PersonalData.builder()
      .id(piiId)
      .type("INSTALLMENT")
      .data(cipherData)
      .build();

    Mockito.when(repositoryMock.save(personalDataInput)).thenReturn(personalDataOutput);

    // When
    long insert = service.insert(pii, Constants.PERSONAL_DATA_TYPE.INSTALLMENT);

    // Then
    Assertions.assertEquals(piiId, insert);
  }

}
