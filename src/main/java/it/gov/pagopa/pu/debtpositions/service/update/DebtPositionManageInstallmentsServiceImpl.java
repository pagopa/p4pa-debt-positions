package it.gov.pagopa.pu.debtpositions.service.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowHubService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.exception.custom.WorkflowErrorException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.service.update.applier.DebtPositionManageApplierService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerErrorException;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DebtPositionManageInstallmentsServiceImpl extends BaseDebtPositionOperationService implements DebtPositionManageInstallmentsService {

  private final DebtPositionService debtPositionService;
  private final DebtPositionManageApplierService debtPositionManageApplierService;
  private final DebtPositionAddInstallmentService debtPositionAddInstallmentService;
  private final DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService;
  private final DebtPositionCancelInstallmentService debtPositionCancelInstallmentService;
  private final ValidateDebtPositionService validateDebtPositionService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final WorkflowHubService workflowHubService;
  private final int maxAttempts;
  private final int retryDelayMs;
  private final ObjectMapper objectMapper;

  private static final String WORKFLOW_STATUS_COMPLETED_VALUE = "WORKFLOW_EXECUTION_STATUS_COMPLETED";

  protected DebtPositionManageInstallmentsServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                                      DebtPositionService debtPositionService,
                                                      DebtPositionSyncService debtPositionSyncService,
                                                      DebtPositionProcessorService debtPositionProcessorService,
                                                      OrganizationService organizationService,
                                                      DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService,
                                                      DebtPositionManageApplierService debtPositionManageApplierService,
                                                      DebtPositionAddInstallmentService debtPositionAddInstallmentService,
                                                      DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService,
                                                      DebtPositionCancelInstallmentService debtPositionCancelInstallmentService, ValidateDebtPositionService validateDebtPositionService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, WorkflowHubService workflowHubService,
                                                      @Value("${wf-await.max-waiting-minutes}") int maxWaitingMinutes,
                                                      @Value("${wf-await.retry-delays-ms}") int retryDelayMs, ObjectMapper objectMapper) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService, debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
    this.debtPositionService = debtPositionService;
    this.debtPositionManageApplierService = debtPositionManageApplierService;
    this.debtPositionAddInstallmentService = debtPositionAddInstallmentService;
    this.debtPositionUpdateInstallmentService = debtPositionUpdateInstallmentService;
    this.debtPositionCancelInstallmentService = debtPositionCancelInstallmentService;
    this.validateDebtPositionService = validateDebtPositionService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.workflowHubService = workflowHubService;
    this.retryDelayMs = retryDelayMs;
    this.objectMapper = objectMapper;
    this.maxAttempts = (int) (((double) maxWaitingMinutes * 60_000) / retryDelayMs);
  }

  @Override
  @Transactional
  public Pair<DebtPositionDTO, WorkflowCreatedDTO> manageDebtPositionInstallments(Long debtPositionId, ManageDebtPositionDTO manageDebtPositionDTO, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    DebtPositionDTO storedDebtPosition = debtPositionService.getDebtPosition(debtPositionId);

    if (!InstallmentUtils.MODIFIABLE_DP_STATUSES.contains(storedDebtPosition.getStatus())) {
      throw new ConflictErrorException(String.format("Debt position with id %s cannot be modified because it is not in an allowed status: %s",
        debtPositionId, storedDebtPosition.getStatus()));
    }

    storedDebtPosition.setDescription(manageDebtPositionDTO.getDebtPositionDescription());
    storedDebtPosition.setValidityDate(manageDebtPositionDTO.getValidityDate());

    PaymentOptionDTO storedPaymentOption = storedDebtPosition.getPaymentOptions().stream()
      .filter(paymentOptionDTO -> paymentOptionDTO.getPaymentOptionId().equals(manageDebtPositionDTO.getPaymentOptionId()))
      .findFirst()
      .orElseThrow(() -> new NotFoundException(String.format("Payment option having id %s not found", manageDebtPositionDTO.getPaymentOptionId())));

    if (!InstallmentUtils.MODIFIABLE_PO_STATUSES.contains(storedPaymentOption.getStatus())) {
      throw new ConflictErrorException(String.format("Payment option having id %s cannot be modified because is not in allowed status: %s", storedPaymentOption.getPaymentOptionId(), storedPaymentOption.getStatus()));
    }

    storedPaymentOption.setDescription(manageDebtPositionDTO.getPaymentOptionDescription());

    List<InstallmentDTO> involvedInstallments = managePaymentOptionInstallments(storedDebtPosition, storedPaymentOption, manageDebtPositionDTO.getInstallments(), accessToken, operatorExternalUserId);

    WorkflowCreatedDTO workflow = invokeWorkflow(storedDebtPosition, PaymentEventType.DP_UPDATED, involvedInstallments, accessToken, wfExecutionParameters);

    log.debug("Managed installments for debt position with id {}", storedDebtPosition.getDebtPositionId());
    return Pair.of(storedDebtPosition, workflow);
  }

  private List<InstallmentDTO> managePaymentOptionInstallments(DebtPositionDTO debtPositionDTO, PaymentOptionDTO paymentOptionDTO, List<ManageInstallmentDTO> manageInstallments, String accessToken, String operatorExternalUserId) {
    ArrayList<InstallmentDTO> installmentsToAdd = new ArrayList<>();
    ArrayList<InstallmentDTO> installmentsToUpdate = new ArrayList<>();
    ArrayList<InstallmentDTO> installmentsToCancel = new ArrayList<>();

    manageInstallments.forEach(
      manageInstallmentDTO -> {
        InstallmentDTO manageInstallment = manageInstallmentDTO.getInstallment();
        switch (manageInstallmentDTO.getAction()) {
          case I -> {
            paymentOptionDTO.addInstallmentsItem(manageInstallment);
            installmentsToAdd.add(manageInstallment);
          }
          case M -> {
            InstallmentDTO storedInstallment = findInstallmentToManage(paymentOptionDTO, manageInstallment.getInstallmentId());
            InstallmentDTO dryStoredInstallment;

            try {
              String json = objectMapper.writeValueAsString(storedInstallment);
              dryStoredInstallment = objectMapper.readValue(json, InstallmentDTO.class);
            } catch (JsonProcessingException e) {
              throw new ServerErrorException("Error cloning installment", e);
            }

            debtPositionManageApplierService.merge(manageInstallment, dryStoredInstallment);

            DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId())
              .orElseThrow(() -> new NotFoundException(String.format("The debt position type org with id %s was not found for organization id %s",
                debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getOrganizationId())));

            validateDebtPositionService.validateInstallment(
              dryStoredInstallment,
              accessToken,
              debtPositionTypeOrg,
              debtPositionDTO.getDebtPositionOrigin(),
              debtPositionDTO.getFlagPuPagoPaPayment()
            );

            debtPositionManageApplierService.merge(manageInstallment, storedInstallment);
            installmentsToUpdate.add(storedInstallment);
          }
          case A -> {
            InstallmentDTO storedInstallment = findInstallmentToManage(paymentOptionDTO, manageInstallment.getInstallmentId());
            installmentsToCancel.add(storedInstallment);
          }
        }
      }
    );

    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder().massive(false).partialChange(true).build();

    if (!installmentsToAdd.isEmpty()) {
      WorkflowCreatedDTO workflowCreated = debtPositionAddInstallmentService.addInstallment(debtPositionDTO, installmentsToAdd, wfExecutionParameters, accessToken, operatorExternalUserId);
      checkWorkflowIsCompleted(workflowCreated, accessToken);
    }
    if (!installmentsToUpdate.isEmpty()) {
      WorkflowCreatedDTO workflowCreated = debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, installmentsToUpdate, wfExecutionParameters, accessToken, operatorExternalUserId);
      checkWorkflowIsCompleted(workflowCreated, accessToken);
    }
    if (!installmentsToCancel.isEmpty()) {
      WorkflowCreatedDTO workflowCreated = debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, installmentsToCancel, wfExecutionParameters, accessToken, operatorExternalUserId);
      checkWorkflowIsCompleted(workflowCreated, accessToken);
    }

    List<InstallmentDTO> installments2operate = new ArrayList<>(manageInstallments.size());
    installments2operate.addAll(installmentsToAdd);
    installments2operate.addAll(installmentsToUpdate);
    installments2operate.addAll(installmentsToCancel);

    return installments2operate;
  }

  private InstallmentDTO findInstallmentToManage(PaymentOptionDTO paymentOptionDTO, Long installmentId) {
    InstallmentDTO installment = paymentOptionDTO.getInstallments().stream()
      .filter(installmentDTO -> installmentDTO.getInstallmentId().equals(installmentId))
      .findFirst()
      .orElseThrow(() -> new NotFoundException(String.format("The installment with id %s not found", installmentId)));

    if (!InstallmentUtils.MODIFIABLE_STATUSES.contains(installment.getStatus())) {
      throw new ConflictErrorException(String.format("Installment having id %s cannot be modified because is not in allowed status: %s", installmentId, installment.getStatus()));
    }

    if (StringUtils.isNotBlank(installment.getIun())) {
      throw new ConflictErrorException("The installment with id " + installment.getInstallmentId() + " cannot be modified because is been notified by SEND");
    }

    return installment;
  }

  private void checkWorkflowIsCompleted(WorkflowCreatedDTO workflowCreatedDTO, String accessToken) {
    if (workflowCreatedDTO != null) {
      String workflowStatus = workflowHubService.waitWorkflowCompletion(accessToken, workflowCreatedDTO.getWorkflowId(), maxAttempts, retryDelayMs);
      if (!WORKFLOW_STATUS_COMPLETED_VALUE.equals(workflowStatus)) {
        throw new WorkflowErrorException("Workflow with id " + workflowCreatedDTO.getWorkflowId() + " terminated with error");
      }
    }
  }

  @Override
  protected void applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
    // Do Nothing, operations are demanded to the invoked services
  }

  @Override
  protected boolean isDebtPositionTypeOrgDisabledAllowed() {
    return true;
  }
}
