package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public abstract class BaseDebtPositionOperationService {

  private final AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final DebtPositionProcessorService debtPositionProcessorService;
  private final OrganizationService organizationService;
  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  protected final DebtPositionService debtPositionService;

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
   * @return the WorkflowId of debt position synchronization
   */

  @Transactional
  public WorkflowCreatedDTO execute(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate,
                                               WfExecutionParameters wfExecutionParameters, PaymentEventType eventType,
                                               String accessToken, String operatorExternalUserId) {
    Organization org = organizationService.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken).orElseThrow(() -> new InvalidValueException("Provided organization id not found on db."));
    if(!OrganizationStatus.ACTIVE.equals(org.getStatus())){
      throw new InvalidValueException("Provided organization is not ACTIVE");
    }
    DebtPositionTypeOrg debtPositionTypeOrg = authorizeOperatorOnDebtPositionTypeService.authorize(org.getIpaCode(), debtPositionDTO.getDebtPositionTypeOrgId(), operatorExternalUserId);

    if (!isDisableDebtPositionTypeOrgAllowed(debtPositionTypeOrg)) {
      throw new OperatorNotAuthorizedException("The operator " + operatorExternalUserId + " is not authorized on the DebtPositionTypeOrg " + debtPositionDTO.getDebtPositionTypeOrgId() + " because it is inactive");
    }

    applyOperation(debtPositionDTO, installments2operate, accessToken, org);

    debtPositionProcessorService.updateAmounts(debtPositionDTO);

    saveAndAlignHierarchyStatus(debtPositionDTO);

    return invokeWorkflow(debtPositionDTO, eventType, installments2operate, accessToken, wfExecutionParameters);
  }

  protected void saveAndAlignHierarchyStatus(DebtPositionDTO debtPositionDTO) {
    debtPositionService.saveDebtPosition(debtPositionDTO);
    debtPositionHierarchyStatusAlignerService.alignHierarchyStatus(debtPositionDTO);
  }

  protected WorkflowCreatedDTO invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventType eventType, List<InstallmentDTO> installments2operate, String accessToken, WfExecutionParameters wfExecutionParameters) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      return debtPositionSyncService.syncDebtPosition(
        debtPositionDTO,
        wfExecutionParameters,
        eventType,
        "IUD:" + installments2operate.stream().map(InstallmentDTO::getIud).collect(Collectors.joining(",")),
        accessToken);
    }
    return null;
  }

  /**
   * It checks if the operator operates on an inactive DebtPositionTypeOrg.
   *
   * @param debtPositionTypeOrg the DebtPositionTypeOrg to check
   * @return true if the DebtPositionTypeOrg can be handled, false otherwise
   */
  protected boolean isDisableDebtPositionTypeOrgAllowed(DebtPositionTypeOrg debtPositionTypeOrg) {
    // Not overridden, so it is always allowed
    return true;
  }

  /**
   * It will set TO_SYNC and syncStatus to the involved installments
   *
   * @param debtPositionDTO      the debt position to operate on
   * @param installments2operate the involved installments
   * @param accessToken          the access token
   * @param org                  the organization related to debt position
   */
  protected abstract void applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org);

}
