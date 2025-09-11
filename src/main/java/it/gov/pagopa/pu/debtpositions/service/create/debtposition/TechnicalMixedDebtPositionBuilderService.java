package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.mapper.TechnicalMixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TechnicalMixedDebtPositionBuilderService {

  private final TechnicalMixedDebtPositionMapper technicalMixedDebtPositionMapper;

  public List<DebtPosition> createTechnicalMixedDebtPositions(
    Map<Long, List<MixedDpAdditionalData>> debtPositionTypeOrgId2TransfersData,
    DebtPosition debtPosition) {
    List<DebtPosition> technicalMixedDebtPositions = new ArrayList<>();

    for (Long debtPositionTypeOrgId : debtPositionTypeOrgId2TransfersData.keySet()) {
      List<MixedDpAdditionalData> mixedDpAdditionalDataList = debtPositionTypeOrgId2TransfersData.get(
        debtPositionTypeOrgId);
      DebtPosition technicalDebtPosition = technicalMixedDebtPositionMapper.toTechnicalMixedDebtPosition(
        debtPosition, debtPositionTypeOrgId, true, mixedDpAdditionalDataList);
      technicalMixedDebtPositions.add(technicalDebtPosition);
    }

    return technicalMixedDebtPositions;
  }
}
