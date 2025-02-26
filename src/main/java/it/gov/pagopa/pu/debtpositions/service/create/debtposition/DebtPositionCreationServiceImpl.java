package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.GenerateIuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class DebtPositionCreationServiceImpl extends BaseDebtPositionOperationService implements DebtPositionCreationService {

  private final ValidateDebtPositionService validateDebtPositionService;
  private final GenerateIuvService generateIuvService;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;

  public DebtPositionCreationServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                         ValidateDebtPositionService validateDebtPositionService,
                                         DebtPositionService debtPositionService,
                                         GenerateIuvService generateIuvService,
                                         DebtPositionSyncService debtPositionSyncService,
                                         InstallmentNoPIIRepository installmentNoPIIRepository,
                                         DebtPositionProcessorService debtPositionProcessorService,
                                         OrganizationService organizationService,
                                         DebtPositionMapper debtPositionMapper,
                                         DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService) {
    super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService,
      debtPositionProcessorService, organizationService, debtPositionMapper, debtPositionHierarchyStatusAlignerService);
    this.validateDebtPositionService = validateDebtPositionService;
    this.generateIuvService = generateIuvService;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
  }

  @Transactional
  @Override
  public DebtPositionDTO createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId) {
    log.info("Creating a DebtPosition having organizationId {}, debtPositionTypeOrgId {}, iupdOrg {}", debtPositionDTO.getOrganizationId(),
      debtPositionDTO.getDebtPositionTypeOrgId(), debtPositionDTO.getIupdOrg());

    List<InstallmentDTO> installment2operate = debtPositionDTO.getPaymentOptions().stream()
      .map(PaymentOptionDTO::getInstallments).flatMap(Collection::stream).toList();
    DebtPositionDTO savedDebtPosition = execute(debtPositionDTO, installment2operate, DebtPositionOrigin.ORDINARY,
      massive, PaymentEventType.DP_CREATED, accessToken, operatorExternalUserId).getLeft();

    log.info("DebtPosition created with id {}", savedDebtPosition.getDebtPositionId());
    return savedDebtPosition;
  }

  @Override
  public DebtPositionDTO applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate,
                                        DebtPositionOrigin debtPositionOrigin, String accessToken, Organization org) {
    debtPositionDTO.setDebtPositionOrigin(debtPositionOrigin);
    validateDebtPositionService.validate(debtPositionDTO, accessToken);
    verifyInstallmentUniqueness(debtPositionDTO);
    generateIuv(debtPositionDTO, org);

    if (debtPositionDTO.getStatus().equals(DebtPositionStatus.UNPAID)) {
      updateDebtPositionStatus(debtPositionDTO, DebtPositionStatus.TO_SYNC, PaymentOptionStatus.TO_SYNC, InstallmentStatus.TO_SYNC);
    } else if (debtPositionDTO.getStatus().equals(DebtPositionStatus.DRAFT)) {
      updateDebtPositionStatus(debtPositionDTO, DebtPositionStatus.DRAFT, PaymentOptionStatus.DRAFT, InstallmentStatus.DRAFT);
    } else if (debtPositionDTO.getStatus().equals(DebtPositionStatus.PAID)) {
      updateDebtPositionStatus(debtPositionDTO, DebtPositionStatus.PAID, PaymentOptionStatus.PAID, InstallmentStatus.PAID);
    }
    return debtPositionDTO;
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

  private void generateIuv(DebtPositionDTO debtPositionDTO, Organization org) {
    if (Boolean.TRUE.equals(debtPositionDTO.getFlagPagoPaPayment())) {
      debtPositionDTO.getPaymentOptions().stream()
        .flatMap(po -> po.getInstallments().stream())
        .forEach(installment -> {
          String generatedIuv = generateIuvService.generateIuv(org);
          String nav = generateIuvService.iuv2Nav(generatedIuv);
          installment.setIuv(generatedIuv);
          installment.setNav(nav);
        });
    }
  }

}
