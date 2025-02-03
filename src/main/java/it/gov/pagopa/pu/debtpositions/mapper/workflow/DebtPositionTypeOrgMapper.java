package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DebtPositionTypeOrgMapper {
  DebtPositionTypeOrgDTO map(DebtPositionTypeOrg debtPositionTypeOrgDTO);
}
