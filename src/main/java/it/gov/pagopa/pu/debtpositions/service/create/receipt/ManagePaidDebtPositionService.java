package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ManagePaidDebtPositionService {

  private final OrganizationService organizationService;
  private final PrimaryOrgInstallmentPaidVerifierService primaryOrgInstallmentPaidVerifierService;
  private final InstallmentUpdateService installmentUpdateService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final DebtPositionService debtPositionService;
  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;
  private final PaymentsProducerService paymentsProducerService;
  private final ReceiptWithAdditionalInfoMapper receiptWithAdditionalInfoMapper;

  public ManagePaidDebtPositionService(OrganizationService organizationService,
                                       PrimaryOrgInstallmentPaidVerifierService primaryOrgInstallmentPaidVerifierService,
                                       InstallmentUpdateService installmentUpdateService,
                                       DebtPositionSyncService debtPositionSyncService,
                                       DebtPositionService debtPositionService,
                                       DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService,
                                       PaymentsProducerService paymentsProducerService,
                                       ReceiptWithAdditionalInfoMapper receiptWithAdditionalInfoMapper) {
    this.organizationService = organizationService;
    this.primaryOrgInstallmentPaidVerifierService = primaryOrgInstallmentPaidVerifierService;
    this.installmentUpdateService = installmentUpdateService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionService = debtPositionService;
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.paymentsProducerService = paymentsProducerService;
    this.receiptWithAdditionalInfoMapper = receiptWithAdditionalInfoMapper;
  }

  boolean handleReceiptReceivedPrimaryOrg(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    return organizationService.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken)
      .map(primaryOrg -> {
        Pair<Optional<InstallmentNoPII>, Boolean> installmentAndPrimaryOrgFound = primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(primaryOrg, receiptDTO.getNoticeNumber());
        installmentAndPrimaryOrgFound.getLeft().ifPresent(installment -> setInstallmentAsPaid(installment, receiptDTO, accessToken, primaryOrg));
        return installmentAndPrimaryOrgFound.getRight();
      })
      .orElse(false);
  }

  private void invokeWorkflow(DebtPositionDTO debtPositionDTO, String accessToken) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      WorkflowCreatedDTO workflow = debtPositionSyncService.syncDebtPosition(debtPositionDTO, new WfExecutionParameters(), PaymentEventType.RT_RECEIVED, accessToken);
      if (workflow != null) {
        log.info("Workflow creation OK for debtPositionId[{}}: workflowId[{}]", debtPositionDTO.getDebtPositionId(), workflow.getWorkflowId());
      } else {
        log.warn("Workflow creation KO for debtPositionId[{}]: received null response", debtPositionDTO.getDebtPositionId());
      }
    }
  }

  private void setInstallmentAsPaid(InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken, Organization organization) {
    log.debug("primaryOrg installment found id[{}]", installment.getInstallmentId());
    //update installment status
    DebtPosition debtPosition = installmentUpdateService.updateInstallmentStatusOfDebtPosition(installment, receiptDTO);
    //align debt position status
    DebtPositionDTO debtPositionDTO = debtPositionHierarchyStatusAlignerService.alignHierarchyStatusAndRemap(debtPosition);
    //persist updated debt position
    DebtPositionDTO persistedDebtPosition = persistDebtPosition(debtPositionDTO, organization);
    //start debt position workflow
    invokeWorkflow(persistedDebtPosition, accessToken);
  }

  private DebtPositionDTO persistDebtPosition(DebtPositionDTO debtPositionDTO, Organization organization) {
    //persist updated debt position
    DebtPositionDTO persistedDebtPosition = debtPositionService.saveDebtPositionAndRemap(debtPositionDTO, organization);
    log.info("updated debt position id[{}]", persistedDebtPosition.getDebtPositionId());
    return persistedDebtPosition;
  }

  void persistTechnicalDebtPositionFromReceiptAndNotifyEvent(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization) {
    log.info("Creating technical debt position from receipt[{} - {}/{}] for organization [{}/{}]",
      receiptDTO.getReceiptId(), receiptDTO.getOrgFiscalCode(), receiptDTO.getNoticeNumber(),
      organization.getOrganizationId(), organization.getOrgFiscalCode());
    DebtPositionDTO debtPositionDTO = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptDTO, organization);
    DebtPositionDTO createdDebtPositionDTO = persistDebtPosition(debtPositionDTO, organization);
    //notify payment event
    paymentsProducerService.notifyPaymentsEvent(createdDebtPositionDTO, PaymentEventType.RT_RECEIVED);
    log.info("Technical debt position created with id [{}]", createdDebtPositionDTO.getDebtPositionId());
  }

}
