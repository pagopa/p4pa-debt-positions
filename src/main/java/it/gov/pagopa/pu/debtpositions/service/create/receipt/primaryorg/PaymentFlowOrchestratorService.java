package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.OrdinaryInstallmentPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.UnknownDebtPositionTypeOrgRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class PaymentFlowOrchestratorService {

  private final ReceiptService receiptService;
  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final OrdinaryInstallmentPaymentHandlerService ordinaryInstallmentPaymentHandlerService;
  private final UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService;

  public PaymentFlowOrchestratorService(ReceiptService receiptService,
                                        DebtPositionRepository debtPositionRepository,
                                        InstallmentNoPIIRepository installmentNoPIIRepository,
                                        OrdinaryInstallmentPaymentHandlerService ordinaryInstallmentPaymentHandlerService,
                                        DebtPositionTypeOrgRepository debtPositionTypeOrgRepository,
                                        UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService) {
    this.receiptService = receiptService;
    this.debtPositionRepository = debtPositionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.ordinaryInstallmentPaymentHandlerService = ordinaryInstallmentPaymentHandlerService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.unknownDebtPositionTypeOrgRetrieverService = unknownDebtPositionTypeOrgRetrieverService;
  }

  public void handleAlreadyPaidLogic(InstallmentNoPII installment,
                                     ReceiptWithAdditionalNodeDataDTO incomingReceiptDTO,
                                     DebtPosition dp,
                                     Runnable fullUpdateAction,
                                     Runnable partialUpdateAction,
                                     Runnable syncAction) {

    ReceiptDTO storedReceipt = receiptService.getReceipt(installment.getReceiptId());

    boolean isStoredPagoPa = ReceiptOriginType.RECEIPT_PAGOPA.equals(storedReceipt.getReceiptOrigin());
    boolean isIncomingPagoPa = ReceiptOriginType.RECEIPT_PAGOPA.equals(incomingReceiptDTO.getReceiptOrigin());

    if (isStoredPagoPa) {
      if (!isIncomingPagoPa) {
        log.info("Updating balance/dpTypeOrgId for debtPositionId {} (Stored: {}, Incoming: {})", dp.getDebtPositionId(), storedReceipt.getReceiptOrigin(), incomingReceiptDTO.getReceiptOrigin());
        partialUpdateAction.run();
        syncAction.run();
      } else {
        log.info("Skipping update and workflow for DP {} (Both RECEIPT_PAGOPA origin)", dp.getDebtPositionId());
      }
    } else {
      if (isIncomingPagoPa) {
        log.info("Executing Full Update for DP {} (Stored: {}, Incoming: {})", dp.getDebtPositionId(), storedReceipt.getReceiptOrigin(), incomingReceiptDTO.getReceiptOrigin());
        fullUpdateAction.run();
        syncAction.run();
      } else {
        log.info("Updating balance/dpTypeOrgId for DP {} (Both not RECEIPT_PAGOPA origin)", dp.getDebtPositionId());
        partialUpdateAction.run();
        syncAction.run();
      }
    }
  }

  public void updateBalanceAndMeta(DebtPosition dp, InstallmentNoPII installment, String debtPositionTypeOrgCode, String accessToken) {
    DebtPositionTypeOrg unknownTypeOrg = unknownDebtPositionTypeOrgRetrieverService.getUnknownDebtPositionTypeOrg(dp.getOrganizationId());
    DebtPositionTypeOrg typeOrgToUse = null;

    if (Objects.equals(dp.getDebtPositionTypeOrgId(), unknownTypeOrg.getDebtPositionTypeOrgId())) {
      Optional<DebtPositionTypeOrg> specificTypeOrgOpt = debtPositionTypeOrgRepository.findByOrganizationIdAndCode(dp.getOrganizationId(), debtPositionTypeOrgCode);

      if (specificTypeOrgOpt.isPresent()) {
        DebtPositionTypeOrg specificTypeOrg = specificTypeOrgOpt.get();
        log.info("Updating DebtPosition {} from UNKNOWN to specific DebtPositionTypeOrgId [{}]", dp.getDebtPositionId(), specificTypeOrg.getDebtPositionTypeOrgId());

        debtPositionRepository.updateDebtPositionTypeOrgId(dp.getDebtPositionId(), specificTypeOrg.getDebtPositionTypeOrgId());
        dp.setDebtPositionTypeOrgId(specificTypeOrg.getDebtPositionTypeOrgId());
        typeOrgToUse = specificTypeOrg;
      } else {
        typeOrgToUse = unknownTypeOrg;
      }
    }

    log.info("Updating balance for installmentId[{}]", installment.getInstallmentId());

    if (typeOrgToUse != null) {
      ordinaryInstallmentPaymentHandlerService.resolveBalance(installment, typeOrgToUse, accessToken);
    } else {
      ordinaryInstallmentPaymentHandlerService.resolveBalance(installment, accessToken);
    }
    installmentNoPIIRepository.updateBalance(installment.getInstallmentId(), installment.getBalance());
  }
}
