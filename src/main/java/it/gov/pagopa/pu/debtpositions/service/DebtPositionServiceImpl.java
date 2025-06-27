package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionSaveService debtPositionSaveService;
  private final DebtPositionMapper debtPositionMapper;
  private final DebtPositionDeleteService debtPositionDeleteService;

  public DebtPositionServiceImpl(DebtPositionRepository debtPositionRepository,
                                 DebtPositionSaveService debtPositionSaveService,
                                 DebtPositionMapper debtPositionMapper, DebtPositionDeleteService debtPositionDeleteService) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionSaveService = debtPositionSaveService;
    this.debtPositionMapper = debtPositionMapper;
    this.debtPositionDeleteService = debtPositionDeleteService;
  }

  @Override
  public void saveDebtPosition(DebtPositionDTO debtPositionDTO) {
    debtPositionSaveService.saveDebtPositionDTO(debtPositionDTO);
  }

  @Override
  public void saveDebtPosition(DebtPosition debtPosition) {
    debtPositionSaveService.saveDebtPosition(debtPosition);
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
    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installmentId);
    if (debtPosition == null) {
      throw new NotFoundException("DebtPosition having installmentId %d not found".formatted(installmentId));
    }
    return mapDebtPosition(debtPosition);
  }

  @Override
  public List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIuv(Long organizationId, String iuv, List<DebtPositionOrigin> debtPositionOrigin) {
    List<DebtPosition> debtPositions = debtPositionRepository.findByOrganizationIdAndInstallmentIuv(organizationId, iuv, debtPositionOrigin);
    return debtPositions.stream().map(this::mapDebtPosition).toList();
  }

  @Override
  public List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIud(Long organizationId, String iud, List<DebtPositionOrigin> debtPositionOrigin) {
    List<DebtPosition> debtPositions = debtPositionRepository.findByOrganizationIdAndInstallmentIud(organizationId, iud, debtPositionOrigin);
    return debtPositions.stream().map(this::mapDebtPosition).toList();
  }

  @Override
  public DebtPosition getDebtPositionNoPII(Long debtPositionId) {
    DebtPosition debtPosition = debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId);
    if (debtPosition == null) {
      throw new NotFoundException("DebtPosition having debtPositionId %d not found".formatted(debtPositionId));
    }
    return debtPosition;
  }

  @Override
  public PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, List<InstallmentStatus> statusToExclude, Pageable pageable) {
    Page<DebtPosition> pagedDebtPositionsDTO = debtPositionRepository.findByIngestionFlowFileIdAndStatusToExclude(ingestionFlowFileId, statusToExclude, pageable);

    return debtPositionMapper.mapToPagedDebtPositions(pagedDebtPositionsDTO);
  }

  @Override
  public void delete(DebtPosition debtPosition) {
    debtPositionDeleteService.delete(debtPosition);
  }
}

