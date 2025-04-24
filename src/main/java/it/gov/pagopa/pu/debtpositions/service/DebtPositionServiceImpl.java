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
    return debtPositionMapper.mapToDto(debtPosition);
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
  public PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, Pageable pageable) {
    Page<DebtPosition> pagedDebtPositionsDTO = debtPositionRepository.findByIngestionFlowFileId(ingestionFlowFileId, pageable);

    return debtPositionMapper.mapToPagedDebtPositions(pagedDebtPositionsDTO);
  }

  @Override
  public void delete(DebtPosition debtPosition) {
    debtPositionDeleteService.delete(debtPosition);
  }
}

