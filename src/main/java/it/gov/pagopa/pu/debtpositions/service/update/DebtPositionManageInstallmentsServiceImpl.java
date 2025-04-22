package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.service.update.applier.DebtPositionManageApplierService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

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

  protected DebtPositionManageInstallmentsServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService, DebtPositionService debtPositionService, DebtPositionSyncService debtPositionSyncService, DebtPositionProcessorService debtPositionProcessorService, OrganizationService organizationService, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, DebtPositionManageApplierService debtPositionManageApplierService, DebtPositionAddInstallmentService debtPositionAddInstallmentService, DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService, DebtPositionCancelInstallmentService debtPositionCancelInstallmentService) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService, debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
    this.debtPositionService = debtPositionService;
    this.debtPositionManageApplierService = debtPositionManageApplierService;
    this.debtPositionAddInstallmentService = debtPositionAddInstallmentService;
    this.debtPositionUpdateInstallmentService = debtPositionUpdateInstallmentService;
    this.debtPositionCancelInstallmentService = debtPositionCancelInstallmentService;
  }

  @Override
  @Transactional
  public Pair<DebtPositionDTO, String> manageDebtPositionInstallments(Long debtPositionId, ManageDebtPositionDTO manageDebtPositionDTO, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
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

    String workflowId = invokeWorkflow(storedDebtPosition, PaymentEventType.DP_UPDATED, involvedInstallments, accessToken, wfExecutionParameters);

    log.debug("Managed installments for debt position with id {}", storedDebtPosition.getDebtPositionId());
    return Pair.of(storedDebtPosition, workflowId);
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

    // TODO task P4ADEV-2669: add check wait for workflow completion
    if(!installmentsToAdd.isEmpty()){
      debtPositionAddInstallmentService.addInstallment(debtPositionDTO, installmentsToAdd, wfExecutionParameters, accessToken, operatorExternalUserId);
    }
    if(!installmentsToUpdate.isEmpty()){
      debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, installmentsToUpdate, wfExecutionParameters, accessToken, operatorExternalUserId);
    }
    if(!installmentsToCancel.isEmpty()){
      debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, installmentsToCancel, wfExecutionParameters, accessToken, operatorExternalUserId);
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
    return installment;
  }

  @Override
  protected void applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
    // Do Nothing, operations are demanded to the invoked services
  }
}
