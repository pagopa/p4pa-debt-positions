package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.event.producer.enums.PaymentEventType;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.GenerateIuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow.DebtPositionSyncService;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
  private final PaymentsProducerService paymentsProducerService;

  public CreateDebtPositionServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                       ValidateDebtPositionService validateDebtPositionService,
                                       DebtPositionService debtPositionService, GenerateIuvService generateIuvService,
                                       DebtPositionSyncService debtPositionSyncService,
                                       InstallmentNoPIIRepository installmentNoPIIRepository, DebtPositionProcessorService debtPositionProcessorService, PaymentsProducerService paymentsProducerService) {
    this.authorizeOperatorOnDebtPositionTypeService = authorizeOperatorOnDebtPositionTypeService;
    this.validateDebtPositionService = validateDebtPositionService;
    this.debtPositionService = debtPositionService;
    this.generateIuvService = generateIuvService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.paymentsProducerService = paymentsProducerService;
  }

  @Transactional
  @Override
  public DebtPositionDTO createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId) {
    log.info("Creating a DebtPosition having organizationId {}, debtPositionTypeOrgId {}, iupdOrg {}", debtPositionDTO.getOrganizationId(),
      debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getIupdOrg());

    authorizeOperatorOnDebtPositionTypeService.authorize(debtPositionDTO.getOrganizationId(), debtPositionDTO.getDebtPositionTypeOrgId(), operatorExternalUserId);
    validateDebtPositionService.validate(debtPositionDTO, accessToken);
    verifyInstallmentUniqueness(debtPositionDTO);
    generateIuv(debtPositionDTO, accessToken);
    DebtPositionDTO debtPositionUpdated = debtPositionProcessorService.updateAmounts(debtPositionDTO);

    if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.UNPAID)) {
      updateDebtPositionStatusToSync(debtPositionUpdated);
    } else if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.DRAFT)) {
      updateDebtPositionStatusToDraft(debtPositionUpdated);
    } else if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.PAID)) {
      updateDebtPositionStatusToPaid(debtPositionUpdated);
    }

    DebtPositionDTO savedDebtPosition = debtPositionService.saveDebtPosition(debtPositionUpdated);

    invokeWorkflowIfStatusToSync(savedDebtPosition, accessToken, massive);

    log.info("DebtPosition created with id {}", debtPositionDTO.getDebtPositionId());
    return savedDebtPosition;
  }

  private void invokeWorkflowIfStatusToSync(DebtPositionDTO debtPositionDTO, String accessToken, Boolean massive) {
    if (debtPositionDTO.getStatus().equals(DebtPositionStatus.TO_SYNC)) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      invokeWorkflow(debtPositionDTO, accessToken, massive);

      log.info("Sending creation message to queue for debt position with id {}", debtPositionDTO.getDebtPositionId());
      paymentsProducerService.notifyPaymentsEvent(debtPositionDTO, PaymentEventType.DP_CREATED);
    }
  }

  private void updateDebtPositionStatusToSync(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setStatus(PaymentOptionStatus.TO_SYNC);
      paymentOption.getInstallments().forEach(installment -> {
        installment.setStatus(InstallmentStatus.TO_SYNC);
        installment.setSyncStatus(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID));
      });
    });
  }

  private void updateDebtPositionStatusToDraft(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    debtPositionDTO.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setStatus(PaymentOptionStatus.DRAFT);
      paymentOption.getInstallments().forEach(installment ->
        installment.setStatus(InstallmentStatus.DRAFT));
    });
  }

  private void updateDebtPositionStatusToPaid(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);
    debtPositionDTO.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setStatus(PaymentOptionStatus.PAID);
      paymentOption.getInstallments().forEach(installment ->
        installment.setStatus(InstallmentStatus.PAID));
    });
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

  private void generateIuv(DebtPositionDTO debtPositionDTO, String accessToken) {
    if (Boolean.TRUE.equals(debtPositionDTO.getFlagPagoPaPayment())) {
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

  private void invokeWorkflow(DebtPositionDTO debtPositionDTO, String accessToken, Boolean massive) {
    WorkflowCreatedDTO workflow = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, accessToken, massive);
    if (workflow != null) {
      log.info("Workflow created with id {}", workflow.getWorkflowId());
    } else {
      log.info("Workflow creation was not executed for debtPositionId {}, origin {}, massive {}: received null response", debtPositionDTO.getDebtPositionId(), debtPositionDTO.getDebtPositionOrigin(), massive);
    }
  }
}
