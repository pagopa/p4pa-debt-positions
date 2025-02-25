package it.gov.pagopa.pu.debtpositions.mapper;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

import it.gov.pagopa.pu.debtpositions.dto.DebtPositionWithType;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO.DebtPositionDTOBuilder;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DebtPositionMapper {

  private final PaymentOptionMapper paymentOptionMapper;

  private static final Collector<PaymentOption, ?, SortedSet<PaymentOption>> toPaymentOptionTreeSet = Collectors.toCollection(TreeSet::new);
  private static final String MULTI_DEBTOR_FULLNAME = "CO-OBBLIGATO";

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
    debtPosition.setFlagPagoPaPayment(dto.getFlagPagoPaPayment());
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
    return getDebtPositionDTOBuilder(
      debtPosition, DebtPositionDTO.builder()).build();
  }

  private <C extends DebtPositionDTO,B extends DebtPositionDTOBuilder<C,B>> DebtPositionDTOBuilder<C, B> getDebtPositionDTOBuilder(
    DebtPosition debtPosition, DebtPositionDTOBuilder<C,B> builder) {
    return builder
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
      .flagPagoPaPayment(debtPosition.isFlagPagoPaPayment())
      .creationDate(localDatetimeToOffsetDateTime(debtPosition.getCreationDate()))
      .updateDate(localDatetimeToOffsetDateTime(debtPosition.getUpdateDate()))
      .paymentOptions(
        debtPosition.getPaymentOptions().stream()
          .map(paymentOptionMapper::mapToDto)
          .toList()
      );
  }

  public DebtPositionDetailDTO mapToDebtPositionDetailDTO(
    DebtPositionWithType debtPositionWithType){
    DebtPositionDetailDTO debtPositionDetailDTO = getDebtPositionDTOBuilder(
      debtPositionWithType.getDebtPosition(), DebtPositionDetailDTO.builder())
      .build();
    setDebtor(debtPositionDetailDTO);
    debtPositionDetailDTO.setDebtPositionTypeOrgDescription(debtPositionWithType.getDebtPositionTypeOrgDescription());
    debtPositionDetailDTO.setDebtPositionTypeOrgCode(debtPositionWithType.getDebtPositionTypeOrgCode());
    return debtPositionDetailDTO;
  }

  private void setDebtor(DebtPositionDetailDTO debtPositionDetailDTO) {
    if(Boolean.TRUE.equals(debtPositionDetailDTO.getMultiDebtor())){
      debtPositionDetailDTO.setDebtor(PersonDTO.builder()
        .fullName(MULTI_DEBTOR_FULLNAME)
        .build());
    } else if(!CollectionUtils.isEmpty(debtPositionDetailDTO.getPaymentOptions()) && !CollectionUtils.isEmpty(
        debtPositionDetailDTO.getPaymentOptions().getFirst().getInstallments())){
      debtPositionDetailDTO.setDebtor(debtPositionDetailDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor());
    }
  }
}

