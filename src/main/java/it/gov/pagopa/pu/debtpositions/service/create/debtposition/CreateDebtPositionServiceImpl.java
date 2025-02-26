package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.GenerateIuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.getRandomicUUID;

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
  private final OrganizationService organizationService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  public CreateDebtPositionServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                       ValidateDebtPositionService validateDebtPositionService,
                                       DebtPositionService debtPositionService,
                                       GenerateIuvService generateIuvService,
                                       DebtPositionSyncService debtPositionSyncService,
                                       InstallmentNoPIIRepository installmentNoPIIRepository,
                                       DebtPositionProcessorService debtPositionProcessorService,
                                       OrganizationService organizationService,
                                       DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
    this.authorizeOperatorOnDebtPositionTypeService = authorizeOperatorOnDebtPositionTypeService;
    this.validateDebtPositionService = validateDebtPositionService;
    this.debtPositionService = debtPositionService;
    this.generateIuvService = generateIuvService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.organizationService = organizationService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
  }

  @Transactional
  @Override
  public DebtPositionDTO createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId) {
    log.info("Creating a DebtPosition having organizationId {}, debtPositionTypeOrgId {}, iupdOrg {}", debtPositionDTO.getOrganizationId(),
      debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getIupdOrg());

    Organization org = organizationService.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)
      .orElseThrow(() -> new InvalidValueException("Provided organization id not found on db."));
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId()).orElse(null);

    checkDebtPosition(debtPositionDTO, org);
    authorizeOperatorOnDebtPositionTypeService.authorize(debtPositionDTO.getDebtPositionTypeOrgId(), operatorExternalUserId);
    DebtPositionDTO debtPositionUpdated = debtPositionProcessorService.updateAmounts(debtPositionDTO);
    validateDebtPositionService.validate(debtPositionUpdated, accessToken, debtPositionTypeOrg);

    verifyInstallmentUniqueness(debtPositionUpdated);
    checkInstallment(debtPositionUpdated, org, debtPositionTypeOrg);

    if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.UNPAID)) {
      updateDebtPositionStatus(debtPositionUpdated, DebtPositionStatus.TO_SYNC, PaymentOptionStatus.TO_SYNC, InstallmentStatus.TO_SYNC);
    } else if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.DRAFT)) {
      updateDebtPositionStatus(debtPositionUpdated, DebtPositionStatus.DRAFT, PaymentOptionStatus.DRAFT, InstallmentStatus.DRAFT);
    } else if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.PAID)) {
      updateDebtPositionStatus(debtPositionUpdated, DebtPositionStatus.PAID, PaymentOptionStatus.PAID, InstallmentStatus.PAID);
    }

    DebtPositionDTO savedDebtPosition = debtPositionService.saveDebtPosition(debtPositionUpdated, org);

    invokeWorkflow(savedDebtPosition, accessToken, massive);

    log.info("DebtPosition created with id {}", savedDebtPosition.getDebtPositionId());
    return savedDebtPosition;
  }

  private void invokeWorkflow(DebtPositionDTO debtPositionDTO, String accessToken, Boolean massive) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      debtPositionSyncService.syncDebtPosition(debtPositionDTO, massive, PaymentEventType.DP_CREATED, accessToken);
    }
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

  private void checkInstallment(DebtPositionDTO debtPositionDTO, Organization org, DebtPositionTypeOrg debtPositionTypeOrg) {
    debtPositionDTO.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .forEach(installment -> {
        String iupdPagopa = org.getOrgFiscalCode() + "_" + getRandomicUUID();
        installment.setIupdPagopa(iupdPagopa);

        if (Boolean.TRUE.equals(debtPositionDTO.getFlagPagoPaPayment())) {
          String generatedIuv = generateIuvService.generateIuv(org);
          String nav = generateIuvService.iuv2Nav(generatedIuv);
          installment.setIuv(generatedIuv);
          installment.setNav(nav);
        }

        if (StringUtils.isBlank(installment.getIud())) {
          String iud = Utilities.getRandomIUD();
          installment.setIud(iud);
        }

        if (StringUtils.isBlank(installment.getBalance())) {
          installment.setBalance(debtPositionTypeOrg.getBalance());
        }
      });
  }

  private void checkDebtPosition(DebtPositionDTO debtPositionDTO, Organization org) {
    if (StringUtils.isBlank(debtPositionDTO.getIupdOrg())) {
      debtPositionDTO.setIupdOrg(Utilities.generateRandomIupd(org.getOrgFiscalCode()));
    }
  }

}
