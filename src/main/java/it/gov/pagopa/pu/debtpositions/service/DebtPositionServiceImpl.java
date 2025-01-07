package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;

public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionMapper debtPositionMapper;

  public DebtPositionServiceImpl(DebtPositionRepository debtPositionRepository, DebtPositionMapper debtPositionMapper) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Override
  public DebtPositionDTO saveDebtPosition(DebtPositionDTO debtPositionDTO) {
    DebtPosition debtPosition = debtPositionMapper.mapToModel(debtPositionDTO);
    DebtPosition savedDebtPosition = debtPositionRepository.save(debtPosition);
    return debtPositionMapper.mapToDto(savedDebtPosition);
  }
}

