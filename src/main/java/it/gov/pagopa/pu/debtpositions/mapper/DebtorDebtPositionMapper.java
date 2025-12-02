package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DebtorDebtPositionMapper {

  @Mapping(target = "debtPositionTypeOrgDescription", source = "debtPositionTypeOrg.description")
  @Mapping(target = "debtPositionId", source = "debtPosition.debtPositionId")
  @Mapping(target = "debtPositionDescription", source = "debtPosition.description")
  @Mapping(target = "status", source = "debtPosition.status")
  @Mapping(target = "debtPositionOrigin", source = "debtPosition.debtPositionOrigin")
  @Mapping(target = "organizationId", source = "debtPosition.organizationId")
  DebtorDebtPositionDTO map(DebtPosition debtPosition, DebtPositionTypeOrg debtPositionTypeOrg);
}
