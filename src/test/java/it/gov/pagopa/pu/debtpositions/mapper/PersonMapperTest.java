package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Person;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonMapperTest {

  private final PersonMapper personMapper = new PersonMapper();

  @Test
  void givenValidPersonDTO_WhenMapToModel_ThenReturnPerson() {
    Person personExpected = buildPerson();
    PersonDTO personDTO = buildPersonDTO();

    Person result = personMapper.mapToModel(personDTO);

    assertEquals(personExpected, result);
    checkNotNullFields(result);
  }
}
