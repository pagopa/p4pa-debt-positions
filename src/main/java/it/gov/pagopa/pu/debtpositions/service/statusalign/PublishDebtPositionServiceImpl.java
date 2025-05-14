package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
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

  private final DebtPositionMapper debtPositionMapper;

  public PublishDebtPositionServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService, DebtPositionService debtPositionService, DebtPositionSyncService debtPositionSyncService, DebtPositionProcessorService debtPositionProcessorService, OrganizationService organizationService, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, DebtPositionMapper debtPositionMapper) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService,
      debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);

    this.debtPositionMapper = debtPositionMapper;
  }

  @Transactional
  public Pair<DebtPositionDTO, WorkflowCreatedDTO> publishDebtPosition(Long debtPositionId, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    DebtPosition debtPosition = debtPositionService.getDebtPositionNoPII(debtPositionId);

    if (!DRAFT.equals(debtPosition.getStatus())) {
      throw new ConflictErrorException("Only debt positions in DRAFT status can be published");
    }

    DebtPositionDTO debtPositionDTO = debtPositionMapper.mapToDto(debtPosition);

    List<InstallmentDTO> installment2operate = debtPositionDTO.getPaymentOptions().stream()
      .map(PaymentOptionDTO::getInstallments).flatMap(Collection::stream).toList();

    WorkflowCreatedDTO workflow = execute(debtPositionDTO, installment2operate,
      wfExecutionParameters, PaymentEventType.DP_CREATED, accessToken, operatorExternalUserId);

    log.info("DebtPosition published with id {}", debtPositionDTO.getDebtPositionId());

    return Pair.of(debtPositionDTO, workflow);
  }

  @Override
  protected void applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
    debtPositionDTO.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().forEach(installment -> {
        installment.setStatus(TO_SYNC);
        InstallmentUtils.setStatus(installment, UNPAID);
      }));
  }
}
