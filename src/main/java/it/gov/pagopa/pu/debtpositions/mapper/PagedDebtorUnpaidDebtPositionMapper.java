package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtorUnpaidDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface PagedDebtorUnpaidDebtPositionMapper {

  @Mapping(target = "content", expression = "java(source != null ? map(source.getContent(), debtPositionTypeOrgMap) : java.util.Collections.emptyList())")
  @Mapping(target = "totalPages", source = "source.totalPages")
  @Mapping(target = "size", source = "source.size")
  @Mapping(target = "number", source = "source.number")
  @Mapping(target = "totalElements", source = "source.totalElements")
  PagedDebtorUnpaidDebtPositionDTO map(Page<DebtPosition> source, @Context Map<Long, DebtPositionTypeOrg> debtPositionTypeOrgMap);

  @Mapping(target = "debtPositionTypeOrgDescription", source = "debtPositionTypeOrgDescription")
  @Mapping(target = "debtPositionId", source = "debtPosition.debtPositionId")
  @Mapping(target = "debtPositionDescription", source = "debtPosition.description")
  @Mapping(target = "status", source = "debtPosition.status")
  @Mapping(target = "debtPositionOrigin", source = "debtPosition.debtPositionOrigin")
  @Mapping(target = "organizationId", source = "debtPosition.organizationId")
  DebtorDebtPositionDTO map(DebtPosition debtPosition, String debtPositionTypeOrgDescription);

  default List<DebtorDebtPositionDTO> map(List<DebtPosition> debtPositions, @Context Map<Long, DebtPositionTypeOrg> debtPositionTypeOrgMap) {
    if (debtPositionTypeOrgMap == null) {
      return debtPositions.stream()
        .map(dp -> map(dp, null))
        .toList();
    }

    return debtPositions.stream()
      .map(dp -> {
        DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgMap.get(dp.getDebtPositionId());
        String description = debtPositionTypeOrg != null ? debtPositionTypeOrg.getDescription() : null;
        return map(dp, description);
      })
      .toList();
  }

}
