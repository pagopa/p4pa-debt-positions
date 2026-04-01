package it.gov.pagopa.pu.debtpositions.service.update.massive;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class MassiveUpdateServiceImpl implements MassiveUpdateService {
  private final DebtPositionService debtPositionService;
  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;
  private final DebtPositionSyncService debtPositionSyncService;

  public MassiveUpdateServiceImpl(
    DebtPositionService debtPositionService,
    DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService,
    DebtPositionSyncService debtPositionSyncService
  ) {
    this.debtPositionService = debtPositionService;
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.debtPositionSyncService = debtPositionSyncService;
  }

  @Override
  public void updateTransferIbansAndSyncDebtPosition(Long debtPositionId, String oldIban, String newIban, String oldPostalIban, String newPostalIban, String accessToken) {
    DebtPositionDTO debtPositionDTO = debtPositionService.getDebtPosition(debtPositionId);
    if (debtPositionDTO == null) {
      throw new NotFoundException("[DEBT_POSITION_NOT_FOUND] DebtPosition with id %d not found".formatted(debtPositionId));
    }

    Set<String> collectedIuds = new HashSet<>();

    debtPositionDTO.getPaymentOptions()
      .forEach(po ->
        po.getInstallments()
          .forEach(i ->
            i.getTransfers().forEach(t -> {
              if (oldIban.equals(t.getIban()) && Objects.equals(oldPostalIban, t.getPostalIban())) {
                t.setIban(newIban);
                t.setPostalIban(newPostalIban);
                i.setStatus(InstallmentStatus.TO_SYNC);
                collectedIuds.add(i.getIud());
              }
            })));

    debtPositionHierarchyStatusAlignerService.alignHierarchyStatus(debtPositionDTO);
    debtPositionService.saveDebtPosition(debtPositionDTO);

    debtPositionSyncService.syncDebtPosition(
      debtPositionDTO,
      new WfExecutionParameters(),
      PaymentEventType.DP_UPDATED,
      "IUD: " + String.join(", ", collectedIuds),
      accessToken
    );
  }
}
