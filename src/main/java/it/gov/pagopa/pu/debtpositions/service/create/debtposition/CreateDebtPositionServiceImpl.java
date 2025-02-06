package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.GenerateIuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow.DebtPositionSyncService;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class CreateDebtPositionServiceImpl implements CreateDebtPositionService {

  private final AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;
  private final ValidateDebtPositionService validateDebtPositionService;
  private final DebtPositionService debtPositionService;
  private final GenerateIuvService generateIuvService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final DebtPositionProcessorService debtPositionProcessorService;

  private static final Set<DebtPositionOrigin> DEBT_POSITION_ORIGIN_TO_SYNC = Set.of(
    DebtPositionOrigin.ORDINARY,
    DebtPositionOrigin.ORDINARY_SIL,
    DebtPositionOrigin.SPONTANEOUS
  );

  public CreateDebtPositionServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                       ValidateDebtPositionService validateDebtPositionService,
                                       DebtPositionService debtPositionService, GenerateIuvService generateIuvService,
                                       DebtPositionSyncService debtPositionSyncService,
                                       InstallmentNoPIIRepository installmentNoPIIRepository, DebtPositionProcessorService debtPositionProcessorService) {
    this.authorizeOperatorOnDebtPositionTypeService = authorizeOperatorOnDebtPositionTypeService;
    this.validateDebtPositionService = validateDebtPositionService;
    this.debtPositionService = debtPositionService;
    this.generateIuvService = generateIuvService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionProcessorService = debtPositionProcessorService;
  }

  @Override
  public DebtPositionDTO createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, Boolean pagopaPayment, String accessToken, String operatorExternalUserId) {
    log.info("Creating a DebtPosition having organizationId {}, debtPositionTypeOrgId {}, iupdOrg {}, ingestionFlowFileId {}, rowNumber {}", debtPositionDTO.getOrganizationId(),
      debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getIupdOrg(), debtPositionDTO.getIngestionFlowFileId(), debtPositionDTO.getIngestionFlowFileLineNumber());

    authorizeOperatorOnDebtPositionTypeService.authorize(debtPositionDTO.getOrganizationId(), debtPositionDTO.getDebtPositionTypeOrgId(), operatorExternalUserId);
    validateDebtPositionService.validate(debtPositionDTO, accessToken);
    verifyInstallmentUniqueness(debtPositionDTO);
    generateIuv(debtPositionDTO, pagopaPayment, accessToken);
    DebtPositionDTO debtPositionUpdated = debtPositionProcessorService.updateAmounts(debtPositionDTO);

    DebtPositionDTO savedDebtPosition = debtPositionService.saveDebtPosition(debtPositionUpdated);
    invokeWorkflow(savedDebtPosition, pagopaPayment, accessToken);

    log.info("DebtPosition created with id {}", debtPositionDTO.getDebtPositionId());
    return savedDebtPosition;
  }

  private void verifyInstallmentUniqueness(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .forEach(installmentNoPII -> {
        long countDuplicates = installmentNoPIIRepository.countExistingInstallments(debtPositionDTO.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav());
        if (countDuplicates > 0) {
          log.error("Duplicate installments found for input Installment having IUD {}, IUV {}, NAV {} on organization {}", installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav(), debtPositionDTO.getOrganizationId());
          throw new ConflictErrorException("Duplicate records found: the provided data conflicts with existing records.");
        }
      });
  }

  private void generateIuv(DebtPositionDTO debtPositionDTO, Boolean pagopaPayment, String accessToken) {
    if (Boolean.TRUE.equals(pagopaPayment)) {
      debtPositionDTO.getPaymentOptions().stream()
        .flatMap(po -> po.getInstallments().stream())
        .forEach(installment -> {
          Long orgId = debtPositionDTO.getOrganizationId();
          String generatedIuv = generateIuvService.generateIuv(orgId, accessToken);
          String nav = generateIuvService.iuv2Nav(generatedIuv);
          installment.setIuv(generatedIuv);
          installment.setNav(nav);
        });
    }
  }

  private void invokeWorkflow(DebtPositionDTO debtPositionDTO, Boolean pagopaPayment, String accessToken) {
    if (Boolean.TRUE.equals(pagopaPayment) && DEBT_POSITION_ORIGIN_TO_SYNC.contains(debtPositionDTO.getDebtPositionOrigin())) {
      WorkflowCreatedDTO workflow = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, accessToken);
      log.info("Workflow created with id {}", workflow.getWorkflowId());
    }
  }
}
