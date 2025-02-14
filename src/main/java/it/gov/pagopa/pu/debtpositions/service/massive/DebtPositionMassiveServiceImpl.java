package it.gov.pagopa.pu.debtpositions.service.massive;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.enums.IngestionFlowRowAction;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.mapper.massive.InstallmentSynchronizeMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.*;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.CreateDebtPositionService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class DebtPositionMassiveServiceImpl implements DebtPositionMassiveService {

  private final InstallmentSynchronizeMapper installmentSynchronizeMapper;
  private final DebtPositionRepository debtPositionRepository;
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeRepository debtPositionTypeRepository;
  private final OrganizationSearchClient organizationSearchClient;
  private final CreateDebtPositionService createDebtPositionService;

  private static final Set<InstallmentStatus> installmentStatusesValidForModification = Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED, InstallmentStatus.DRAFT);
  private static final Set<InstallmentStatus> installmentStatusesValidForInsertion = Set.of(InstallmentStatus.CANCELLED, InstallmentStatus.INVALID);

  public DebtPositionMassiveServiceImpl(InstallmentSynchronizeMapper installmentSynchronizeMapper, DebtPositionRepository debtPositionRepository, PaymentOptionRepository paymentOptionRepository, InstallmentNoPIIRepository installmentNoPIIRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, DebtPositionTypeRepository debtPositionTypeRepository, OrganizationSearchClient organizationSearchClient, CreateDebtPositionService createDebtPositionService) {
    this.installmentSynchronizeMapper = installmentSynchronizeMapper;
    this.debtPositionRepository = debtPositionRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
    this.organizationSearchClient = organizationSearchClient;
    this.createDebtPositionService = createDebtPositionService;
  }

  @Override
  public String installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken, String operatorExternalUserId) {
    InstallmentSynchronizeDTO.ActionEnum action = installmentSynchronizeDTO.getAction();

    DebtPositionTypeOrg debtPositionTypeOrg = retrieveDebtPositionTypeOrg(installmentSynchronizeDTO.getOrganizationId(),
      installmentSynchronizeDTO.getDebtPositionTypeCode());

    populateFirstTransfer(installmentSynchronizeDTO, accessToken, debtPositionTypeOrg);

    DebtPositionDTO debtPositionDTO = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO);
    debtPositionDTO.setDebtPositionTypeOrgId(debtPositionTypeOrg.getDebtPositionTypeOrgId());

    Optional<DebtPosition> debtPosition = debtPositionRepository.findByIupdOrgAndDebtPositionOrigin(installmentSynchronizeDTO.getIupdOrg(), DebtPositionOrigin.ORDINARY_SIL);

    //dopo rischedulazione workflow scadenza
    return debtPosition.map(dp -> processExistingDp(installmentSynchronizeDTO, action.getValue(), dp))
      .orElseGet(() -> processNewDp(installmentSynchronizeDTO, action.getValue()));
  }

  private String processNewDp(InstallmentSynchronizeDTO installmentSynchronizeDTO, String action) {
    if (installmentSynchronizeDTO.getIupdOrg() != null) {
      throw new ConflictErrorException(String.format("There is another debt position with iupd %s requested but different origin", installmentSynchronizeDTO.getIupdOrg()));
    } else {
      if (IngestionFlowRowAction.I.name().equals(action)) {
        //creo DP, PO, IN, TR in stato unpaid
        //update importi/stati
      } else {
        Optional<InstallmentNoPII> installment = installmentNoPIIRepository.getByIudAndOrganizationIdAndStatuses(installmentSynchronizeDTO.getIud(),
          installmentSynchronizeDTO.getIupdOrg(),
          Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED));
        if (installment.isPresent()) {
          // update DP, PO, IN, TR
          // update importi/stati
        } else {
          throw new ConflictErrorException("The installment cannot be modified or cancelled because it doesn't exist or is not in an allowed status");
        }
      }
    }
    return "";
  }

  private String processExistingDp(InstallmentSynchronizeDTO installmentSynchronizeDTO, String action, DebtPosition debtPosition) {
    Optional<PaymentOption> paymentOption = paymentOptionRepository.getByPaymentOptionIndexAndIupdOrg(installmentSynchronizeDTO.getPaymentOptionIndex(), installmentSynchronizeDTO.getIupdOrg());
    if (paymentOption.isEmpty()) {
      if (IngestionFlowRowAction.I.name().equals(action)) {
        if (Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED).contains(debtPosition.getStatus())) {
          // creo PO, IN, TR in stato UNPAID
        } else {
          throw new ConflictErrorException("The payment option cannot be created because the debt position is not in an allowed status");
        }
      } else {
        throw new ConflictErrorException("The payment option cannot be modified or cancelled because it doesn't exist");
      }
    } else {
      if (Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED, DebtPositionStatus.PARTIALLY_PAID).contains(debtPosition.getStatus()) &&
        Set.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.EXPIRED, PaymentOptionStatus.PARTIALLY_PAID).contains(paymentOption.get().getStatus())) {
        Optional<InstallmentNoPII> installment = installmentNoPIIRepository.getByIudAndOrganizationIdAndStatuses(installmentSynchronizeDTO.getIud(),
          installmentSynchronizeDTO.getIupdOrg(),
          null);
        if (IngestionFlowRowAction.I.name().equals(action)) {
          if (installment.isEmpty() || installmentStatusesValidForInsertion.contains(installment.get().getStatus())) {
            // crea IN, TR in stato UNPAID
          } else {
            throw new ConflictErrorException("The installment cannot be created because it already exists");
          }
        } else {
          if (installment.isEmpty()) {
            throw new ConflictErrorException("The installment cannot be modified or cancelled because it doesn't exist");
          }
          checkInstallmentIfProcessable(installment.get(), installmentSynchronizeDTO);
          //aggiorna IN, ecc
        }
      } else {
        throw new ConflictErrorException("The installment cannot be modified, cancelled or created because the debt position and payment option are not in an allowed status");
      }
    }
    return "";
  }

  private void checkInstallmentIfProcessable(InstallmentNoPII installment, InstallmentSynchronizeDTO installmentSynchronizeDTO) {
    if (InstallmentStatus.TO_SYNC.equals(installment.getStatus())) {
      if (!installmentSynchronizeDTO.getIngestionFlowFileId().equals(installment.getIngestionFlowFileId())) {
        throw new ConflictErrorException("The installment cannot be modified or cancelled because there was an error in the previous synchronization");
      } else if (installment.getSyncStatus() != null && !installmentStatusesValidForModification.contains(installment.getSyncStatus().getSyncStatusTo())) {
        throw new ConflictErrorException("The installment cannot be modified or cancelled because is not in an allowed status");
      }
    } else if (!installmentStatusesValidForModification.contains(installment.getStatus())) {
      throw new ConflictErrorException("The installment cannot be modified or cancelled because is not in an allowed status");
    }
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId, String debtPositionTypeCode){
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(
        organizationId, debtPositionTypeCode)
      .orElseThrow(() -> new InvalidValueException(String.format("The debt position type code %s is not valid for this organizationId %s", debtPositionTypeCode, organizationId)));
  }

  private void populateFirstTransfer(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg) {
    Long organizationId = installmentSynchronizeDTO.getOrganizationId();

    Organization organization = organizationSearchClient.findByOrganizationId(organizationId, accessToken);
    String category = debtPositionTypeRepository.findByDebtPositionTypeId(debtPositionTypeOrg.getDebtPositionTypeId()).getTaxonomyCode();

    BigDecimal totalAmountOtherTransfers = installmentSynchronizeDTO.getTransfersList().stream()
      .filter(Objects::nonNull).map(TransferSynchronizeDTO::getAmount)
      .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

    TransferSynchronizeDTO firstTransfer = TransferSynchronizeDTO.builder()
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban(debtPositionTypeOrg.getIban())
      .category(category)
      .amount(installmentSynchronizeDTO.getAmount().min(totalAmountOtherTransfers))
      .remittanceInformation(installmentSynchronizeDTO.getRemittanceInformation())
      .build();

    installmentSynchronizeDTO.getTransfersList().add(firstTransfer);
  }
}

