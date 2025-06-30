package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonEntityType;

public class PersonFaker {

  public static PersonDTO buildPerson(){
    return PersonDTO.builder()
      .entityType(PersonEntityType.F)
      .fiscalCode("AAAAAA00B00C000D")
      .fullName("fullName")
      .address("address")
      .civic("civic")
      .postalCode("postalCode")
      .location("location")
      .province("province")
      .nation("nation")
      .email("email@test.it")
      .build();
  }

}
