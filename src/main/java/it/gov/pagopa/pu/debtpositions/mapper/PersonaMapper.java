package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Person;
import it.gov.pagopa.pu.debtpositions.dto.Persona;
import org.springframework.stereotype.Component;

@Component
public class PersonaMapper {

  public Persona mapToPersona(Person person){
    return Persona.builder()
      .codiceIdentificativoUnivoco(person.getFiscalCode())
      .anagrafica(person.getFullName())
      .indirizzo(person.getAddress())
      .civico(person.getCivic())
      .cap(person.getPostalCode())
      .localita(person.getLocation())
      .provincia(person.getProvince())
      .nazione(person.getNation())
      .email(person.getEmail())
      .build();
  }

}
