package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.springframework.stereotype.Service;

@Service
public class PaymentOptionMapper {

  public PaymentOption mapToModel(PaymentOptionDTO dto) {
    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(dto.getPaymentOptionId());
    paymentOption.setTotalAmountCents(dto.getTotalAmountCents());
    paymentOption.setDueDate(dto.getDueDate());
    paymentOption.setStatus(dto.getStatus());
    paymentOption.setMultiDebtor(dto.getMultiDebtor());
    paymentOption.setDescription(dto.getDescription());
    paymentOption.setPaymentOptionType(PaymentOptionType.valueOf(dto.getPaymentOptionType().name()));
    return paymentOption;
  }

}
