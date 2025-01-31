package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PaymentOptionRequestMapper.class})
public interface DebtPositionRequestMapper {
  DebtPositionRequestDTO map(DebtPositionDTO debtPositionDTO);
}
