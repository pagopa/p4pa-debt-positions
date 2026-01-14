package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtorUnpaidDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PagedDebtorUnpaidDebtPositionMapper {

  @Mapping(
    target = "content",
    expression = "java(source != null ? map(source.getContent(), debtPositionTypeOrgMap, debtorFiscalCode, dataCipherService) : java.util.Collections.emptyList())"
  )
  @Mapping(target = "totalPages", source = "source.totalPages")
  @Mapping(target = "size", source = "source.size")
  @Mapping(target = "number", source = "source.number")
  @Mapping(target = "totalElements", source = "source.totalElements")
  PagedDebtorUnpaidDebtPositionDTO map(Page<DebtPosition> source, @Context Map<Long, DebtPositionTypeOrg> debtPositionTypeOrgMap,  @Context String debtorFiscalCode, @Context DataCipherService dataCipherService);

  @Mapping(target = "debtPositionTypeOrgDescription", source = "debtPositionTypeOrgDescription")
  @Mapping(target = "debtPositionId", source = "debtPosition.debtPositionId")
  @Mapping(target = "debtPositionDescription", source = "debtPosition.description")
  @Mapping(target = "status", source = "debtPosition.status")
  @Mapping(target = "debtPositionOrigin", source = "debtPosition.debtPositionOrigin")
  @Mapping(target = "organizationId", source = "debtPosition.organizationId")
  DebtorDebtPositionDTO map(DebtPosition debtPosition, String debtPositionTypeOrgDescription, @Context String debtorFiscalCode);

  default List<DebtorDebtPositionDTO> map(List<DebtPosition> debtPositions, @Context Map<Long, DebtPositionTypeOrg> debtPositionTypeOrgMap, @Context String debtorFiscalCode, DataCipherService dataCipherService) {
    return debtPositions.stream()
      .map(dp -> {
        DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgMap != null ? debtPositionTypeOrgMap.get(dp.getDebtPositionId()) : null;

        String description = debtPositionTypeOrg != null ? debtPositionTypeOrg.getDescription() : null;

        SortedSet<PaymentOption> filteredPaymentOptions =
          dp.getPaymentOptions().stream()
            .filter(po -> InstallmentUtils.PAYABLE_PO_STATUSES.contains(po.getStatus()))
            .map(po -> po.toBuilder()
              .installments(
                po.getInstallments().stream()
                  .filter(i ->
                    InstallmentStatus.UNPAID.equals(i.getStatus()) &&
                      Arrays.equals(i.getDebtorFiscalCodeHash(), dataCipherService.hash(debtorFiscalCode))
                  )
                  .collect(Collectors.toCollection(() ->
                    new TreeSet<>(Comparator.comparing(InstallmentNoPII::getDueDate,
                      Comparator.nullsFirst(Comparator.naturalOrder())
                    ))
                  ))
              )
              .build()
            )
            .filter(Objects::nonNull)
            .filter(po -> !po.getInstallments().isEmpty())
            .collect(Collectors.toCollection(TreeSet::new));

        DebtPosition filteredDp = dp.toBuilder()
          .paymentOptions(filteredPaymentOptions)
          .build();

        return map(filteredDp, description, debtorFiscalCode);
      })
      .toList();
  }
}
