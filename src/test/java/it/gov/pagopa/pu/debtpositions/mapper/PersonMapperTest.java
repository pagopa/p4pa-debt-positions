package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Person;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PersonMapperTest {

  private PersonMapper personMapper;

  @Test
  void givenValidPersonDTO_WhenMapToModel_ThenReturnPerson() {
    Person personExpected = buildPerson();
    PersonDTO personDTO = buildPersonDTO();

    Person result = personMapper.mapToModel(personDTO);

    assertEquals(personExpected, result);
    checkNotNullFields(result);
  }
}
