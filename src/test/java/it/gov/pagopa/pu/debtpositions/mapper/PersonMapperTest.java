package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Person;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPerson;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPersonDTO;

@ExtendWith(MockitoExtension.class)
class PersonMapperTest {

  private PersonMapper personMapper;

  @BeforeEach
  void setUp() {
    personMapper = new PersonMapper();
  }

  @Test
  void givenValidPersonDTO_WhenMapToModel_ThenReturnPerson() {
    Person personExpected = buildPerson();
    PersonDTO personDTO = buildPersonDTO();

    Person result = personMapper.mapToModel(personDTO);

    reflectionEqualsByName(personExpected, result);
    checkNotNullFields(result);
  }

  @Test
  void givenValidPerson_WhenMapToDto_ThenReturnPersonDTO() {
    Person person = buildPerson();
    PersonDTO personDTOExpected = buildPersonDTO();

    PersonDTO result = personMapper.mapToDto(person);

    reflectionEqualsByName(personDTOExpected, result);
    checkNotNullFields(result);
  }
}
