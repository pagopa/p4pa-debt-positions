package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.service.update.TechnicalMixedDebtPositionUpdaterService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
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
  private final TechnicalMixedDebtPositionUpdaterService technicalMixedDebtPositionUpdaterService;
  private final DebtPositionMapper debtPositionMapper;

  public ManagePaidDebtPositionService(
    OrganizationService organizationService,
    PrimaryOrgInstallmentPaidVerifierService primaryOrgInstallmentPaidVerifierService,
    InstallmentUpdateService installmentUpdateService,
    DebtPositionSyncService debtPositionSyncService,
    DebtPositionService debtPositionService,
    DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService,
    PaymentsProducerService paymentsProducerService,
    ReceiptWithAdditionalInfoMapper receiptWithAdditionalInfoMapper,
    DebtPositionProcessorService debtPositionProcessorService,
    TechnicalMixedDebtPositionUpdaterService technicalMixedDebtPositionUpdaterService,
    DebtPositionMapper debtPositionMapper
  ) {
    this.organizationService = organizationService;
    this.primaryOrgInstallmentPaidVerifierService = primaryOrgInstallmentPaidVerifierService;
    this.installmentUpdateService = installmentUpdateService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionService = debtPositionService;
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.paymentsProducerService = paymentsProducerService;
    this.receiptWithAdditionalInfoMapper = receiptWithAdditionalInfoMapper;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.technicalMixedDebtPositionUpdaterService = technicalMixedDebtPositionUpdaterService;
    this.debtPositionMapper = debtPositionMapper;
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
    // handle mixed technical debt positions
    List<DebtPosition> newMixedTechnicalDebtPositions = technicalMixedDebtPositionUpdaterService.update(debtPosition, accessToken);
    for (DebtPosition newMixedTechnicalDebtPosition :  newMixedTechnicalDebtPositions) {
      saveAndNotifyDebtPosition(this.debtPositionMapper.mapToDto(newMixedTechnicalDebtPosition), receiptDTO);
    }
    //start debt position workflow
    invokeWorkflow(debtPositionService.mapDebtPosition(debtPosition), receiptDTO, accessToken);
  }

  void persistTechnicalDebtPositionFromReceiptAndNotifyEvent(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization) {
    log.info("Creating technical debt position from receipt[{} - {}/{} - {}] for organization [{}/{}]",
      receiptDTO.getReceiptId(),
      receiptDTO.getOrgFiscalCode(), receiptDTO.getNoticeNumber(),
      receiptDTO.getIud(),
      organization.getOrganizationId(), organization.getOrgFiscalCode());
    DebtPositionDTO debtPositionDTO = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptDTO, organization);
    Optional<DebtPosition> existingDebtPosition = debtPositionService.getDebtPositionByIupdAndOrganizationId(
      debtPositionDTO.getIupdOrg(),
      debtPositionDTO.getOrganizationId()
    );

    if (existingDebtPosition.isEmpty()) {
      saveAndNotifyDebtPosition(debtPositionDTO, receiptDTO);
    } else {
      updateAndNotifyDebtPosition(existingDebtPosition.get().getDebtPositionId(), debtPositionDTO, receiptDTO);
    }
  }

  private String buildPaymentEventDescription(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    return "receiptId:" + receiptDTO.getReceiptId();
  }

  private void updateAndNotifyDebtPosition(Long debtPositionId, DebtPositionDTO debtPositionDTO, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    debtPositionService.updateDebtPosition(debtPositionId, debtPositionDTO);
    notifyPayment(debtPositionDTO, receiptDTO);
    log.info("Technical debt position updated with id [{}]", debtPositionDTO.getDebtPositionId());
  }

  private void saveAndNotifyDebtPosition(DebtPositionDTO debtPositionDTO, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    debtPositionService.saveDebtPosition(debtPositionDTO);
    notifyPayment(debtPositionDTO, receiptDTO);
    log.info("Technical debt position created with id [{}]", debtPositionDTO.getDebtPositionId());
  }

  private void notifyPayment(DebtPositionDTO debtPositionDTO, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    //notify payment event
    paymentsProducerService.notifyPaymentsEvent(debtPositionDTO, PaymentEventType.RT_RECEIVED, buildPaymentEventDescription(receiptDTO));
  }

}
