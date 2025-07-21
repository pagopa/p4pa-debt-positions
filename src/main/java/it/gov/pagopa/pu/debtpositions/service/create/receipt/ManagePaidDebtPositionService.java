package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
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
  private final DebtPositionProcessorService debtPositionProcessorService;

  public ManagePaidDebtPositionService(OrganizationService organizationService,
                                       PrimaryOrgInstallmentPaidVerifierService primaryOrgInstallmentPaidVerifierService,
                                       InstallmentUpdateService installmentUpdateService,
                                       DebtPositionSyncService debtPositionSyncService,
                                       DebtPositionService debtPositionService,
                                       DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService,
                                       PaymentsProducerService paymentsProducerService,
                                       ReceiptWithAdditionalInfoMapper receiptWithAdditionalInfoMapper,
    DebtPositionProcessorService debtPositionProcessorService) {
    this.organizationService = organizationService;
    this.primaryOrgInstallmentPaidVerifierService = primaryOrgInstallmentPaidVerifierService;
    this.installmentUpdateService = installmentUpdateService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionService = debtPositionService;
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.paymentsProducerService = paymentsProducerService;
    this.receiptWithAdditionalInfoMapper = receiptWithAdditionalInfoMapper;
    this.debtPositionProcessorService = debtPositionProcessorService;
  }

  boolean handleReceiptReceivedPrimaryOrg(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    return organizationService.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken)
      .map(primaryOrg -> {
        Pair<Optional<InstallmentNoPII>, Boolean> installmentAndPrimaryOrgFound = primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(primaryOrg, receiptDTO.getNoticeNumber(), receiptDTO.getIud());
        installmentAndPrimaryOrgFound.getLeft().ifPresent(installment -> setInstallmentAsPaid(installment, receiptDTO, accessToken));
        return installmentAndPrimaryOrgFound.getRight();
      })
      .orElse(false);
  }

  private void invokeWorkflow(DebtPositionDTO debtPositionDTO, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
    WorkflowCreatedDTO workflow = debtPositionSyncService.syncDebtPosition(debtPositionDTO, new WfExecutionParameters(), PaymentEventType.RT_RECEIVED, buildPaymentEventDescription(receiptDTO), accessToken);
    if (workflow != null) {
      log.info("Workflow creation OK for debtPositionId[{}}: workflowId[{}] runId[{}]", debtPositionDTO.getDebtPositionId(), workflow.getWorkflowId(), workflow.getRunId());
    } else {
      log.warn("Workflow creation KO for debtPositionId[{}]: received null response", debtPositionDTO.getDebtPositionId());
    }
  }

  private void setInstallmentAsPaid(InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    log.debug("primaryOrg installment found id[{}]", installment.getInstallmentId());
    //update installment status
    DebtPosition debtPosition = installmentUpdateService.updateInstallmentStatusOfDebtPosition(installment, receiptDTO, accessToken);
    //update amounts
    debtPositionProcessorService.updateAmounts(debtPosition);
    //align debt position status
    debtPositionHierarchyStatusAlignerService.alignHierarchyStatus(debtPosition);
    //persist updated debt position
    debtPositionService.saveDebtPosition(debtPosition);
    //start debt position workflow
    invokeWorkflow(debtPositionService.mapDebtPosition(debtPosition), receiptDTO, accessToken);
  }

  void persistTechnicalDebtPositionFromReceiptAndNotifyEvent(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization) {
    log.info("Creating technical debt position from receipt[{} - {}/{}] for organization [{}/{}]",
      receiptDTO.getReceiptId(), receiptDTO.getOrgFiscalCode(), receiptDTO.getNoticeNumber(),
      organization.getOrganizationId(), organization.getOrgFiscalCode());
    DebtPositionDTO debtPositionDTO = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptDTO, organization);
    debtPositionService.saveDebtPosition(debtPositionDTO);
    //notify payment event
    paymentsProducerService.notifyPaymentsEvent(debtPositionDTO, PaymentEventType.RT_RECEIVED, buildPaymentEventDescription(receiptDTO));
    log.info("Technical debt position created with id [{}]", debtPositionDTO.getDebtPositionId());
  }

  private String buildPaymentEventDescription(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    return "receiptId:" + receiptDTO.getReceiptId();
  }

}
