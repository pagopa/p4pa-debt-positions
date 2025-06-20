package it.gov.pagopa.pu.debtpositions.service.update;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.delete.DebtPositionDeletionService;
import it.gov.pagopa.pu.debtpositions.service.delete.InstallmentDeletionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DebtPositionCancelInstallmentServiceImpl extends BaseDebtPositionOperationService implements DebtPositionCancelInstallmentService {

  private final InstallmentDeletionService installmentDeletionService;
  private final DebtPositionDeletionService debtPositionDeletionService;

  protected DebtPositionCancelInstallmentServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                                     DebtPositionService debtPositionService,
                                                     DebtPositionSyncService debtPositionSyncService,
                                                     DebtPositionProcessorService debtPositionProcessorService,
                                                     OrganizationService organizationService,
                                                     DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService,
                                                     InstallmentDeletionService installmentDeletionService, DebtPositionDeletionService debtPositionDeletionService) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService, debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
    this.installmentDeletionService = installmentDeletionService;
    this.debtPositionDeletionService = debtPositionDeletionService;
  }

  @Transactional
  @Override
  public WorkflowCreatedDTO cancelInstallment(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    Set<Long> installmentIds = installments2operate.stream().map(InstallmentDTO::getInstallmentId).collect(Collectors.toSet());

    if (log.isDebugEnabled()) {
      log.debug("Cancelling installments with ids {} for debt position with id {}", installmentIds, debtPositionDTO.getDebtPositionId());
    }

    if (DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      boolean allInstallmentsToBeDeleted = installmentIds.size() ==
        debtPositionDTO.getPaymentOptions().stream()
          .mapToLong(po -> po.getInstallments().size())
          .sum();

      if (allInstallmentsToBeDeleted) {
        return debtPositionDeletionService.deleteDebtPosition(debtPositionDTO.getDebtPositionId(), accessToken, operatorExternalUserId);
      }
    }

    WorkflowCreatedDTO workflow = execute(debtPositionDTO, installments2operate, wfExecutionParameters, PaymentEventType.DPI_CANCELLED, accessToken, operatorExternalUserId);

    log.debug("Cancelled installments for debt position with id {}", debtPositionDTO.getDebtPositionId());
    return workflow;
  }

  @Override
  protected void applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
    installments2operate = installments2operate.stream()
      .filter(installmentDTO -> !InstallmentStatus.CANCELLED.equals(installmentDTO.getStatus()))
      .map(installmentDTO -> {
        if (!InstallmentUtils.MODIFIABLE_STATUSES.contains(installmentDTO.getStatus())) {
          throw new ConflictErrorException("The installment with id " + installmentDTO.getInstallmentId() + " cannot be cancelled because is not in allowed status: " + installmentDTO.getStatus());
        }
        if (StringUtils.isNotBlank(installmentDTO.getIun())) {
          throw new ConflictErrorException("The installment with id " + installmentDTO.getInstallmentId() + " cannot be cancelled because is been notified by SEND");
        }
        return installmentDTO;
      })
      .toList();

    Set<Long> installmentIds = installments2operate.stream().map(InstallmentDTO::getInstallmentId).collect(Collectors.toSet());

    if (DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.debug("Deleting draft installments with ids {}", installmentIds);
      installmentDeletionService.deleteDraftInstallments(debtPositionDTO, installmentIds);
    } else {
      log.debug("Updating status cancelled for installments with ids {}", installmentIds);
      debtPositionDTO.getPaymentOptions()
        .forEach(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream()
          .filter(installmentDTO -> installmentIds.contains(installmentDTO.getInstallmentId()))
          .forEach(installmentDTO -> InstallmentUtils.setStatus(installmentDTO, InstallmentStatus.CANCELLED))
        );
    }
  }


  @Override
  protected void saveDebtPosition(DebtPositionDTO debtPositionDTO) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      super.saveDebtPosition(debtPositionDTO);
    }
  }

  @Override
  protected void alignHierarchyStatus(DebtPositionDTO debtPositionDTO) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      super.alignHierarchyStatus(debtPositionDTO);
    }
  }
}
