package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.common.pii.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.filters.LocalDateTimeIntervalFilter;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.DebtorDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.pages.PagedDebtorUnpaidDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionSaveService debtPositionSaveService;
  private final DebtPositionMapper debtPositionMapper;
  private final DebtPositionDeleteService debtPositionDeleteService;
  private final DataCipherService dataCipherService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final PagedDebtorUnpaidDebtPositionMapper pagedDebtorUnpaidDebtPositionMapper;
  private final DebtorDebtPositionMapper debtorDebtPositionMapper;

  public DebtPositionServiceImpl(DebtPositionRepository debtPositionRepository,
                                 DebtPositionSaveService debtPositionSaveService,
                                 DebtPositionMapper debtPositionMapper, DebtPositionDeleteService debtPositionDeleteService, DataCipherService dataCipherService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, PagedDebtorUnpaidDebtPositionMapper pagedDebtorUnpaidDebtPositionMapper, DebtorDebtPositionMapper debtorDebtPositionMapper) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionSaveService = debtPositionSaveService;
    this.debtPositionMapper = debtPositionMapper;
    this.debtPositionDeleteService = debtPositionDeleteService;
    this.dataCipherService = dataCipherService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.pagedDebtorUnpaidDebtPositionMapper = pagedDebtorUnpaidDebtPositionMapper;
    this.debtorDebtPositionMapper = debtorDebtPositionMapper;
  }

  @Override
  public void saveDebtPosition(DebtPositionDTO debtPositionDTO, String accessToken) {
    debtPositionSaveService.saveDebtPositionDTO(debtPositionDTO, accessToken);
  }

  @Override
  public void saveDebtPosition(DebtPosition debtPosition) {
    debtPositionSaveService.saveDebtPosition(debtPosition);
  }

  @Override
  public List<DebtPositionDTO> mapAllDebtPosition(List<DebtPosition> debtPosition) {
    return debtPositionMapper.mapAllToDto(debtPosition);
  }

  @Override
  public DebtPositionDTO mapDebtPosition(DebtPosition debtPosition) {
    return debtPositionMapper.mapToDto(debtPosition);
  }

  @Override
  public DebtPositionDTO getDebtPosition(Long debtPositionId) {
    DebtPosition debtPosition = getDebtPositionNoPII(debtPositionId);
    return mapDebtPosition(debtPosition);
  }

  @Override
  public DebtPositionDTO getDebtPositionByInstallmentId(Long installmentId) {
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByInstallmentId(installmentId);
    if (debtPosition == null) {
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_NOT_FOUND, "DebtPosition having installmentId %d not found".formatted(installmentId));
    }
    return mapDebtPosition(debtPosition);
  }

  @Override
  public List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigins) {
    List<DebtPosition> debtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndNav(organizationId, nav, debtPositionOrigins);
    return mapAllDebtPosition(debtPositions);
  }

  @Override
  public List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIuv(Long organizationId, String iuv, List<DebtPositionOrigin> debtPositionOrigin) {
    List<DebtPosition> debtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndInstallmentIuv(organizationId, iuv, debtPositionOrigin);
    return mapAllDebtPosition(debtPositions);
  }

  @Override
  public List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIud(Long organizationId, String iud, List<DebtPositionOrigin> debtPositionOrigin) {
    List<DebtPosition> debtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndInstallmentIud(organizationId, iud, debtPositionOrigin);
    return mapAllDebtPosition(debtPositions);
  }

  @Override
  public DebtPosition getDebtPositionNoPII(Long debtPositionId) {
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByDebtPositionId(debtPositionId);
    if (debtPosition == null) {
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_NOT_FOUND, "DebtPosition having debtPositionId %d not found".formatted(debtPositionId));
    }
    return debtPosition;
  }

  @Override
  public PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, List<InstallmentStatus> statusToExclude, Pageable pageable) {
    Page<DebtPosition> pagedDebtPositionsDTO = debtPositionRepository.findEntityGraphByIngestionFlowFileIdAndStatusToExclude(ingestionFlowFileId, statusToExclude, pageable);

    return debtPositionMapper.mapToPagedDebtPositions(pagedDebtPositionsDTO);
  }

  @Override
  public void delete(DebtPosition debtPosition) {
    debtPositionDeleteService.delete(debtPosition);
  }

  @Override
  public Optional<DebtPosition> getDebtPositionByIupdAndOrganizationId(String iupd, Long organizationId) {
    return debtPositionRepository.findDebtPositionByIupdOrgAndOrganizationId(iupd, organizationId);
  }

  @Override
  public List<DebtPositionDTO> getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(String debtorFiscalCode, PersonEntityType debtorEntityType, List<InstallmentStatus> status, List<DebtPositionOrigin> debtPositionOrigin, List<String> debtPositionTypeOrgCodesToExclude, List<Long> organizationIds, LocalDateTimeIntervalFilter dateTimeIntervalFilter) {
    if (debtorFiscalCode == null || debtorEntityType == null) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_FIELDS, "debtorFiscalCode and debtorEntityType are required");
    }

    byte[] debtorFiscalCodeHash = dataCipherService.hash(debtorFiscalCode);

    List<DebtPosition> debtPositions = debtPositionRepository.findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      debtorFiscalCodeHash,
      debtorEntityType,
      status,
      debtPositionOrigin,
      debtPositionTypeOrgCodesToExclude,
      organizationIds,
      dateTimeIntervalFilter
    );

    return mapAllDebtPosition(debtPositions);
  }

  @Override
  public PagedDebtorUnpaidDebtPositionDTO getPagedDebtorUnpaidDebtPosition(String xFiscalCode, List<Long> organizationIds, Pageable pageable) {
    Page<DebtPosition> pagedPrimaryDebtPositionByFilters = debtPositionRepository.findEntityGraphUnpaidOrdinaryDebtPositionByDebtorFiscalCode(xFiscalCode, organizationIds, pageable);
    byte[] hashedDebtorFiscalCode = dataCipherService.hash(xFiscalCode);

    return pagedDebtorUnpaidDebtPositionMapper.map(pagedPrimaryDebtPositionByFilters, retrieveDebtPositionTypeOrgMap(pagedPrimaryDebtPositionByFilters), hashedDebtorFiscalCode);
  }

  @Override
  public DebtorDebtPositionDTO getDebtorUnpaidDebtPositionOverview(Long debtPositionId, String xFiscalCode, Long organizationId) {
    DebtPosition primaryDebtPositionDetail = debtPositionRepository.findEntityGraphUnpaidOrPaidDebtPositionsByDebtorFiscalCode(debtPositionId, xFiscalCode, organizationId);
    if (primaryDebtPositionDetail == null){
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_NOT_FOUND, "DebtPosition with id "+ debtPositionId + "not found");
    }

    byte[] hashedDebtorFiscalCode = dataCipherService.hash(xFiscalCode);
    return debtorDebtPositionMapper.map(primaryDebtPositionDetail, retrieveDebtPositionTypeOrg(primaryDebtPositionDetail.getDebtPositionTypeOrgId()), hashedDebtorFiscalCode);
  }

  private Map<Long, DebtPositionTypeOrg> retrieveDebtPositionTypeOrgMap(Page<DebtPosition> pagedPrimaryDebtPositionByFilters){
    List<DebtPosition> debtPositions = pagedPrimaryDebtPositionByFilters.getContent();
    if (debtPositions.isEmpty()){
      return Collections.emptyMap();
    }

    return debtPositions.stream()
      .collect(Collectors.toMap(DebtPosition::getDebtPositionId, dp -> retrieveDebtPositionTypeOrg(dp.getDebtPositionTypeOrgId())));
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long debtPositionTypeOrgId){
    return debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)
      .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_ORG_NOT_FOUND, "DebtPositionTypeOrg with id "+ debtPositionTypeOrgId + "not found"));
  }
}

