package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.BasePaymentOption;
import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import org.mapstruct.Context;
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
  DebtorDebtPositionDTO map(DebtPosition debtPosition, DebtPositionTypeOrg debtPositionTypeOrg, @Context byte[] hashedDebtorFiscalCode);

  @Named("mapPaymentOption")
  default List<BasePaymentOption> paymentOptionSortedSetToBasePaymentOptionList(SortedSet<PaymentOption> sortedSet, @Context byte[] hashedDebtorFiscalCode) {
    if (sortedSet == null) {
      return Collections.emptyList();
    }
    Set<PaymentOption> filteredSortedSet = sortedSet.stream()
      .filter(po -> InstallmentUtils.PAYABLE_AND_EXPIRED_PO_STATUSES.contains(po.getStatus()))
      .collect(Collectors.toSet());
    List<BasePaymentOption> list = new ArrayList<>(filteredSortedSet.size());

    for (PaymentOption paymentOption : filteredSortedSet) {

      SortedSet<InstallmentNoPII> installments = paymentOption.getInstallments();
      if (installments != null) {
        SortedSet<InstallmentNoPII> sortedInstallments = getFilteredInstallments(hashedDebtorFiscalCode, installments);
        paymentOption.setInstallments(sortedInstallments);

        if (sortedInstallments.isEmpty()){
          continue;
        }
      }
      list.add(paymentOption);
    }
    return list;
  }

  private static SortedSet<InstallmentNoPII> getFilteredInstallments(byte[] hashedDebtorFiscalCode, SortedSet<InstallmentNoPII> installments) {
    return installments.stream().filter(i ->
        InstallmentUtils.UNPAID_OR_PAID_INSTALLMENT_STATUSES.contains(i.getStatus())
          && Arrays.equals(i.getDebtorFiscalCodeHash(), hashedDebtorFiscalCode)
      )
      .collect(Collectors.toCollection(() ->
        new TreeSet<>(
          Comparator
            .comparing(
              InstallmentNoPII::getDueDate,
              Comparator.nullsFirst(Comparator.naturalOrder())
            )
            .thenComparing(InstallmentNoPII::getInstallmentId)
        )
      ));
  }

}
