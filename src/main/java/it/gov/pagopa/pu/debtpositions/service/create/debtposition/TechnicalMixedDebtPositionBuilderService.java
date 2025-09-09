package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TechnicalMixedDebtPositionBuilderService {

  // TODO: Class not complete. [P4ADEV-3518] Technical Mixed Debt Position creation
  public List<DebtPosition> createTechnicalMixedDebtPositions(Map<Long, List<MixedDpAdditionalData>> debtPositionTypeOrgId2TransfersData, DebtPosition debtPosition) {
    List<DebtPosition> technicalMixedDebtPositions = new ArrayList<>();

    for (Long debtPositionTypeOrgId : debtPositionTypeOrgId2TransfersData.keySet()) {
      DebtPosition technicalDebtPosition = new DebtPosition();


      List<MixedDpAdditionalData> transfers = debtPositionTypeOrgId2TransfersData.get(debtPositionTypeOrgId);


      technicalMixedDebtPositions.add(technicalDebtPosition);
    }

    return null;
  }
}
