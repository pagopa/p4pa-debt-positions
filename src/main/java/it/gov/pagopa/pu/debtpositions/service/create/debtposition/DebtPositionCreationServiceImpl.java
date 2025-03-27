package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.IuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.getRandomicUUID;

@Service
@Slf4j
public class DebtPositionCreationServiceImpl extends BaseDebtPositionOperationService implements DebtPositionCreationService {

  private final ValidateDebtPositionService validateDebtPositionService;
  private final IuvService iuvService;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeRepository debtPositionTypeRepository;
  private final DebtPositionProcessorService debtPositionProcessorService;

  public DebtPositionCreationServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                         ValidateDebtPositionService validateDebtPositionService,
                                         DebtPositionService debtPositionService,
                                         IuvService iuvService,
                                         DebtPositionSyncService debtPositionSyncService,
                                         InstallmentNoPIIRepository installmentNoPIIRepository,
                                         DebtPositionProcessorService debtPositionProcessorService,
                                         OrganizationService organizationService,
                                         DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService,
                                         DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, DebtPositionTypeRepository debtPositionTypeRepository
  ) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService,
      debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
    this.validateDebtPositionService = validateDebtPositionService;
    this.iuvService = iuvService;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
  }

  @Transactional
  @Override
  public Pair<DebtPositionDTO, String> createDebtPosition(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    log.info("Creating a DebtPosition having organizationId {}, debtPositionTypeOrgId {}, iupdOrg {}", debtPositionDTO.getOrganizationId(),
      debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getIupdOrg());

    List<InstallmentDTO> installment2operate = debtPositionDTO.getPaymentOptions().stream()
      .map(PaymentOptionDTO::getInstallments).flatMap(Collection::stream).toList();

    Pair<DebtPositionDTO, String> savedDebtPosition = execute(debtPositionDTO, installment2operate,
      wfExecutionParameters, PaymentEventType.DP_CREATED, accessToken, operatorExternalUserId);

    log.info("DebtPosition created with id {}", savedDebtPosition.getLeft().getDebtPositionId());
    return savedDebtPosition;
  }

  @Override
  protected DebtPositionDTO applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate,
                                        String accessToken, Organization org) {

    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId()).orElse(null);
    checkDebtPosition(debtPositionDTO, org);

    DebtPositionDTO debtPositionUpdated = debtPositionProcessorService.updateAmounts(debtPositionDTO);
    checkAllInstallments(debtPositionUpdated, org, debtPositionTypeOrg);
    validateDebtPositionService.validate(debtPositionUpdated, accessToken, debtPositionTypeOrg);

    if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.UNPAID)) {
      updateDebtPositionStatus(debtPositionUpdated, DebtPositionStatus.TO_SYNC, PaymentOptionStatus.TO_SYNC, InstallmentStatus.TO_SYNC);
    } else if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.DRAFT)) {
      updateDebtPositionStatus(debtPositionUpdated, DebtPositionStatus.DRAFT, PaymentOptionStatus.DRAFT, InstallmentStatus.DRAFT);
    } else if (debtPositionUpdated.getStatus().equals(DebtPositionStatus.PAID)) {
      updateDebtPositionStatus(debtPositionUpdated, DebtPositionStatus.PAID, PaymentOptionStatus.PAID, InstallmentStatus.PAID);
    }
    return debtPositionUpdated;
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

  private void checkAllInstallments(DebtPositionDTO debtPositionDTO, Organization org, DebtPositionTypeOrg debtPositionTypeOrg) {
    debtPositionDTO.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .forEach(installment -> checkInstallment(debtPositionDTO, org, debtPositionTypeOrg, installment));
  }

  @Override
  public void checkInstallment(DebtPositionDTO debtPositionDTO, Organization org, DebtPositionTypeOrg debtPositionTypeOrg, InstallmentDTO installmentDTO) {
    if (Boolean.TRUE.equals(debtPositionDTO.getFlagPagoPaPayment())) {
      String nav;
      if (installmentDTO.getIuv() != null) {
        nav = iuvService.validateIuvAndRetrieveNav(installmentDTO.getIuv(), org);
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

    if (StringUtils.isBlank(installmentDTO.getBalance())) {
      installmentDTO.setBalance(debtPositionTypeOrg.getBalance());
    }

    populateFirstTransfer(installmentDTO, org, debtPositionTypeOrg);

    verifyInstallmentUniqueness(debtPositionDTO, installmentDTO);
  }

  private void verifyInstallmentUniqueness(DebtPositionDTO debtPositionDTO, InstallmentDTO installmentDTO) {
    long countDuplicates = installmentNoPIIRepository.countExistingInstallments(debtPositionDTO.getOrganizationId(), installmentDTO.getIud(), installmentDTO.getIuv(), installmentDTO.getNav());
    if (countDuplicates > 0) {
      log.error("Duplicate installments found for input Installment having IUD {}, IUV {}, NAV {} on organization {}", installmentDTO.getIud(), installmentDTO.getIuv(), installmentDTO.getNav(), debtPositionDTO.getOrganizationId());
      throw new ConflictErrorException("Duplicate records found: the provided data conflicts with existing records.");
    }
  }

  private void checkDebtPosition(DebtPositionDTO debtPositionDTO, Organization org) {
    if (StringUtils.isBlank(debtPositionDTO.getIupdOrg())) {
      debtPositionDTO.setIupdOrg(Utilities.generateRandomIupd(org.getOrgFiscalCode()));
    }
  }

  public void populateFirstTransfer(InstallmentDTO installmentDTO, Organization organization, DebtPositionTypeOrg debtPositionTypeOrg) {
    String category = debtPositionTypeRepository.findById(debtPositionTypeOrg.getDebtPositionTypeId())
      .orElseThrow(() -> new NotFoundException(String.format("The debt position type with id %s is not found", debtPositionTypeOrg.getDebtPositionTypeId())))
      .getTaxonomyCode();

    Long totalAmountOtherTransfers = installmentDTO.getTransfers().stream()
      .mapToLong(TransferDTO::getAmountCents).sum();

    TransferDTO firstTransfer = TransferDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban(debtPositionTypeOrg.getIban().isBlank() ? organization.getIban() : debtPositionTypeOrg.getIban())
      .category(category)
      .amountCents(installmentDTO.getAmountCents() - totalAmountOtherTransfers)
      .remittanceInformation(installmentDTO.getRemittanceInformation())
      .build();

    installmentDTO.addTransfersItem(firstTransfer);
  }
}
