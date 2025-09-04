package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TechnicalMixedDebtPositionBuilderService {

  private final DebtPositionMapper debtPositionMapper;

  public List<DebtPosition> createTechnicalMixedDebtPositions(
    DebtPositionDTO debtPositionDTO) {
    DebtPosition debtPosition = debtPositionMapper.mapToModel(debtPositionDTO);
    // TODO
    return List.of(debtPosition);
  }
}
