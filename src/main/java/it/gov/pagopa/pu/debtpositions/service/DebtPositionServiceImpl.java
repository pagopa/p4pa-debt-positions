package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionSaveService debtPositionSaveService;
  private final DebtPositionMapper debtPositionMapper;

  public DebtPositionServiceImpl(DebtPositionRepository debtPositionRepository,
                                 DebtPositionSaveService debtPositionSaveService,
                                 DebtPositionMapper debtPositionMapper) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionSaveService = debtPositionSaveService;
    this.debtPositionMapper = debtPositionMapper;
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
    DebtPosition debtPosition = debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId);
    if (debtPosition == null) {
      throw new NotFoundException("DebtPosition having debtPositionId %d not found".formatted(debtPositionId));
    }
    return debtPositionMapper.mapToDto(debtPosition);
  }

  @Override
  public PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, Pageable pageable) {
    Page<DebtPosition> pagedDebtPositionsDTO = debtPositionRepository.findByIngestionFlowFileId(ingestionFlowFileId, pageable);

    return debtPositionMapper.mapToPagedDebtPositions(pagedDebtPositionsDTO);
  }
}

