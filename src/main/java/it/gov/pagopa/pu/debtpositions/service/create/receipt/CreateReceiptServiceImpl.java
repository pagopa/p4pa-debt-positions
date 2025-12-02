package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.mixed.MixedDpPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.PrimaryOrgPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.secondaryorg.SecondaryOrgPaymentHandlerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CreateReceiptServiceImpl implements CreateReceiptService {
  private final ReceiptNoPIIRepository receiptNoPIIRepository;
  private final ReceiptPIIRepository receiptPIIRepository;

  private final PrimaryOrgPaymentHandlerService primaryOrgPaymentHandlerService;
  private final SecondaryOrgPaymentHandlerService secondaryOrgPaymentHandlerService;
  private final MixedDpPaymentHandlerService mixedDpPaymentHandlerService;

  public CreateReceiptServiceImpl(ReceiptNoPIIRepository receiptNoPIIRepository, ReceiptPIIRepository receiptPIIRepository, PrimaryOrgPaymentHandlerService primaryOrgPaymentHandlerService, SecondaryOrgPaymentHandlerService secondaryOrgPaymentHandlerService, MixedDpPaymentHandlerService mixedDpPaymentHandlerService) {
    this.receiptNoPIIRepository = receiptNoPIIRepository;
    this.receiptPIIRepository = receiptPIIRepository;
    this.primaryOrgPaymentHandlerService = primaryOrgPaymentHandlerService;
    this.secondaryOrgPaymentHandlerService = secondaryOrgPaymentHandlerService;
    this.mixedDpPaymentHandlerService = mixedDpPaymentHandlerService;
  }

  @Override
  @Transactional
  public ReceiptDTO createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    logReceiptData("createReceipt", receiptDTO);

    ReceiptDTO existingReceipt = getExistingReceipt(receiptDTO);
    boolean shouldSave;

    if (existingReceipt == null) {
      shouldSave = true;
    } else {
      shouldSave = shouldUpdateExistingReceipt(receiptDTO, existingReceipt);
    }

    if (shouldSave) {
      saveReceipt(receiptDTO);
    }

    Optional<DebtPosition> primaryOrgDp = primaryOrgPaymentHandlerService.handlePayment(receiptDTO, accessToken);
    secondaryOrgPaymentHandlerService.handle(receiptDTO, accessToken);
    primaryOrgDp.ifPresent(dp -> mixedDpPaymentHandlerService.handle(dp, receiptDTO, accessToken));

    return receiptDTO;
  }

  private ReceiptDTO getExistingReceipt(ReceiptDTO receiptDTO) {
    ReceiptNoPII receiptInDb = receiptNoPIIRepository.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId());
    if (receiptInDb != null) {
      log.info("Receipt with paymentReceiptId[{}] already present in DB with id[{}] and origin[{}]", receiptDTO.getPaymentReceiptId(), receiptInDb.getReceiptId(), receiptInDb.getReceiptOrigin());
      receiptDTO.setReceiptId(receiptInDb.getReceiptId());
      receiptDTO.setNoPII(receiptInDb);
      return receiptDTO;
    }
    return null;
  }

  private static void logReceiptData(String message, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    log.info("{} paymentReceiptId[{}] org/nav/iud[{}/{}/{}]",
      message,
      receiptDTO.getPaymentReceiptId(),
      receiptDTO.getOrgFiscalCode(),
      receiptDTO.getNoticeNumber(),
      receiptDTO.getIud());
  }

  private boolean shouldUpdateExistingReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO, ReceiptDTO existingReceipt) {
    if (StringUtils.isEmpty(receiptDTO.getIud())) {
      log.info("Skipping receipt update: the input is not a manual import");
      return false;
    }

    if (ReceiptOriginType.RECEIPT_FILE.equals(existingReceipt.getReceiptOrigin())) {
      log.info("Updating receipt: the input is a manual import and the stored receipt has technical origin: {}", existingReceipt.getReceiptOrigin());
      return true;
    }

    log.info("Skipping update for manual import: existing receipt origin [{}] is not technical", existingReceipt.getReceiptOrigin());
    return false;
  }

  private void saveReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    ReceiptDTO storedReceipt = receiptPIIRepository.save(receiptDTO);
    receiptDTO.setReceiptId(storedReceipt.getReceiptId());
    receiptDTO.setNoPII(storedReceipt.getNoPII());
    log.debug("Receipt paymentReceiptId[{}] persisted/updated with id[{}]", receiptDTO.getPaymentReceiptId(), storedReceipt.getReceiptId());
  }
}
