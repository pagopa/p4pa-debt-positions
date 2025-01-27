package it.gov.pagopa.pu.debtpositions.citizen.service;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.model.PersonalData;
import it.gov.pagopa.pu.debtpositions.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PersonalDataServiceTest {

  @Mock
  private PersonalDataRepository repositoryMock;
  @Mock
  private DataCipherService cipherServiceMock;

  private PersonalDataService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

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
    long insert = service.insert(pii, PersonalDataType.INSTALLMENT);

    // Then
    Assertions.assertEquals(piiId, insert);
  }

  //region get

  @Test
  void givenValidPersonalDataIdWhenGetThenOk(){
    //given
    InstallmentPIIDTO expected = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    Mockito.when(repositoryMock.findById(1L)).thenReturn(
      Optional.of(PersonalData.builder().id(1L).data(new byte[0]).type(PersonalDataType.INSTALLMENT.name()).build()));
    Mockito.when(cipherServiceMock.decryptObj(new byte[0], InstallmentPIIDTO.class)).thenReturn(expected);
    //when
    InstallmentPIIDTO installmentPIIDTO = service.get(1L, InstallmentPIIDTO.class);
    //then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(expected, installmentPIIDTO,true, null, true));
    Mockito.verify(repositoryMock, Mockito.times(1)).findById(1L);
    Mockito.verify(cipherServiceMock, Mockito.times(1)).decryptObj(new byte[0], InstallmentPIIDTO.class);
  }

  @Test
  void givenNotFoundPersonalDataIdWhenGetThenException(){
    //given
    Mockito.when(repositoryMock.findById(1L)).thenReturn(null);
    //when
    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> service.get(1L, InstallmentPIIDTO.class));
    //then
    Assertions.assertEquals("installment pii not found for id 1", notFoundException.getMessage());
    Mockito.verify(repositoryMock, Mockito.times(1)).findById(1L);
    Mockito.verifyNoInteractions(cipherServiceMock);
  }

  //endregion

}
