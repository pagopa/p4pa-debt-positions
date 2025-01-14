package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PaymentOptionMapper.class})
public interface DebtPositionMapper {

  DebtPositionDTO map(DebtPosition debtPosition);
}

