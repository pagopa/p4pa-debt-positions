package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DebtPositionAddInstallmentServiceImpl extends BaseDebtPositionOperationService implements DebtPositionAddInstallmentService {

  private final ValidateDebtPositionService validateDebtPositionService;
  private final DebtPositionCreationService debtPositionCreationService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  protected DebtPositionAddInstallmentServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService, DebtPositionService debtPositionService, DebtPositionSyncService debtPositionSyncService, DebtPositionProcessorService debtPositionProcessorService, OrganizationService organizationService, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, ValidateDebtPositionService validateDebtPositionService, DebtPositionCreationService debtPositionCreationService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService, debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
    this.validateDebtPositionService = validateDebtPositionService;
    this.debtPositionCreationService = debtPositionCreationService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
  }

  @Transactional
  @Override
  public WorkflowCreatedDTO addInstallment(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    log.debug("Adding new installments for debt position with id {}", debtPositionDTO.getDebtPositionId());

    WorkflowCreatedDTO workflow = execute(debtPositionDTO, installments2operate, wfExecutionParameters, PaymentEventType.DPI_ADDED, accessToken, operatorExternalUserId);

    log.debug("Added installments for debt position with id {}", debtPositionDTO.getDebtPositionId());
    return workflow;
  }

  @Override
  protected void applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
    Set<Long> installmentIds = installments2operate.stream().map(InstallmentDTO::getInstallmentId).collect(Collectors.toSet());

    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId())
      .orElseThrow(() -> new NotFoundException(String.format("The debt position type org with id %s was not found for organization id %s",
        debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getOrganizationId())));


    Set<Integer> poIndexes = HashSet.newHashSet(debtPositionDTO.getPaymentOptions().size());
    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> {
          if(!poIndexes.add(paymentOptionDTO.getPaymentOptionIndex())){
            throw new InvalidValueException("PaymentOption index duplicated: " + paymentOptionDTO.getPaymentOptionIndex());
          }
          paymentOptionDTO.getInstallments().stream()
            .filter(installmentDTO -> installmentIds.contains(installmentDTO.getInstallmentId()))
            .forEach(installmentDTO -> {
              debtPositionCreationService.checkInstallment(debtPositionDTO, org, debtPositionTypeOrg, installmentDTO, accessToken);
              validateDebtPositionService.validateInstallment(installmentDTO, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin(), debtPositionDTO.getFlagPuPagoPaPayment());
              if(!InstallmentStatus.DRAFT.equals(installmentDTO.getStatus())) {
                installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
                installmentDTO.setSyncStatus(InstallmentSyncStatus.builder().syncStatusFrom(InstallmentStatus.DRAFT).syncStatusTo(InstallmentStatus.UNPAID).build());
              }
            });
        }
      );
  }

  @Override
  protected boolean isDebtPositionTypeOrgDisabledAllowed() {
    return true;
  }
}
