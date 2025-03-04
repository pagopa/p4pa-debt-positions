package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DebtPositionCancelInstallmentServiceImpl extends BaseDebtPositionOperationService implements DebtPositionCancelInstallmentService {

  protected DebtPositionCancelInstallmentServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                                     DebtPositionService debtPositionService,
                                                     DebtPositionSyncService debtPositionSyncService,
                                                     DebtPositionProcessorService debtPositionProcessorService,
                                                     OrganizationService organizationService,
                                                     DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService, debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
  }

  @Override
  public Pair<DebtPositionDTO, String> cancelInstallment(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, Boolean massive, String accessToken, String operatorExternalUserId) {
    if (log.isDebugEnabled()) {
      Set<Long> installmentIds = installments2operate.stream().map(InstallmentDTO::getInstallmentId).collect(Collectors.toSet());
      log.debug("Cancelling installments with ids {} for debt position with id {}", installmentIds, debtPositionDTO.getDebtPositionId());
    }

    Pair<DebtPositionDTO, String> debtPositionUpdated = execute(debtPositionDTO, installments2operate, massive, PaymentEventType.DPI_CANCELLED, accessToken, operatorExternalUserId);

    log.debug("Cancelled installments for debt position with id {}", debtPositionDTO.getDebtPositionId());
    return debtPositionUpdated;
  }

  @Override
  public DebtPositionDTO applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
    Set<Long> installmentIds = installments2operate.stream().map(InstallmentDTO::getInstallmentId).collect(Collectors.toSet());
    log.debug("Updating status cancelled for installments with ids {}", installmentIds);

    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream()
        .filter(installmentDTO -> installmentIds.contains(installmentDTO.getInstallmentId()))
        .findFirst()
        .ifPresent(installmentDTO -> {
          installmentDTO.setSyncStatus(new InstallmentSyncStatus(installmentDTO.getStatus(), InstallmentStatus.CANCELLED));
          installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
        })
      );

    return debtPositionDTO;
  }

}
