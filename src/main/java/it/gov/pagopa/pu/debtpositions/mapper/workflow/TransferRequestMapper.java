package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.TransferRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferRequestMapper {

  TransferRequestDTO map(TransferDTO transferDTO);
}

