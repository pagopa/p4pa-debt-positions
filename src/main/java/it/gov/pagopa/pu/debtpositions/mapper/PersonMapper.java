package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Person;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import org.springframework.stereotype.Service;

@Service
public class PersonMapper {

  public Person mapToModel(PersonDTO dto) {
    Person person = new Person();
    person.setEntityType(dto.getUniqueIdentifierType());
    person.setFiscalCode(dto.getUniqueIdentifierCode());
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

}
