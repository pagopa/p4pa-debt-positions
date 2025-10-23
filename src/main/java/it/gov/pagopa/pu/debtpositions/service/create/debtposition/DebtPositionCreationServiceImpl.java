package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.CategoryResolverService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.IuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.getRandomicUUID;

@Service
@Primary
@Slf4j
public class DebtPositionCreationServiceImpl extends BaseDebtPositionOperationService implements DebtPositionCreationService {

  private final ValidateDebtPositionService validateDebtPositionService;
  private final IuvService iuvService;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionProcessorService debtPositionProcessorService;
  private final CategoryResolverService categoryResolverService;

  public DebtPositionCreationServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                         ValidateDebtPositionService validateDebtPositionService,
                                         DebtPositionService debtPositionService,
                                         IuvService iuvService,
                                         DebtPositionSyncService debtPositionSyncService,
                                         InstallmentNoPIIRepository installmentNoPIIRepository,
                                         DebtPositionProcessorService debtPositionProcessorService,
                                         OrganizationService organizationService,
                                         DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService,
                                         DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, CategoryResolverService categoryResolverService
  ) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService,
      debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
    this.validateDebtPositionService = validateDebtPositionService;
    this.iuvService = iuvService;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.categoryResolverService = categoryResolverService;
  }

  @Transactional
  @Override
  public WorkflowCreatedDTO createDebtPosition(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    log.info("Creating a DebtPosition having organizationId {}, debtPositionTypeOrgId {}, iupdOrg {}", debtPositionDTO.getOrganizationId(),
      debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getIupdOrg());

    List<InstallmentDTO> installment2operate = debtPositionDTO.getPaymentOptions().stream()
      .map(PaymentOptionDTO::getInstallments).flatMap(Collection::stream).toList();

    WorkflowCreatedDTO workflow = execute(debtPositionDTO, installment2operate,
      wfExecutionParameters, PaymentEventType.DP_CREATED, accessToken, operatorExternalUserId);

    log.info("DebtPosition created with id {}", debtPositionDTO.getDebtPositionId());
    return workflow;
  }

  @Override
  protected boolean isDebtPositionTypeOrgDisabledAllowed() {
    return false;
  }

  @Override
  protected void applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate,
                                String accessToken, Organization org) {

    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId()).orElse(null);
    checkDebtPosition(debtPositionDTO, org);

    debtPositionProcessorService.updateAmounts(debtPositionDTO);
    checkAllInstallments(debtPositionDTO, org, debtPositionTypeOrg, accessToken);
    validateDebtPositionService.validate(debtPositionDTO, accessToken, debtPositionTypeOrg);

    if (DebtPositionStatus.UNPAID.equals(debtPositionDTO.getStatus())) {
      updateDebtPositionStatus(debtPositionDTO, DebtPositionStatus.TO_SYNC, PaymentOptionStatus.TO_SYNC, InstallmentStatus.TO_SYNC);
    } else if (DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      updateDebtPositionStatus(debtPositionDTO, DebtPositionStatus.DRAFT, PaymentOptionStatus.DRAFT, InstallmentStatus.DRAFT);
    } else if (DebtPositionStatus.PAID.equals(debtPositionDTO.getStatus())) {
      updateDebtPositionStatus(debtPositionDTO, DebtPositionStatus.PAID, PaymentOptionStatus.PAID, InstallmentStatus.PAID);
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

  private void checkAllInstallments(DebtPositionDTO debtPositionDTO, Organization org, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    debtPositionDTO.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .forEach(installment -> checkInstallment(debtPositionDTO, org, debtPositionTypeOrg, installment, accessToken));
  }

  @Override
  public void checkInstallment(DebtPositionDTO debtPositionDTO, Organization org, DebtPositionTypeOrg debtPositionTypeOrg, InstallmentDTO installmentDTO, String accessToken) {
    if (Boolean.TRUE.equals(debtPositionDTO.getFlagPuPagoPaPayment())) {
      String nav;
      if (installmentDTO.getIuv() != null) {
        nav = iuvService.validateIuvAndRetrieveNav(installmentDTO.getIuv(), org, debtPositionDTO.getDebtPositionOrigin());
      } else {
        String generatedIuv = iuvService.generateIuv(org);
        nav = iuvService.iuv2Nav(generatedIuv);
        installmentDTO.setIuv(generatedIuv);
      }

      installmentDTO.setNav(nav);
      String iupdPagopa = org.getOrgFiscalCode() + "_" + getRandomicUUID();
      installmentDTO.setIupdPagopa(iupdPagopa);
    }

    if (StringUtils.isBlank(installmentDTO.getIud())) {
      String iud = Utilities.getRandomIUD();
      installmentDTO.setIud(iud);
    }

    setSourceFlowName(installmentDTO, debtPositionDTO.getDebtPositionOrigin(), org.getIpaCode());

    verifyInstallmentUniqueness(debtPositionDTO, installmentDTO);

    populateFirstTransfer(installmentDTO, org, debtPositionTypeOrg);
  }

  private void setSourceFlowName(InstallmentDTO installmentDTO, DebtPositionOrigin debtPositionOrigin, String orgIpaCode) {
    String now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    if (DebtPositionOrigin.ORDINARY.equals(debtPositionOrigin))
      installmentDTO.setSourceFlowName(orgIpaCode + "_IMPORT-DOVUTO_" + now);
    if (DebtPositionOrigin.SPONTANEOUS.equals(debtPositionOrigin))
      installmentDTO.setSourceFlowName(orgIpaCode + "_SPONTANEO_" + now);
    if (DebtPositionOrigin.SPONTANEOUS_SIL.equals(debtPositionOrigin))
      installmentDTO.setSourceFlowName(orgIpaCode + "_SPONTANEO-SIL_" + now);
  }

  private void verifyInstallmentUniqueness(DebtPositionDTO debtPositionDTO, InstallmentDTO installmentDTO) {
    boolean isInstallmentDuplicate = installmentNoPIIRepository.isInstallmentExists(debtPositionDTO.getOrganizationId(), installmentDTO.getIud(), installmentDTO.getIuv(), installmentDTO.getNav(), InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS);
    if (isInstallmentDuplicate) {
      log.error("Duplicate installments found for input Installment having IUD {}, IUV {}, NAV {} on organization {}", installmentDTO.getIud(), installmentDTO.getIuv(), installmentDTO.getNav(), debtPositionDTO.getOrganizationId());
      throw new ConflictErrorException("Duplicate records found: the provided data conflicts with existing records.");
    }
  }

  private void checkDebtPosition(DebtPositionDTO debtPositionDTO, Organization org) {
    if (StringUtils.isBlank(debtPositionDTO.getIupdOrg())) {
      debtPositionDTO.setIupdOrg(Utilities.generateRandomIupd(org.getOrgFiscalCode()));
    }
  }

  private void populateFirstTransfer(InstallmentDTO installmentDTO, Organization organization, DebtPositionTypeOrg debtPositionTypeOrg) {
    if (installmentDTO.getTransfers().stream()
      .anyMatch(transferDTO -> transferDTO.getTransferIndex() == 1)) {
      return;
    }

    String category = categoryResolverService.resolveCategory(installmentDTO.getLegacyPaymentMetadata(), debtPositionTypeOrg.getDebtPositionTypeId(), organization.getOrgTypeCode());

    Long totalAmountOtherTransfers = installmentDTO.getTransfers().stream()
      .mapToLong(TransferDTO::getAmountCents).sum();

    TransferDTO firstTransfer = TransferDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban(StringUtils.isEmpty(debtPositionTypeOrg.getIban()) ? organization.getIban() : debtPositionTypeOrg.getIban())
      .category(category)
      .amountCents(installmentDTO.getAmountCents() - totalAmountOtherTransfers)
      .remittanceInformation(installmentDTO.getRemittanceInformation())
      .build();

    installmentDTO.addTransfersItem(firstTransfer);
  }

}
