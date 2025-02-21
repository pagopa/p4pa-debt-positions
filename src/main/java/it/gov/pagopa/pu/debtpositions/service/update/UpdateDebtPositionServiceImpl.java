package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
public class UpdateDebtPositionServiceImpl implements UpdateDebtPositionService {

  private final DebtPositionSyncService debtPositionSyncService;
  private final DebtPositionProcessorService debtPositionProcessorService;
  private final DebtPositionService debtPositionService;
  private final OrganizationService organizationService;

  public UpdateDebtPositionServiceImpl(DebtPositionSyncService debtPositionSyncService, DebtPositionProcessorService debtPositionProcessorService, DebtPositionService debtPositionService, OrganizationService organizationService) {
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.debtPositionService = debtPositionService;
    this.organizationService = organizationService;
  }

  @Override
  public String updateDebtPosition(DebtPositionDTO debtPositionToSyncDTO, List<Long> installmentIds, Boolean massive, String accessToken) {
    log.info("Updating data of debt position with id {}", debtPositionToSyncDTO.getDebtPositionId());

    debtPositionToSyncDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream()
        .filter(installmentDTO -> installmentIds.contains(installmentDTO.getInstallmentId()))
        .findFirst()
        .ifPresent(installmentDTO -> {
            InstallmentSyncStatus syncStatus = updateSyncStatus(installmentDTO.getStatus(), installmentDTO.getDueDate());
            installmentDTO.setSyncStatus(syncStatus);
            installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
          }
        ));

    debtPositionProcessorService.updateAmounts(debtPositionToSyncDTO);

    Organization org = organizationService.getOrganizationById(debtPositionToSyncDTO.getOrganizationId(), accessToken)
      .orElseThrow(() -> new InvalidValueException("Provided organization id not found on db."));

    debtPositionService.saveDebtPosition(debtPositionToSyncDTO, org);

    return invokeWorkflow(debtPositionToSyncDTO, accessToken, massive);
  }

  private InstallmentSyncStatus updateSyncStatus(InstallmentStatus oldStatus, OffsetDateTime newDueDate) {
    InstallmentSyncStatus syncStatus = new InstallmentSyncStatus();
    syncStatus.setSyncStatusFrom(oldStatus);
    syncStatus.setSyncStatusTo(oldStatus);

    if (oldStatus.equals(InstallmentStatus.EXPIRED) && newDueDate != null && newDueDate.isAfter(OffsetDateTime.now())) {
      syncStatus.setSyncStatusTo(InstallmentStatus.UNPAID);
    }
    return syncStatus;
  }

  private String invokeWorkflow(DebtPositionDTO debtPositionDTO, String accessToken, Boolean massive) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      WorkflowCreatedDTO workflowCreatedDTO = debtPositionSyncService.syncDebtPosition(debtPositionDTO, massive, PaymentEventType.DP_UPDATED, accessToken);
      if (workflowCreatedDTO != null) {
        return workflowCreatedDTO.getWorkflowId();
      }
    }
    return null;
  }
}
