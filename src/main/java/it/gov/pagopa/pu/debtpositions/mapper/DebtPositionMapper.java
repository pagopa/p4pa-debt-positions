package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class DebtPositionMapper {

  private final PaymentOptionMapper paymentOptionMapper;

  private static final Collector<PaymentOption, ?, SortedSet<PaymentOption>> toPaymentOptionTreeSet = Collectors.toCollection(TreeSet::new);

  public DebtPositionMapper(PaymentOptionMapper paymentOptionMapper) {
    this.paymentOptionMapper = paymentOptionMapper;
  }

  public Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mapToModel(DebtPositionDTO dto) {
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(dto.getDebtPositionId());
    debtPosition.setIupdOrg(dto.getIupdOrg());
    debtPosition.setDescription(dto.getDescription());
    debtPosition.setStatus(dto.getStatus());
    debtPosition.setOrganizationId(dto.getOrganizationId());
    debtPosition.setDebtPositionTypeOrgId(dto.getDebtPositionTypeOrgId());
    debtPosition.setValidityDate(dto.getValidityDate());
    debtPosition.setFlagIuvVolatile(dto.getFlagIuvVolatile());
    debtPosition.setDebtPositionOrigin(dto.getDebtPositionOrigin());
    debtPosition.setMultiDebtor(dto.getMultiDebtor());
    debtPosition.setCreationDate(dto.getCreationDate().toLocalDateTime());
    debtPosition.setUpdateDate(dto.getUpdateDate().toLocalDateTime());

    Map<InstallmentNoPII, Installment> installmentMapping = new HashMap<>();

    SortedSet<PaymentOption> paymentOptions = dto.getPaymentOptions().stream()
      .map(paymentOptionDTO -> {
        Pair<PaymentOption, Map<InstallmentNoPII, Installment>> paymentOptionWithInstallments = paymentOptionMapper.mapToModel(paymentOptionDTO);
        installmentMapping.putAll(paymentOptionWithInstallments.getSecond());
        return paymentOptionWithInstallments.getFirst();
      })
      .collect(toPaymentOptionTreeSet);

    debtPosition.setPaymentOptions(paymentOptions);

    return Pair.of(debtPosition, installmentMapping);
  }

  public DebtPositionDTO mapToDto(DebtPosition debtPosition){
    return DebtPositionDTO.builder()
      .debtPositionId(debtPosition.getDebtPositionId())
      .iupdOrg(debtPosition.getIupdOrg())
      .description(debtPosition.getDescription())
      .status(debtPosition.getStatus())
      .organizationId(debtPosition.getOrganizationId())
      .debtPositionTypeOrgId(debtPosition.getDebtPositionTypeOrgId())
      .validityDate(debtPosition.getValidityDate())
      .flagIuvVolatile(debtPosition.isFlagIuvVolatile())
      .debtPositionOrigin(debtPosition.getDebtPositionOrigin())
      .multiDebtor(debtPosition.isMultiDebtor())
      .creationDate(localDatetimeToOffsetDateTime(debtPosition.getCreationDate()))
      .updateDate(localDatetimeToOffsetDateTime(debtPosition.getUpdateDate()))
      .paymentOptions(
        debtPosition.getPaymentOptions().stream()
          .map(paymentOptionMapper::mapToDto)
          .toList()
      )      .build();
  }
}

