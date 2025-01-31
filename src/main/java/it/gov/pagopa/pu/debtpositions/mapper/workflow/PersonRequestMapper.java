package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PersonRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonRequestMapper {

  PersonRequestDTO map(PersonDTO personDTO);
}
