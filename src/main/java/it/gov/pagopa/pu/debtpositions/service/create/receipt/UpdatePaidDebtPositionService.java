package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UpdatePaidDebtPositionService {

  private final OrganizationService organizationService;
  private final PrimaryOrgInstallmentPaidVerifierService primaryOrgInstallmentPaidVerifierService;
  private final InstallmentUpdateService installmentUpdateService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final DebtPositionService debtPositionService;
  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  public UpdatePaidDebtPositionService(OrganizationService organizationService, PrimaryOrgInstallmentPaidVerifierService primaryOrgInstallmentPaidVerifierService, InstallmentUpdateService installmentUpdateService, DebtPositionSyncService debtPositionSyncService, DebtPositionService debtPositionService, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService) {
    this.organizationService = organizationService;
    this.primaryOrgInstallmentPaidVerifierService = primaryOrgInstallmentPaidVerifierService;
    this.installmentUpdateService = installmentUpdateService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionService = debtPositionService;
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
  }

  boolean handleReceiptReceived(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    return organizationService.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken)
      .map(primaryOrg -> {
        Pair<Optional<InstallmentNoPII>, Boolean> installmentAndPrimaryOrgFound = primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(primaryOrg, receiptDTO.getNoticeNumber());
        installmentAndPrimaryOrgFound.getLeft().ifPresent(installment -> setInstallmentAsPaid(installment, receiptDTO, accessToken));
        return installmentAndPrimaryOrgFound.getRight();
      })
      .orElse(false);
  }

  private void invokeWorkflow(DebtPositionDTO debtPositionDTO, String accessToken) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      WorkflowCreatedDTO workflow = debtPositionSyncService.syncDebtPosition(debtPositionDTO, false, PaymentEventType.RT_RECEIVED, accessToken);
      if (workflow != null) {
        log.info("Workflow creation OK for debtPositionId[{}}: workflowId[{}]", debtPositionDTO.getDebtPositionId(), workflow.getWorkflowId());
      } else {
        log.warn("Workflow creation KO for debtPositionId[{}]: received null response", debtPositionDTO.getDebtPositionId());
      }
    }
  }

  private void setInstallmentAsPaid(InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    log.debug("primaryOrg installment found id[{}]", installment.getInstallmentId());
    //update installment status
    DebtPosition debtPosition = installmentUpdateService.updateInstallmentStatusOfDebtPosition(installment, receiptDTO);
    //align debt position status
    DebtPositionDTO debtPositionDTO = debtPositionHierarchyStatusAlignerService.alignHierarchyStatus(debtPosition);
    //persist updated debt position
    DebtPositionDTO persistedDebtPosition = debtPositionService.saveDebtPosition(debtPositionDTO, receiptDTO.getOrgFiscalCode());
    log.info("updated debt position id[{}]", persistedDebtPosition.getDebtPositionId());
    //start debt position workflow
    invokeWorkflow(persistedDebtPosition, accessToken);
  }

}
