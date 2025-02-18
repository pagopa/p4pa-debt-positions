package it.gov.pagopa.pu.debtpositions.service.massive;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.mapper.massive.InstallmentSynchronizeMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.*;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.CreateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.massive.action.InsertionActionMassiveDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.massive.action.UpdateActionMassiveDebtPositionService;
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
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeRepository debtPositionTypeRepository;
  private final OrganizationSearchClient organizationSearchClient;
  private final InsertionActionMassiveDebtPositionService insertionActionMassiveDebtPositionService;
  private final UpdateActionMassiveDebtPositionService updateActionMassiveDebtPositionService;

  public DebtPositionMassiveServiceImpl(InstallmentSynchronizeMapper installmentSynchronizeMapper, DebtPositionRepository debtPositionRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, DebtPositionTypeRepository debtPositionTypeRepository, OrganizationSearchClient organizationSearchClient, InsertionActionMassiveDebtPositionService insertionActionMassiveDebtPositionService, UpdateActionMassiveDebtPositionService updateActionMassiveDebtPositionService) {
    this.installmentSynchronizeMapper = installmentSynchronizeMapper;
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
    this.organizationSearchClient = organizationSearchClient;
    this.insertionActionMassiveDebtPositionService = insertionActionMassiveDebtPositionService;
    this.updateActionMassiveDebtPositionService = updateActionMassiveDebtPositionService;
  }


  @Override
  public String installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken, String operatorExternalUserId) {
    DebtPositionDTO debtPositionSynchronizeDTO = createDpFromInstallmentSynchronize(installmentSynchronizeDTO, accessToken);

    DebtPosition debtPosition = checkProcessableDebtPosition(debtPositionSynchronizeDTO.getIupdOrg());

    String workflowId = null;
    InstallmentSynchronizeDTO.ActionEnum action = installmentSynchronizeDTO.getAction();
    switch (action) {
      case InstallmentSynchronizeDTO.ActionEnum.I ->
        workflowId = insertionActionMassiveDebtPositionService.handleInsertion(debtPositionSynchronizeDTO, debtPosition, accessToken, operatorExternalUserId);
      case InstallmentSynchronizeDTO.ActionEnum.M ->
        workflowId = updateActionMassiveDebtPositionService.handleModification(debtPositionSynchronizeDTO, accessToken);
      case InstallmentSynchronizeDTO.ActionEnum.A ->
        workflowId = updateActionMassiveDebtPositionService.handleCancellation(debtPositionSynchronizeDTO, accessToken);
    }

    //dopo rischedulazione workflow scadenza

    return workflowId;
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId, String debtPositionTypeCode) {
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

  private DebtPositionDTO createDpFromInstallmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken) {
    DebtPositionTypeOrg debtPositionTypeOrg = retrieveDebtPositionTypeOrg(installmentSynchronizeDTO.getOrganizationId(),
      installmentSynchronizeDTO.getDebtPositionTypeCode());

    populateFirstTransfer(installmentSynchronizeDTO, accessToken, debtPositionTypeOrg);

    return installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
  }


  private DebtPosition checkProcessableDebtPosition(String iupdOrg) {
    Optional<DebtPosition> debtPosition = debtPositionRepository.findByIupdOrg(iupdOrg);

    if (debtPosition.isPresent() && !DebtPositionOrigin.ORDINARY_SIL.equals(debtPosition.get().getDebtPositionOrigin())) {
      throw new ConflictErrorException(String.format("There is another debt position with iupd %s requested but different origin", iupdOrg));
    }
    return debtPosition.orElse(null);
  }


//  private String processNewDp(DebtPositionDTO debtPositionDTO, String action, String accessToken, String operatorExternalUserId) {
//    String workflowId = null;
//    if (debtPositionDTO.getIupdOrg() != null) {
//      throw new ConflictErrorException(String.format("There is another debt position with iupd %s requested but different origin", debtPositionDTO.getIupdOrg()));
//    } else {
//      if (IngestionFlowRowAction.I.name().equals(action)) {
//        workflowId = createDebtPositionService.createDebtPosition(debtPositionDTO, true, accessToken, operatorExternalUserId).getRight();
//      } else {
//        Optional<InstallmentNoPII> installment = installmentNoPIIRepository.getByOrganizationIdAndIudAndIuv(
//          debtPositionDTO.getOrganizationId(),
//          debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud(),
//          debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIuv()
//        );
//        if (installment.isPresent() && installmentStatusesValidForModification.contains(installment.get().getStatus())) {
//          DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installment.get().getInstallmentId());
//          // update DP, PO, IN, TR
//          // update importi/stati
//        } else {
//          throw new ConflictErrorException("The installment cannot be modified or cancelled because it doesn't exist or is not in an allowed status");
//        }
//      }
//    }
//    return workflowId;
//  }

  //  private String processExistingDp(DebtPositionDTO debtPositionDTO, String action, DebtPosition debtPosition) {
//    Optional<PaymentOption> paymentOption = paymentOptionRepository.getByPaymentOptionIndexAndIupdOrg(
//      debtPositionDTO.getPaymentOptions().getFirst().getPaymentOptionIndex(),
//      debtPositionDTO.getIupdOrg());
//    if (paymentOption.isEmpty()) {
//      processNewPaymentOption(debtPosition, action);
//    } else {
//      if (!(Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED, DebtPositionStatus.PARTIALLY_PAID).contains(debtPosition.getStatus()) &&
//        Set.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.EXPIRED, PaymentOptionStatus.PARTIALLY_PAID).contains(paymentOption.get().getStatus()))) {
//      }
//      processExistingPaymentOption();
//    }
//    return "";
//  }
//
//  private void processNewPaymentOption(DebtPosition debtPosition, String action){
//    if (!IngestionFlowRowAction.I.name().equals(action)) {
//      throw new ConflictErrorException("The payment option cannot be modified or cancelled because it doesn't exist");
//    }
//    if (!Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED, DebtPositionStatus.DRAFT).contains(debtPosition.getStatus())) {
//      throw new ConflictErrorException("The payment option cannot be created because the debt position is not in an allowed status");
//    }
//    // creo PO, IN, TR in stato UNPAID
//  }
//
//  private void processExistingPaymentOption(DebtPositionDTO debtPositionDTO, String action){
//    InstallmentDTO newInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
//    Optional<InstallmentNoPII> installment = installmentNoPIIRepository.getByIupdOrgAndIudAndIuv(debtPositionDTO.getIupdOrg(),
//      newInstallment.getIud(),
//      newInstallment.getIuv());
//
//    if (IngestionFlowRowAction.I.name().equals(action)) {
//      if(installment.isPresent() && !installmentStatusesValidForInsertion.contains(installment.get().getStatus())){
//        throw new ConflictErrorException("The installment cannot be created because it already exists");
//      }
//      // crea IN, TR in stato UNPAID
//    } else {
//      if (installment.isEmpty()) {
//        throw new ConflictErrorException("The installment cannot be modified or cancelled because it doesn't exist");
//      }
//      checkInstallmentIfProcessable(installment.get(), newInstallment);
//      //aggiorna IN, ecc
//    }
//  }
//

}

