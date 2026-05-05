package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.pii.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStation;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class DebtPositionMapper {

  private final PaymentOptionMapper paymentOptionMapper;
  private final InstallmentPIIMapper installmentPIIMapper;
  private final OrganizationService organizationService;

  private static final Collector<PaymentOption, ?, SortedSet<PaymentOption>> toPaymentOptionTreeSet = Collectors.toCollection(TreeSet::new);

  public DebtPositionMapper(PaymentOptionMapper paymentOptionMapper,
                            InstallmentPIIMapper installmentPIIMapper,
                            OrganizationService organizationService) {
    this.paymentOptionMapper = paymentOptionMapper;
    this.installmentPIIMapper = installmentPIIMapper;
    this.organizationService = organizationService;
  }

  public DebtPosition mapToModel(DebtPositionDTO dto) {
    String accessToken = SecurityUtils.getAccessToken();
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(dto.getDebtPositionId());
    debtPosition.setIupdOrg(dto.getIupdOrg());
    debtPosition.setDescription(dto.getDescription());
    debtPosition.setStatus(dto.getStatus());
    debtPosition.setOrganizationId(dto.getOrganizationId());
    debtPosition.setDebtPositionTypeOrgId(dto.getDebtPositionTypeOrgId());
    debtPosition.setValidityDate(dto.getValidityDate());
    debtPosition.setDebtPositionOrigin(dto.getDebtPositionOrigin());
    debtPosition.setMultiDebtor(Optional.ofNullable(dto.getMultiDebtor()).orElse(false));
    debtPosition.setFlagPuPagoPaPayment(dto.getFlagPuPagoPaPayment());

    SortedSet<PaymentOption> paymentOptions = dto.getPaymentOptions().stream()
      .map(paymentOptionMapper::mapToModel)
      .collect(toPaymentOptionTreeSet);

    debtPosition.setPaymentOptions(paymentOptions);

    if(dto.getStationId() != null) {
      organizationService.verifyStationId(dto.getOrganizationId(), dto.getStationId(), accessToken);
      debtPosition.setStationId(dto.getStationId());
    } else {
      OrganizationStation defaultOrganizationStation = organizationService.getDefaultOrganizationStation(dto.getOrganizationId(), accessToken);
      debtPosition.setStationId(defaultOrganizationStation.getStationId());
    }

    return debtPosition;
  }

  public List<DebtPositionDTO> mapAllToDto(List<DebtPosition> debtPosition) {
    Map<Long, ArrayList<InstallmentDTO>> poId2InstallmentDTO = buildPo2InstallmentDTOMap(debtPosition.stream());

    return debtPosition.stream()
      .map(dp -> mapToDto(dp, poId2InstallmentDTO))
      .toList();
  }

  private Map<Long, ArrayList<InstallmentDTO>> buildPo2InstallmentDTOMap(Stream<DebtPosition> dpStream) {
    List<InstallmentNoPII> installments = dpStream
      .flatMap(dp -> dp.getPaymentOptions().stream())
      .flatMap(po -> po.getInstallments().stream())
      .toList();
    @SuppressWarnings("DataFlowIssue") // paymentOptionId cannot be null when building noPII into FullDTO
    Map<Long, ArrayList<InstallmentDTO>> poId2InstallmentDTO = installmentPIIMapper.mapAll(installments).stream()
      .collect(Collectors.groupingBy(InstallmentDTO::getPaymentOptionId, Collectors.toCollection(ArrayList<InstallmentDTO>::new)));
    return poId2InstallmentDTO;
  }

  public DebtPositionDTO mapToDto(DebtPosition debtPosition) {
    return mapToDto(debtPosition, null);
  }

  private DebtPositionDTO mapToDto(DebtPosition debtPosition, Map<Long, ArrayList<InstallmentDTO>> poId2InstallmentDTO) {
    @SuppressWarnings("unchecked") // type check verified by construction
    Map<Long, ArrayList<InstallmentDTO>>[] poId2InstallmentDTOHolder = new Map[1];
    if(poId2InstallmentDTO == null) {
      poId2InstallmentDTOHolder[0] = buildPo2InstallmentDTOMap(Stream.of(debtPosition));
    } else {
      poId2InstallmentDTOHolder[0] = poId2InstallmentDTO;
    }

    DebtPositionDTO dto = DebtPositionDTO.builder()
      .iupdOrg(debtPosition.getIupdOrg())
      .description(debtPosition.getDescription())
      .status(debtPosition.getStatus())
      .organizationId(debtPosition.getOrganizationId())
      .debtPositionTypeOrgId(debtPosition.getDebtPositionTypeOrgId())
      .validityDate(debtPosition.getValidityDate())
      .debtPositionOrigin(debtPosition.getDebtPositionOrigin())
      .multiDebtor(debtPosition.isMultiDebtor())
      .flagPuPagoPaPayment(debtPosition.isFlagPuPagoPaPayment())
      .paymentOptions(
        debtPosition.getPaymentOptions().stream()
          .map(po -> paymentOptionMapper.mapToDto(po, poId2InstallmentDTOHolder[0].get(po.getPaymentOptionId())))
          .collect(Collectors.toCollection(ArrayList<PaymentOptionDTO>::new))
      )
      .stationId(debtPosition.getStationId())
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
        mappedPagedDebtPositions.setContent(mapAllToDto(pagedDebtPositionsDTO.getContent()));
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
}

