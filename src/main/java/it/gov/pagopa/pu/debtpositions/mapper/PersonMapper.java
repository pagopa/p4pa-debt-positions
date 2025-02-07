package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Person;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import org.springframework.stereotype.Service;

@Service
public class PersonMapper {

  public Person mapToModel(PersonDTO dto) {
    Person person = new Person();
    person.setEntityType(PersonEntityType.valueOf(dto.getEntityType().getValue()));
    person.setFiscalCode(dto.getFiscalCode());
    person.setFullName(dto.getFullName());
    person.setAddress(dto.getAddress());
    person.setCivic(dto.getCivic());
    person.setPostalCode(dto.getPostalCode());
    person.setLocation(dto.getLocation());
    person.setProvince(dto.getProvince());
    person.setNation(dto.getNation());
    person.setEmail(dto.getEmail());
    return person;
  }

  public PersonDTO mapToDto(Person person) {
    return PersonDTO.builder()
      .entityType(PersonDTO.EntityTypeEnum.fromValue(person.getEntityType().name()))
      .fiscalCode(person.getFiscalCode())
      .fullName(person.getFullName())
      .address(person.getAddress())
      .civic(person.getCivic())
      .postalCode(person.getPostalCode())
      .location(person.getLocation())
      .province(person.getProvince())
      .nation(person.getNation())
      .email(person.getEmail())
      .build();
  }
}
