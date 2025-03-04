package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.Persona;

public class PersonaFaker {

  public static Persona buildPersona(){
    return Persona.builder()
      .codiceIdentificativoUnivoco("uniqueIdentifierCode")
      .anagrafica("fullName")
      .indirizzo("address")
      .civico("civic")
      .cap("postalCode")
      .localita("location")
      .provincia("province")
      .nazione("nation")
      .email("email@test.it")
      .build();
  }
}
