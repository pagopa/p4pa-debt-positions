package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.mapper.TechnicalMixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    DebtPosition debtPosition, boolean isUnpayable, String accessToken) {
    List<DebtPosition> technicalMixedDebtPositions = new ArrayList<>();

    for (Long debtPositionTypeOrgId : debtPositionTypeOrgId2TransfersData.keySet()) {
      List<MixedDpAdditionalData> mixedDpAdditionalDataList = debtPositionTypeOrgId2TransfersData.get(
        debtPositionTypeOrgId);
      DebtPosition technicalDebtPosition = technicalMixedDebtPositionMapper.toTechnicalMixedDebtPosition(
        debtPosition, debtPositionTypeOrgId, isUnpayable, mixedDpAdditionalDataList, accessToken);
      technicalMixedDebtPositions.add(technicalDebtPosition);
    }

    return technicalMixedDebtPositions;
  }
}
