package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.BasePaymentOption;
import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DebtorDebtPositionMapper {

  @Mapping(target = "debtPositionTypeOrgDescription", source = "debtPositionTypeOrg.description")
  @Mapping(target = "debtPositionId", source = "debtPosition.debtPositionId")
  @Mapping(target = "debtPositionDescription", source = "debtPosition.description")
  @Mapping(target = "iupdOrg", source = "debtPosition.iupdOrg")
  @Mapping(target = "status", source = "debtPosition.status")
  @Mapping(target = "debtPositionOrigin", source = "debtPosition.debtPositionOrigin")
  @Mapping(target = "organizationId", source = "debtPosition.organizationId")
  @Mapping(target = "paymentOptions", source = "debtPosition.paymentOptions", qualifiedByName = "mapPaymentOption")
  DebtorDebtPositionDTO map(DebtPosition debtPosition, DebtPositionTypeOrg debtPositionTypeOrg);

  @Named("mapPaymentOption")
  default List<BasePaymentOption> paymentOptionSortedSetToBasePaymentOptionList(SortedSet<PaymentOption> sortedSet) {
    if (sortedSet == null) {
      return Collections.emptyList();
    }
    List<BasePaymentOption> list = new ArrayList<>(sortedSet.size());

    for (PaymentOption paymentOption : sortedSet) {

      SortedSet<InstallmentNoPII> installments = paymentOption.getInstallments();
      if (installments != null) {
        SortedSet<InstallmentNoPII> sortedInstallments = installments.stream()
          .filter(i -> InstallmentUtils.UNPAID_OR_PAID_INSTALLMENT_STATUSES.contains(i.getStatus()))
          .collect(Collectors.toCollection(() -> new TreeSet<>(
            Comparator.comparing(InstallmentNoPII::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
              .thenComparing(InstallmentNoPII::getInstallmentId)
          )));

        paymentOption.setInstallments(sortedInstallments);

        if (sortedInstallments.isEmpty()){
          continue;
        }
      }
      list.add(paymentOption);
    }
    return list;
  }

}
