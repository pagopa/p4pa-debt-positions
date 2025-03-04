package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Person;
import it.gov.pagopa.pu.debtpositions.dto.Persona;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonaMapperTest {

  private PersonaMapper personaMapper;

  @BeforeEach
  void setUp() {
    personaMapper = new PersonaMapper();
  }

  @Test
  void givenValidPerson_WhenMapToPersona_ThenReturnPersona() {
    //given
    Person person = PersonFaker.buildPerson();
    //when
    Persona result = personaMapper.mapToPersona(person);
    //then
    assertEquals(person.getFullName(), result.getAnagrafica());
    assertEquals(person.getFiscalCode(), result.getCodiceIdentificativoUnivoco());
    assertEquals(person.getNation(), result.getNazione());
    assertEquals(person.getLocation(), result.getLocalita());
    assertEquals(person.getProvince(), result.getProvincia());
    assertEquals(person.getPostalCode(), result.getCap());
    assertEquals(person.getAddress(), result.getIndirizzo());
    assertEquals(person.getCivic(), result.getCivico());
    assertEquals(person.getEmail(), result.getEmail());

    TestUtils.checkNotNullFields(result);
  }
}
