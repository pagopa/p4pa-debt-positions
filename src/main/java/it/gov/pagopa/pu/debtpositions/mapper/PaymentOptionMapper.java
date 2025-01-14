package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;

@Service
public class PaymentOptionMapper {

  private final InstallmentMapper installmentMapper;
  private final InstallmentPIIMapper installmentPIIMapper;

  private static final Supplier<TreeSet<InstallmentNoPII>> TREE_SET_SUPPLIER = TreeSet::new;

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
      .collect(
        TREE_SET_SUPPLIER,
        SortedSet::add,
        SortedSet::addAll);

    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(dto.getPaymentOptionId());
    paymentOption.setDebtPositionId(dto.getDebtPositionId());
    paymentOption.setTotalAmountCents(dto.getTotalAmountCents());
    paymentOption.setStatus(dto.getStatus());
    paymentOption.setMultiDebtor(dto.getMultiDebtor());
    paymentOption.setDueDate(dto.getDueDate());
    paymentOption.setDescription(dto.getDescription());
    paymentOption.setPaymentOptionType(PaymentOptionType.valueOf(dto.getPaymentOptionType().name()));
    paymentOption.setInstallments(installmentNoPIIs);

    return Pair.of(paymentOption, installmentMapping);
  }
}
