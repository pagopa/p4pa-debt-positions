package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionCancelInstallmentServiceImpl;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class DebtPositionDeletionServiceImpl implements DebtPositionDeletionService {

  private final DebtPositionService debtPositionService;
  private final OrganizationService organizationService;
  private final AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;
  private final DebtPositionCancelInstallmentServiceImpl debtPositionCancelInstallmentService;

  public DebtPositionDeletionServiceImpl(DebtPositionService debtPositionService, OrganizationService organizationService, AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService, DebtPositionCancelInstallmentServiceImpl debtPositionCancelInstallmentService) {
    this.debtPositionService = debtPositionService;
    this.organizationService = organizationService;
    this.authorizeOperatorOnDebtPositionTypeService = authorizeOperatorOnDebtPositionTypeService;
    this.debtPositionCancelInstallmentService = debtPositionCancelInstallmentService;
  }

  @Override
  @Transactional
  public WorkflowCreatedDTO deleteDebtPosition(Long debtPositionId, String accessToken, String operatorExternalUserId) {
    log.info("Cancelling debt position having id {}", debtPositionId);

    DebtPosition debtPosition = debtPositionService.getDebtPositionNoPII(debtPositionId);

    if (DebtPositionStatus.DRAFT.equals(debtPosition.getStatus())) {
      deleteDraftDebtPosition(debtPosition, accessToken, operatorExternalUserId);
      return null;
    }

    if(isIunPresent(debtPosition)) {
      throw new ConflictErrorException("INVALID_DEBT_POSITION_STATUS", "DebtPosition with id " + debtPositionId + " cannot be deleted because it is been notified");
    }

    if(!InstallmentUtils.DELETABLE_DP_STATUSES.contains(debtPosition.getStatus())){
      throw new ConflictErrorException("INVALID_DEBT_POSITION_STATUS", "DebtPosition with id " + debtPositionId + " cannot be deleted because is not in allowed status: " + debtPosition.getStatus());
    }

    DebtPositionDTO debtPositionDTO = debtPositionService.mapDebtPosition(debtPosition);

    List<InstallmentDTO> installment2operate = debtPositionDTO.getPaymentOptions().stream()
      .map(PaymentOptionDTO::getInstallments).flatMap(Collection::stream)
      .filter(installmentDTO -> InstallmentUtils.MODIFIABLE_STATUSES.contains(installmentDTO.getStatus()))
      .toList();

    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .partialChange(false)
      .massive(false)
      .build();

    return debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, installment2operate, wfExecutionParameters, accessToken, operatorExternalUserId);
  }


  private void deleteDraftDebtPosition(DebtPosition debtPosition, String accessToken, String operatorExternalUserId) {
    Long organizationId = debtPosition.getOrganizationId();
    Organization org = organizationService.getOrganizationById(organizationId, accessToken)
      .orElseThrow(() -> new InvalidValueException("INVALID_ORGANIZATION", String.format("Provided organization with id %s not found", organizationId)));
    if(!OrganizationStatus.ACTIVE.equals(org.getStatus())){
      throw new InvalidValueException("INVALID_ORGANIZATION_STATUS", "Provided organization is not ACTIVE");
    }
    authorizeOperatorOnDebtPositionTypeService.authorize(org.getIpaCode(), debtPosition.getDebtPositionTypeOrgId(), operatorExternalUserId);

    debtPositionService.delete(debtPosition);

    log.info("Permanently deleted debt position with id {} having status DRAFT", debtPosition.getDebtPositionId());
  }

  private boolean isIunPresent(DebtPosition debtPosition) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments()
        .stream())
        .anyMatch(installment -> StringUtils.isNotBlank(installment.getIun()));
  }
}
