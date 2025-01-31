package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.InstallmentRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {TransferRequestMapper.class, PersonRequestMapper.class})
public interface InstallmentRequestMapper {

  InstallmentRequestDTO map(InstallmentDTO installmentDTO);
}
