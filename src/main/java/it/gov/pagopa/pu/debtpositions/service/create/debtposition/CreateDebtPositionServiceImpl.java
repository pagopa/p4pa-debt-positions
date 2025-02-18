package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.GenerateIuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class CreateDebtPositionServiceImpl implements CreateDebtPositionService {

  private final AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;
  private final ValidateDebtPositionService validateDebtPositionService;
  private final DebtPositionService debtPositionService;
  private final GenerateIuvService generateIuvService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final InstallmentPIIRepository installmentPIIRepository;
  private final DebtPositionProcessorService debtPositionProcessorService;
  private final InstallmentMapper installmentMapper;
  private final TransferRepository transferRepository;
  private final PaymentOptionRepository paymentOptionRepository;

  public CreateDebtPositionServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                       ValidateDebtPositionService validateDebtPositionService,
                                       DebtPositionService debtPositionService,
                                       GenerateIuvService generateIuvService,
                                       DebtPositionSyncService debtPositionSyncService,
                                       InstallmentNoPIIRepository installmentNoPIIRepository, InstallmentPIIRepository installmentPIIRepository,
                                       DebtPositionProcessorService debtPositionProcessorService, InstallmentMapper installmentMapper, TransferRepository transferRepository, PaymentOptionRepository paymentOptionRepository) {
    this.authorizeOperatorOnDebtPositionTypeService = authorizeOperatorOnDebtPositionTypeService;
    this.validateDebtPositionService = validateDebtPositionService;
    this.debtPositionService = debtPositionService;
    this.generateIuvService = generateIuvService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.installmentPIIRepository = installmentPIIRepository;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.installmentMapper = installmentMapper;
    this.transferRepository = transferRepository;
    this.paymentOptionRepository = paymentOptionRepository;
  }

  @Transactional
  @Override
  public Pair<DebtPositionDTO, String> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId) {
    log.info("Creating a DebtPosition having organizationId {}, debtPositionTypeOrgId {}, iupdOrg {}", debtPositionDTO.getOrganizationId(),
      debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getIupdOrg());

    authorizeOperatorOnDebtPositionTypeService.authorize(debtPositionDTO.getOrganizationId(), debtPositionDTO.getDebtPositionTypeOrgId(), operatorExternalUserId);
    validateDebtPositionService.validate(debtPositionDTO, accessToken);
    verifyInstallmentUniqueness(debtPositionDTO);
    generateIuv(debtPositionDTO, accessToken);
    DebtPositionDTO debtPositionUpdated = debtPositionProcessorService.updateAmounts(debtPositionDTO);

    if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.UNPAID)) {
      updateDebtPositionStatus(debtPositionUpdated, DebtPositionStatus.TO_SYNC, PaymentOptionStatus.TO_SYNC, InstallmentStatus.TO_SYNC);
    } else if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.DRAFT)) {
      updateDebtPositionStatus(debtPositionUpdated, DebtPositionStatus.DRAFT, PaymentOptionStatus.DRAFT, InstallmentStatus.DRAFT);
    } else if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.PAID)) {
      updateDebtPositionStatus(debtPositionUpdated, DebtPositionStatus.PAID, PaymentOptionStatus.PAID, InstallmentStatus.PAID);
    }

    DebtPositionDTO savedDebtPosition = debtPositionService.saveDebtPosition(debtPositionUpdated);

    String workflowId = invokeWorkflow(savedDebtPosition, accessToken, massive);

    log.info("DebtPosition created with id {}", debtPositionDTO.getDebtPositionId());
    return Pair.of(savedDebtPosition, workflowId);
  }

  @Override
  public Pair<DebtPositionDTO, String> createInstallment(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, PaymentOptionDTO paymentOptionDTO, InstallmentDTO installmentDTO) {
    installmentDTO.setPaymentOptionId(paymentOptionDTO.getPaymentOptionId());
    installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
    installmentDTO.setSyncStatus(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID));

    verifyInstallmentUniqueness(debtPositionDTO);
    generateInstallmentIuv(installmentDTO, debtPositionDTO.getOrganizationId(), accessToken);

    InstallmentDTO savedInstallment = debtPositionService.saveSingleInstallment(installmentDTO);

    //update stati

    //update importi




    String workflowId = invokeWorkflow(debtPositionDTO, accessToken, massive);

    return Pair.of(debtPositionDTO, workflowId);
  }

  @Override
  public Pair<DebtPositionDTO, String> createPaymentOption(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, PaymentOptionDTO paymentOptionDTO) {
    return null;
  }

  private String invokeWorkflow(DebtPositionDTO debtPositionDTO, String accessToken, Boolean massive) {
    if(!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      WorkflowCreatedDTO workflowCreatedDTO = debtPositionSyncService.syncDebtPosition(debtPositionDTO, massive, PaymentEventType.DP_CREATED, accessToken);
      return workflowCreatedDTO.getWorkflowId();
    }
    return null;
  }

  private void updateDebtPositionStatus(DebtPositionDTO debtPositionDTO, DebtPositionStatus debtPositionStatus, PaymentOptionStatus paymentStatus,
                                        InstallmentStatus installmentStatus) {
    debtPositionDTO.setStatus(debtPositionStatus);
    debtPositionDTO.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setStatus(paymentStatus);
      paymentOption.getInstallments().forEach(installment -> {
        installment.setStatus(installmentStatus);
        if (debtPositionStatus.equals(DebtPositionStatus.TO_SYNC)) {
          installment.setSyncStatus(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID));
        }
      });
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
          generateInstallmentIuv(installment, orgId, accessToken);
        });
    }
  }

  private void generateInstallmentIuv(InstallmentDTO installmentDTO, Long orgId, String accessToken){
    String generatedIuv = generateIuvService.generateIuv(orgId, accessToken);
    String nav = generateIuvService.iuv2Nav(generatedIuv);
    installmentDTO.setIuv(generatedIuv);
    installmentDTO.setNav(nav);
  }

}
