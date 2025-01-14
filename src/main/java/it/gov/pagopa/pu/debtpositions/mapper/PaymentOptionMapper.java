package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {InstallmentMapper.class})
public interface PaymentOptionMapper {

  PaymentOptionDTO map(PaymentOption paymentOption);
}
