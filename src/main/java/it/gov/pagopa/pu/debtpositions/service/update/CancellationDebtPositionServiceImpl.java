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

import java.util.List;

@Service
@Slf4j
public class CancellationDebtPositionServiceImpl implements CancellationDebtPositionService {

  private final DebtPositionProcessorService debtPositionProcessorService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final DebtPositionService debtPositionService;
  private final OrganizationService organizationService;

  public CancellationDebtPositionServiceImpl(DebtPositionProcessorService debtPositionProcessorService, DebtPositionSyncService debtPositionSyncService, DebtPositionService debtPositionService, OrganizationService organizationService) {
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionService = debtPositionService;
    this.organizationService = organizationService;
  }

  @Override
  public String cancelInstallment(DebtPositionDTO debtPositionToSyncDTO, List<Long> installmentIds, Boolean massive, String accessToken) {
    log.info("Updating status cancelled for installments with id {}", installmentIds);

    debtPositionToSyncDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream()
        .filter(installmentDTO -> installmentIds.contains(installmentDTO.getInstallmentId()))
        .findFirst()
        .ifPresent(installmentDTO -> {
            installmentDTO.setSyncStatus(new InstallmentSyncStatus(installmentDTO.getStatus(), InstallmentStatus.CANCELLED));
            installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
          }
        ));

    debtPositionProcessorService.updateAmounts(debtPositionToSyncDTO);

    Organization org = organizationService.getOrganizationById(debtPositionToSyncDTO.getOrganizationId(), accessToken)
      .orElseThrow(() -> new InvalidValueException("Provided organization id not found on db."));

    debtPositionService.saveDebtPosition(debtPositionToSyncDTO, org);

    return invokeWorkflow(debtPositionToSyncDTO, accessToken, massive);
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
