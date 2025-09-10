package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.mapper.TechnicalMixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
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
      DebtPosition technicalDebtPosition = technicalMixedDebtPositionMapper.toTechnicalMixedDebtPosition(
        debtPosition, debtPositionTypeOrgId, DebtPositionStatus.UNPAID);

      List<MixedDpAdditionalData> mixedDpAdditionalDataList = debtPositionTypeOrgId2TransfersData.get(
        debtPositionTypeOrgId);
      InstallmentNoPII installment = debtPosition.getPaymentOptions().getFirst()
        .getInstallments().getFirst();
      SortedSet<InstallmentNoPII> technicalMixedDpInstallments = new TreeSet<>();
      mixedDpAdditionalDataList
        .forEach(mixedDpAdditionalData -> technicalMixedDpInstallments.add(
          technicalMixedDebtPositionMapper.toTechnicalMixedDPInstallment(
            installment,
            InstallmentStatus.UNPAYABLE, mixedDpAdditionalData)));

      technicalDebtPosition.getPaymentOptions().getFirst()
        .setInstallments(technicalMixedDpInstallments);

      technicalMixedDebtPositions.add(technicalDebtPosition);
    }

    return technicalMixedDebtPositions;
  }
}
