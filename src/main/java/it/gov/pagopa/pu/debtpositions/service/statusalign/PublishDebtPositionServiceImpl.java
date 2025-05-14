package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus.DRAFT;
import static it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus.TO_SYNC;
import static it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus.UNPAID;

@Service
@Slf4j
public class PublishDebtPositionServiceImpl extends BaseDebtPositionOperationService implements PublishDebtPositionService {

  private final ValidateDebtPositionService validateDebtPositionService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  public PublishDebtPositionServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService, DebtPositionService debtPositionService, DebtPositionSyncService debtPositionSyncService, DebtPositionProcessorService debtPositionProcessorService, OrganizationService organizationService, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, ValidateDebtPositionService validateDebtPositionService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService,
      debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
    this.validateDebtPositionService = validateDebtPositionService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
  }

  @Transactional
  public Pair<DebtPositionDTO, WorkflowCreatedDTO> publishDebtPosition(Long debtPositionId, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    DebtPositionDTO debtPositionDTO = debtPositionService.getDebtPosition(debtPositionId);

    if (!DRAFT.equals(debtPositionDTO.getStatus())) {
      throw new ConflictErrorException(String.format("The debt position with id %s cannot be published because is not in an allowed status: %s"
        ,debtPositionId, debtPositionDTO.getStatus()));
    }

    List<InstallmentDTO> installment2operate = debtPositionDTO.getPaymentOptions().stream()
      .map(PaymentOptionDTO::getInstallments).flatMap(Collection::stream).toList();

    WorkflowCreatedDTO workflow = execute(debtPositionDTO, installment2operate,
      wfExecutionParameters, PaymentEventType.DP_CREATED, accessToken, operatorExternalUserId);

    log.info("DebtPosition published with id {}", debtPositionDTO.getDebtPositionId());

    return Pair.of(debtPositionDTO, workflow);
  }

  @Override
  protected void applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId()).orElse(null);

    debtPositionDTO.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().forEach(installment -> {
        validateDebtPositionService.validateInstallment(installment, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin());
        installment.setStatus(TO_SYNC);
        InstallmentUtils.setStatus(installment, UNPAID);
      }));
  }
}
