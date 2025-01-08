package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.springframework.stereotype.Service;

@Service
public class PaymentOptionMapper {

  private final InstallmentMapper installmentMapper;

  public PaymentOptionMapper(InstallmentMapper installmentMapper) {
    this.installmentMapper = installmentMapper;
  }

  public PaymentOptionDTO mapToDto(PaymentOption paymentOption) {
    return PaymentOptionDTO.builder()
      .paymentOptionId(paymentOption.getPaymentOptionId())
      .totalAmountCents(paymentOption.getTotalAmountCents())
      .dueDate(paymentOption.getDueDate())
      .status(paymentOption.getStatus())
      .multiDebtor(paymentOption.isMultiDebtor())
      .description(paymentOption.getDescription())
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.valueOf(paymentOption.getPaymentOptionType().name()))
      .installments(paymentOption.getInstallments().stream()
        .map(installmentMapper::mapToDto)
        .toList())
      .build();
  }

  public PaymentOption mapToModel(PaymentOptionDTO dto) {
    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(dto.getPaymentOptionId());
    paymentOption.setTotalAmountCents(dto.getTotalAmountCents());
    paymentOption.setDueDate(dto.getDueDate());
    paymentOption.setStatus(dto.getStatus());
    paymentOption.setMultiDebtor(dto.getMultiDebtor());
    paymentOption.setDescription(dto.getDescription());
    paymentOption.setPaymentOptionType(PaymentOptionType.valueOf(dto.getPaymentOptionType().name()));
    paymentOption.setInstallments(dto.getInstallments().stream()
      .map(installmentMapper::mapToModel)
      .toList());
    return paymentOption;
  }

}
