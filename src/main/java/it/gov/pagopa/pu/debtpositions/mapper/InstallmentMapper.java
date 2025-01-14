package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {TransferMapper.class, PersonMapper.class})
public interface InstallmentMapper {

  InstallmentDTO map(InstallmentNoPII installment);
}
