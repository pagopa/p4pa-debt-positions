package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
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

@Service
@Slf4j
public class DebtPositionCancelInstallmentServiceImpl extends BaseDebtPositionOperationService implements DebtPositionCancelInstallmentService {

  protected DebtPositionCancelInstallmentServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                                     DebtPositionService debtPositionService,
                                                     DebtPositionSyncService debtPositionSyncService,
                                                     DebtPositionProcessorService debtPositionProcessorService,
                                                     OrganizationService organizationService,
                                                     DebtPositionMapper debtPositionMapper,
                                                     DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService, debtPositionProcessorService, organizationService, debtPositionMapper, debtPositionHierarchyStatusAlignerService);
  }

  @Override
  public Pair<DebtPositionDTO, String> cancelInstallment(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, Boolean massive, String accessToken, String operatorExternalUserId) {
    List<Long> installmentIds = installments2operate.stream().map(InstallmentDTO::getInstallmentId).toList();
    log.info("Cancelling installments with ids {} for debt position with id {}", installmentIds, debtPositionDTO.getDebtPositionId());

    Pair<DebtPositionDTO, String> debtPositionUpdated = execute(debtPositionDTO, installments2operate, massive, PaymentEventType.DP_UPDATED, accessToken, operatorExternalUserId);

    log.info("Cancelled installments for debt position with id {}", debtPositionDTO.getDebtPositionId());
    return debtPositionUpdated;
  }

  @Override
  public DebtPositionDTO applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
    List<Long> installmentIds = installments2operate.stream().map(InstallmentDTO::getInstallmentId).toList();
    log.info("Updating status cancelled for installments with ids {}", installmentIds);

    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream()
        .filter(installmentDTO -> installmentIds.contains(installmentDTO.getInstallmentId()))
        .findFirst()
        .ifPresent(installmentDTO -> {
            installmentDTO.setSyncStatus(new InstallmentSyncStatus(installmentDTO.getStatus(), InstallmentStatus.CANCELLED));
            installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
          }
        ));

    return debtPositionDTO;
  }

}
