package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;

public class PersonFaker {

  public static PersonDTO buildPersonDTO(){
    return PersonDTO.builder()
      .uniqueIdentifierType("F")
      .uniqueIdentifierCode("uniqueIdentifierCode")
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
