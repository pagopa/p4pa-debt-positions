package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public abstract class BaseDebtPositionOperationService {

  private final AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;
  private final DebtPositionService debtPositionService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final DebtPositionProcessorService debtPositionProcessorService;
  private final OrganizationService organizationService;
  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  protected BaseDebtPositionOperationService(
    AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
    DebtPositionService debtPositionService,
    DebtPositionSyncService debtPositionSyncService,
    DebtPositionProcessorService debtPositionProcessorService,
    OrganizationService organizationService,
    DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService
  ) {
    this.authorizeOperatorOnDebtPositionTypeService = authorizeOperatorOnDebtPositionTypeService;
    this.debtPositionService = debtPositionService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.organizationService = organizationService;
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
  }

  /***
   * It will:
   * <ol>
   *    <li>Authorize the operation
   *    <li>Call the {@link #applyOperation} method
   *    <li>Update amounts
   *    <li>Save the DebtPositionDTO
   *    <li>Align hierarchy status
   *    <li>Invoke workflow
   * </ol>
   *
   * @param debtPositionDTO the debt position to operate on
   * @param installments2operate the list of {@link InstallmentDTO} included in the Debt Position involved in the operation (use same objects! it will be used == operator to identify them)
   * @param wfExecutionParameters wf execution parameters
   * @param accessToken the access token
   * @param operatorExternalUserId the operator who requested the creation
   * @return the {@link DebtPositionDTO} created and WorkflowId of debt position synchronization
   */

  @Transactional
  public Pair<DebtPositionDTO, String> execute(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate,
                                               WfExecutionParameters wfExecutionParameters, PaymentEventType eventType,
                                               String accessToken, String operatorExternalUserId) {
    Organization org = organizationService.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken).orElseThrow(() -> new InvalidValueException("Provided organization id not found on db."));
    if(!OrganizationStatus.ACTIVE.equals(org.getStatus())){
      throw new InvalidValueException("Provided organization is not ACTIVE");
    }
    authorizeOperatorOnDebtPositionTypeService.authorize(debtPositionDTO.getDebtPositionTypeOrgId(), operatorExternalUserId);

    DebtPositionDTO debtPositionOperated = applyOperation(debtPositionDTO, installments2operate, accessToken, org);

    DebtPositionDTO debtPositionUpdated = debtPositionProcessorService.updateAmounts(debtPositionOperated);
    DebtPosition savedDebtPosition = debtPositionService.saveDebtPosition(debtPositionUpdated, org);

    DebtPositionDTO debtPositionAligned = debtPositionHierarchyStatusAlignerService.alignHierarchyStatusAndRemap(savedDebtPosition);

    String workflowId = invokeWorkflow(debtPositionAligned, eventType, accessToken, wfExecutionParameters);

    return Pair.of(debtPositionAligned, workflowId);
  }

  private String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventType eventType, String accessToken, WfExecutionParameters wfExecutionParameters) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      WorkflowCreatedDTO workflowCreatedDTO = debtPositionSyncService.syncDebtPosition(debtPositionDTO, wfExecutionParameters, eventType, accessToken);
      if (workflowCreatedDTO != null) {
        return workflowCreatedDTO.getWorkflowId();
      }
    }
    return null;
  }

  /**
   * It will set TO_SYNC and syncStatus to the involved installments
   *
   * @param debtPositionDTO      the debt position to operate on
   * @param installments2operate the involved installments
   * @param accessToken          the access token
   * @param org                  the organization related to debt position
   * @return the {@link DebtPositionDTO} updated
   */
  public abstract DebtPositionDTO applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org);

}
