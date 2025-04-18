package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class PaymentOptionMapper {

  private final InstallmentMapper installmentMapper;
  private final InstallmentPIIMapper installmentPIIMapper;

  private static final Collector<InstallmentNoPII, ?, SortedSet<InstallmentNoPII>> toInstallmentTreeSet = Collectors.toCollection(TreeSet::new);

  public PaymentOptionMapper(InstallmentMapper installmentMapper, InstallmentPIIMapper installmentPIIMapper) {
    this.installmentMapper = installmentMapper;
    this.installmentPIIMapper = installmentPIIMapper;
  }

  public Pair<PaymentOption, Map<InstallmentNoPII, Installment>> mapToModel(PaymentOptionDTO dto) {
    Map<InstallmentNoPII, Installment> installmentMapping = new HashMap<>();

    List<Installment> installments = dto.getInstallments().stream()
      .map(installmentMapper::mapToModel)
      .toList();

    SortedSet<InstallmentNoPII> installmentNoPIIs = installments.stream()
      .map(installment -> {
        Pair<InstallmentNoPII, InstallmentPIIDTO> result = installmentPIIMapper.map(installment);
        InstallmentNoPII installmentNoPII = result.getFirst();
        installmentMapping.put(installmentNoPII, installment);
        return installmentNoPII;
      })
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

    return Pair.of(paymentOption, installmentMapping);
  }

  public PaymentOptionDTO mapToDto(PaymentOption paymentOption) {
    PaymentOptionDTO dto = PaymentOptionDTO.builder()
      .debtPositionId(paymentOption.getDebtPositionId())
      .totalAmountCents(paymentOption.getTotalAmountCents())
      .status(paymentOption.getStatus())
      .description(paymentOption.getDescription())
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.valueOf(paymentOption.getPaymentOptionType().name()))
      .paymentOptionIndex(paymentOption.getPaymentOptionIndex())
      .installments(
        paymentOption.getInstallments().stream()
          .map(installmentMapper::mapToDto)
          .collect(Collectors.toCollection(ArrayList<InstallmentDTO>::new))
      )
      .build();

    setToDtoAutoDbFields(dto, paymentOption);
    return dto;
  }

  public static void setToDtoAutoDbFields(PaymentOptionDTO paymentOptionDTO, PaymentOption paymentOption){
    paymentOptionDTO.setPaymentOptionId(paymentOption.getPaymentOptionId());
    paymentOptionDTO.setCreationDate(localDatetimeToOffsetDateTime(paymentOption.getCreationDate()));
    paymentOptionDTO.setUpdateDate(localDatetimeToOffsetDateTime(paymentOption.getUpdateDate()));
    paymentOptionDTO.setUpdateOperatorExternalId(paymentOption.getUpdateOperatorExternalId());
  }

}
