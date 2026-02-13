package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class PaymentOptionMapper {

  private final InstallmentPIIMapper installmentMapper;

  private static final Collector<InstallmentNoPII, ?, SortedSet<InstallmentNoPII>> toInstallmentTreeSet = Collectors.toCollection(TreeSet::new);

  public PaymentOptionMapper(InstallmentPIIMapper installmentMapper) {
    this.installmentMapper = installmentMapper;
  }

  public PaymentOption mapToModel(PaymentOptionDTO dto) {
    SortedSet<InstallmentNoPII> installmentNoPIIs = dto.getInstallments().stream()
      .map(installment -> installmentMapper.map(installment).getFirst())
      .collect(toInstallmentTreeSet);

    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(dto.getPaymentOptionId());
    paymentOption.setDebtPositionId(dto.getDebtPositionId());
    paymentOption.setTotalAmountCents(dto.getTotalAmountCents());
    paymentOption.setStatus(dto.getStatus());
    paymentOption.setDescription(dto.getDescription());
    paymentOption.setPaymentOptionType(PaymentOptionType.valueOf(dto.getPaymentOptionType().name()));
    paymentOption.setPaymentOptionIndex(dto.getPaymentOptionIndex());
    paymentOption.setInstallments(installmentNoPIIs);

    return paymentOption;
  }

  public PaymentOptionDTO mapToDto(PaymentOption paymentOption) {
    return mapToDto(paymentOption, null);
  }

  /**
   * In order to optimize PII retrieve, it could be useful to resolve them first and provide them.<BR/>
   * The list should be modifiable in order to handle some useCases which need to change its content
   * */
  protected PaymentOptionDTO mapToDto(PaymentOption paymentOption, ArrayList<InstallmentDTO> installmentDTOS) {
    if(installmentDTOS == null) {
      installmentDTOS = new ArrayList<>(installmentMapper.mapAll(new ArrayList<>(paymentOption.getInstallments())));
    }

    PaymentOptionDTO dto = PaymentOptionDTO.builder()
      .debtPositionId(paymentOption.getDebtPositionId())
      .totalAmountCents(paymentOption.getTotalAmountCents())
      .status(paymentOption.getStatus())
      .description(paymentOption.getDescription())
      .paymentOptionType(paymentOption.getPaymentOptionType())
      .paymentOptionIndex(paymentOption.getPaymentOptionIndex())
      .installments(installmentDTOS)
      .build();

    setToDtoAutoDbFields(dto, paymentOption);
    return dto;
  }

  public static void setToDtoAutoDbFields(PaymentOptionDTO paymentOptionDTO, PaymentOption paymentOption){
    paymentOptionDTO.setPaymentOptionId(paymentOption.getPaymentOptionId());
    paymentOptionDTO.setCreationDate(localDatetimeToOffsetDateTime(paymentOption.getCreationDate()));
    paymentOptionDTO.setUpdateDate(localDatetimeToOffsetDateTime(paymentOption.getUpdateDate()));
    paymentOptionDTO.setUpdateOperatorExternalId(paymentOption.getUpdateOperatorExternalId());
    paymentOptionDTO.setUpdateTraceId(paymentOption.getUpdateTraceId());
  }

}
