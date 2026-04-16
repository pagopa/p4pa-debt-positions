package it.gov.pagopa.pu.debtpositions.service.update.massive;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    validateNewIbans(newIban, newPostalIban);

    DebtPositionDTO debtPositionDTO = debtPositionService.getDebtPosition(debtPositionId);
    if (debtPositionDTO == null) {
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_NOT_FOUND, "DebtPosition with id %d not found".formatted(debtPositionId));
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

                // Not use InstallmentUtils for TO_SYNC status to preserve syncStatusFrom/to
                if (!InstallmentStatus.TO_SYNC.equals(i.getStatus())) {
                  InstallmentUtils.setStatus(i, i.getStatus());
                } else {
                  i.getSyncStatus().setSyncError(null);
                }

                collectedIuds.add(i.getIud());
              }
            })));

    if (!collectedIuds.isEmpty()) {
      debtPositionHierarchyStatusAlignerService.alignHierarchyStatus(debtPositionDTO);
      debtPositionService.saveDebtPosition(debtPositionDTO);

      debtPositionSyncService.syncDebtPosition(
        debtPositionDTO,
        new WfExecutionParameters(),
        PaymentEventType.DPI_UPDATED,
        "IUD: " + String.join(", ", collectedIuds),
        accessToken
      );
    }
  }

  private void validateNewIbans(String newIban, String newPostalIban) {
    if (StringUtils.isBlank(newIban) || !Utilities.isValidIban(newIban)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IBAN, "Provided newIban is not valid");
    }

    if (StringUtils.isNotBlank(newPostalIban) && !Utilities.isValidIban(newPostalIban)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IBAN, "Provided newPostalIban is not valid");
    }
  }
}
