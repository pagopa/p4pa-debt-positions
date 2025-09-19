package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class DebtPositionMapper {

  private final PaymentOptionMapper paymentOptionMapper;

  private static final Collector<PaymentOption, ?, SortedSet<PaymentOption>> toPaymentOptionTreeSet = Collectors.toCollection(TreeSet::new);

  public DebtPositionMapper(PaymentOptionMapper paymentOptionMapper) {
    this.paymentOptionMapper = paymentOptionMapper;
  }

  public DebtPosition mapToModel(DebtPositionDTO dto) {
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(dto.getDebtPositionId());
    debtPosition.setIupdOrg(dto.getIupdOrg());
    debtPosition.setDescription(dto.getDescription());
    debtPosition.setStatus(dto.getStatus());
    debtPosition.setOrganizationId(dto.getOrganizationId());
    debtPosition.setDebtPositionTypeOrgId(dto.getDebtPositionTypeOrgId());
    debtPosition.setValidityDate(dto.getValidityDate());
    debtPosition.setFlagIuvVolatile(Optional.ofNullable(dto.getFlagIuvVolatile()).orElse(false));
    debtPosition.setDebtPositionOrigin(dto.getDebtPositionOrigin());
    debtPosition.setMultiDebtor(Optional.ofNullable(dto.getMultiDebtor()).orElse(false));
    debtPosition.setFlagPuPagoPaPayment(dto.getFlagPuPagoPaPayment());

    SortedSet<PaymentOption> paymentOptions = dto.getPaymentOptions().stream()
      .map(paymentOptionMapper::mapToModel)
      .collect(toPaymentOptionTreeSet);

    debtPosition.setPaymentOptions(paymentOptions);

    return debtPosition;
  }

  public DebtPositionDTO mapToDto(DebtPosition debtPosition) {
    DebtPositionDTO dto = DebtPositionDTO.builder()
      .iupdOrg(debtPosition.getIupdOrg())
      .description(debtPosition.getDescription())
      .status(debtPosition.getStatus())
      .organizationId(debtPosition.getOrganizationId())
      .debtPositionTypeOrgId(debtPosition.getDebtPositionTypeOrgId())
      .validityDate(debtPosition.getValidityDate())
      .flagIuvVolatile(debtPosition.isFlagIuvVolatile())
      .debtPositionOrigin(debtPosition.getDebtPositionOrigin())
      .multiDebtor(debtPosition.isMultiDebtor())
      .flagPuPagoPaPayment(debtPosition.isFlagPuPagoPaPayment())
      .paymentOptions(
        debtPosition.getPaymentOptions().stream()
          .map(paymentOptionMapper::mapToDto)
          .collect(Collectors.toCollection(ArrayList<PaymentOptionDTO>::new))
      )
      .build();

    setToDtoAutoDbFields(dto, debtPosition);
    return dto;
  }

  public static void setToDtoAutoDbFields(DebtPositionDTO debtPositionDTO, DebtPosition debtPosition) {
    debtPositionDTO.setDebtPositionId(debtPosition.getDebtPositionId());
    debtPositionDTO.setCreationDate(localDatetimeToOffsetDateTime(debtPosition.getCreationDate()));
    debtPositionDTO.setUpdateDate(localDatetimeToOffsetDateTime(debtPosition.getUpdateDate()));
    debtPositionDTO.setUpdateOperatorExternalId(debtPosition.getUpdateOperatorExternalId());
    debtPositionDTO.setUpdateTraceId(debtPosition.getUpdateTraceId());
  }

  public PagedDebtPositions mapToPagedDebtPositions(Page<DebtPosition> pagedDebtPositionsDTO) {
    PagedDebtPositions mappedPagedDebtPositions = new PagedDebtPositions();
    if (pagedDebtPositionsDTO != null) {
      if (!pagedDebtPositionsDTO.getContent().isEmpty()) {
        mappedPagedDebtPositions.setContent(pagedDebtPositionsDTO.stream().map(this::mapToDto).toList());
      } else {
        mappedPagedDebtPositions.setContent(Collections.emptyList());
      }

      if (pagedDebtPositionsDTO.getPageable().isPaged()) {
        mappedPagedDebtPositions.setTotalPages((long) pagedDebtPositionsDTO.getTotalPages());
        mappedPagedDebtPositions.setSize((long) pagedDebtPositionsDTO.getSize());
        mappedPagedDebtPositions.setNumber((long) pagedDebtPositionsDTO.getNumber());
        mappedPagedDebtPositions.setTotalElements(pagedDebtPositionsDTO.getTotalElements());
      }
    }
    return mappedPagedDebtPositions;
  }

  public void updateFromDTO(DebtPosition debtPosition, DebtPositionDTO dto) {
    debtPosition.setDescription(dto.getDescription());
    debtPosition.setStatus(dto.getStatus());
    debtPosition.setValidityDate(dto.getValidityDate());
    debtPosition.setFlagIuvVolatile(Optional.ofNullable(dto.getFlagIuvVolatile()).orElse(false));
    debtPosition.setMultiDebtor(Optional.ofNullable(dto.getMultiDebtor()).orElse(false));
    debtPosition.setFlagPuPagoPaPayment(dto.getFlagPuPagoPaPayment());

    List<PaymentOptionDTO> paymentOptionDTOList = dto.getPaymentOptions().stream().toList();
    List<PaymentOption> paymentOptionList = debtPosition.getPaymentOptions().stream().toList();

    IntStream.range(0, Math.min(paymentOptionDTOList.size(), paymentOptionList.size())).forEach(i -> {
      paymentOptionMapper.updateFromDTO(paymentOptionList.get(i), paymentOptionDTOList.get(i));
    });

    SortedSet<PaymentOption> paymentOptions = new TreeSet<>(paymentOptionList);

    debtPosition.setPaymentOptions(paymentOptions);
  }
}

