package it.gov.pagopa.pu.debtpositions.service.create.debtposition.mixed;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.mapper.TechnicalMixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TechnicalMixedDebtPositionBuilderService {

  private final TechnicalMixedDebtPositionMapper technicalMixedDebtPositionMapper;

  public List<DebtPosition> createTechnicalMixedDebtPositions(Map<Long, List<MixedDpAdditionalData>> debtPositionTypeOrgId2TransfersData,
                                                              DebtPosition debtPosition, String accessToken) {
    return createTechnicalMixedDebtPositions(debtPositionTypeOrgId2TransfersData, debtPosition, true, accessToken);
  }

  public List<DebtPosition> createTechnicalMixedDebtPositions(Map<Long, List<MixedDpAdditionalData>> debtPositionTypeOrgId2TransfersData,
                                                              DebtPosition debtPosition, boolean isUnpaid, String accessToken) {
    return debtPositionTypeOrgId2TransfersData.entrySet().stream()
      .map(e ->
        technicalMixedDebtPositionMapper.toTechnicalMixedDebtPosition(
          debtPosition, e.getKey(), isUnpaid, e.getValue(), accessToken))
      .toList();
  }
}
