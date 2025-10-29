package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DebtPositionUpdateInstallmentServiceImpl extends BaseDebtPositionOperationService implements DebtPositionUpdateInstallmentService {

  private final ValidateDebtPositionService validateDebtPositionService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  protected DebtPositionUpdateInstallmentServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService, DebtPositionService debtPositionService, DebtPositionSyncService debtPositionSyncService, DebtPositionProcessorService debtPositionProcessorService, OrganizationService organizationService, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, ValidateDebtPositionService validateDebtPositionService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService, debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
    this.validateDebtPositionService = validateDebtPositionService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
  }

  @Transactional
  @Override
  public WorkflowCreatedDTO updateInstallment(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    if (log.isDebugEnabled()) {
      Set<Long> installmentIds = installments2operate.stream().map(InstallmentDTO::getInstallmentId).collect(Collectors.toSet());
      log.debug("Updating data of installments with ids {} for debt position with id {}", installmentIds, debtPositionDTO.getDebtPositionId());
    }

    WorkflowCreatedDTO workflow = execute(debtPositionDTO, installments2operate, wfExecutionParameters, PaymentEventType.DPI_UPDATED, accessToken, operatorExternalUserId);

    log.debug("Updated installments for debt position with id {}", debtPositionDTO.getDebtPositionId());
    return workflow;
  }

  @Override
  protected void applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
    Set<Long> installmentIds = installments2operate.stream().map(InstallmentDTO::getInstallmentId).collect(Collectors.toSet());
    log.debug("Updating status for installments with ids {}", installmentIds);

    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId())
      .orElseThrow(() -> new NotFoundException(String.format("The debt position type org with id %s was not found for organization id %s",
        debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getOrganizationId())));

    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream()
        .filter(installmentDTO -> installmentIds.contains(installmentDTO.getInstallmentId()))
        .forEach(installmentDTO -> {
          validateDebtPositionService.validateInstallment(installmentDTO, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin(), debtPositionDTO.getFlagPuPagoPaPayment());

          InstallmentStatus statusTo = installmentDTO.getStatus();
          if (InstallmentStatus.EXPIRED.equals(installmentDTO.getStatus()) && installmentDTO.getDueDate() != null &&
            installmentDTO.getDueDate().isAfter(LocalDate.now())) {
            statusTo = InstallmentStatus.UNPAID;
          }
          InstallmentUtils.setStatus(installmentDTO, statusTo);
        })
      );
  }

  @Override
  protected boolean isDebtPositionTypeOrgDisabledAllowed() {
    return true;
  }
}
