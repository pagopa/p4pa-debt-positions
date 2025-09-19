package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class PaymentOptionMapper {

  private final InstallmentPIIMapper installmentMapper;
  private final InstallmentPIIMapper installmentPIIMapper;

  private static final Collector<InstallmentNoPII, ?, SortedSet<InstallmentNoPII>> toInstallmentTreeSet = Collectors.toCollection(TreeSet::new);

  public PaymentOptionMapper(InstallmentPIIMapper installmentMapper, InstallmentPIIMapper installmentPIIMapper) {
    this.installmentMapper = installmentMapper;
    this.installmentPIIMapper = installmentPIIMapper;
  }

  public PaymentOption mapToModel(PaymentOptionDTO dto) {
    SortedSet<InstallmentNoPII> installmentNoPIIs = dto.getInstallments().stream()
      .map(installment -> installmentPIIMapper.map(installment).getFirst())
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
    PaymentOptionDTO dto = PaymentOptionDTO.builder()
      .debtPositionId(paymentOption.getDebtPositionId())
      .totalAmountCents(paymentOption.getTotalAmountCents())
      .status(paymentOption.getStatus())
      .description(paymentOption.getDescription())
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.valueOf(paymentOption.getPaymentOptionType().name()))
      .paymentOptionIndex(paymentOption.getPaymentOptionIndex())
      .installments(
        paymentOption.getInstallments().stream()
          .map(installmentMapper::map)
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
    paymentOptionDTO.setUpdateTraceId(paymentOption.getUpdateTraceId());
  }

  public void updateFromDTO(PaymentOption paymentOption, PaymentOptionDTO dto) {
    List<InstallmentNoPII> installmentNoPIIList = paymentOption.getInstallments().stream().toList();

    IntStream.range(0, Math.min(installmentNoPIIList.size(), dto.getInstallments().size())).forEach(installmentIndex -> {
      // @TODO: use the mapper for only the amounts? how does the PII work?
      InstallmentNoPII installment = installmentNoPIIList.get(installmentIndex);
      InstallmentDTO installmentDTO = dto.getInstallments().get(installmentIndex);
      installment.setBalance(installmentDTO.getBalance());
      installment.setAmountCents(installmentDTO.getAmountCents());
      List<Transfer> transfers = installment.getTransfers().stream().toList();
      IntStream.range(0, Math.min(transfers.size(), installmentDTO.getTransfers().size())).forEach(transferIndex -> {
        TransferDTO transferDTO = installmentDTO.getTransfers().get(transferIndex);
        Transfer transfer = transfers.get(transferIndex);
        transfer.setAmountCents(transferDTO.getAmountCents());
      });
    });

    paymentOption.setTotalAmountCents(dto.getTotalAmountCents());
    paymentOption.setStatus(dto.getStatus());
    paymentOption.setDescription(dto.getDescription());
    paymentOption.setPaymentOptionType(PaymentOptionType.valueOf(dto.getPaymentOptionType().name()));
    paymentOption.setPaymentOptionIndex(dto.getPaymentOptionIndex());
    paymentOption.setInstallments(new TreeSet<>(installmentNoPIIList));
  }

}
