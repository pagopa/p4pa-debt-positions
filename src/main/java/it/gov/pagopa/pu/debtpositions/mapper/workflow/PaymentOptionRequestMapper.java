package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentOptionRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {InstallmentRequestMapper.class})
public interface PaymentOptionRequestMapper {

  PaymentOptionRequestDTO map(PaymentOptionDTO paymentOptionDTO);
}
