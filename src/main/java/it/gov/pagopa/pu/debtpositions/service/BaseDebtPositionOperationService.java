package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public abstract class BaseDebtPositionOperationService {

  private final AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;
  private final DebtPositionService debtPositionService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final DebtPositionProcessorService debtPositionProcessorService;
  private final OrganizationService organizationService;
  private final DebtPositionMapper debtPositionMapper;
  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  protected BaseDebtPositionOperationService(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService, DebtPositionService debtPositionService, DebtPositionSyncService debtPositionSyncService, DebtPositionProcessorService debtPositionProcessorService, OrganizationService organizationService, DebtPositionMapper debtPositionMapper, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService) {
    this.authorizeOperatorOnDebtPositionTypeService = authorizeOperatorOnDebtPositionTypeService;
    this.debtPositionService = debtPositionService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.organizationService = organizationService;
    this.debtPositionMapper = debtPositionMapper;
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
   * @param massive indicates that the operation is massive or single
   * @param accessToken the access token
   * @param operatorExternalUserId the operator who requested the creation
   * @return the {@link DebtPositionDTO} created and WorkflowId of debt position synchronization
   */

  @Transactional
  public Pair<DebtPositionDTO, String> execute(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate,
                                               Boolean massive, PaymentEventType eventType,
                                               String accessToken, String operatorExternalUserId) {
    Organization org = organizationService.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken).orElseThrow(() -> new InvalidValueException("Provided organization id not found on db."));
    authorizeOperatorOnDebtPositionTypeService.authorize(debtPositionDTO.getDebtPositionTypeOrgId(), operatorExternalUserId);

    DebtPositionDTO debtPositionOperated = applyOperation(debtPositionDTO, installments2operate, accessToken, org);

    DebtPositionDTO debtPositionUpdated = debtPositionProcessorService.updateAmounts(debtPositionOperated);
    org.springframework.data.util.Pair<DebtPosition, DebtPositionDTO> savedDebtPosition = debtPositionService.saveDebtPosition(debtPositionUpdated, org);

    //DebtPosition debtPosition = debtPositionMapper.mapToModel(savedDebtPosition).getFirst();
    DebtPositionDTO debtPositionAligned = debtPositionHierarchyStatusAlignerService.alignHierarchyStatus(savedDebtPosition.getFirst());

    String workflowId = invokeWorkflow(debtPositionAligned, eventType, accessToken, massive);

    return Pair.of(debtPositionAligned, workflowId);
  }

  private String invokeWorkflow(DebtPositionDTO debtPositionDTO, PaymentEventType eventType, String accessToken, Boolean massive) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      WorkflowCreatedDTO workflowCreatedDTO = debtPositionSyncService.syncDebtPosition(debtPositionDTO, massive, eventType, accessToken);
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

  public void setSyncStatus(DebtPositionDTO debtPositionDTO, Set<Long> installmentIds, InstallmentStatus statusTo) {
    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream()
        .filter(installmentDTO -> installmentIds.contains(installmentDTO.getInstallmentId()))
        .findFirst()
        .ifPresent(installmentDTO -> {
            installmentDTO.setSyncStatus(new InstallmentSyncStatus(installmentDTO.getStatus(), statusTo));
            installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
          }
        ));
  }
}
