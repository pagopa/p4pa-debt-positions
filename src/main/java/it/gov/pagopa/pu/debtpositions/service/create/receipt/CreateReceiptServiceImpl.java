package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
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

    ReceiptDTO receiptAlreadyHandled = checkIfAlreadyHandled(receiptDTO);
    if (receiptAlreadyHandled != null) {
      return receiptAlreadyHandled;
    }

    saveReceipt(receiptDTO);

    Optional<DebtPosition> primaryOrgDp = primaryOrgPaymentHandlerService.handlePayment(receiptDTO, accessToken);
    secondaryOrgPaymentHandlerService.handle(receiptDTO, accessToken);
    primaryOrgDp.ifPresent(dp -> mixedDpPaymentHandlerService.handle(dp, receiptDTO, accessToken));

    return receiptDTO;
  }

  private ReceiptDTO checkIfAlreadyHandled(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    ReceiptDTO receiptInDb = checkIfAlreadyStored(receiptDTO);
    if (receiptInDb != null) {
      if (isManualImport(receiptDTO)) {
        if (!ReceiptOriginType.RECEIPT_FILE.equals(receiptInDb.getReceiptOrigin())) {
          throw new ConflictErrorException("Receipt having paymentReceiptId " + receiptDTO.getPaymentReceiptId() +
            " has already been stored with origin " + receiptInDb.getReceiptOrigin() + " and id " + receiptInDb.getReceiptId());
        }
        logReceiptData("Updating Receipt manually imported receiptOrigin[" + receiptDTO.getReceiptOrigin() + "]", receiptDTO);
      } else {
        logReceiptData("Skipping Receipt already handled receiptOrigin[" + receiptDTO.getReceiptOrigin() + "]", receiptDTO);
        // Nothing to do (neither updating data), this event has already been handled
        return receiptInDb;
      }
    }
    return null;
  }

  private static void logReceiptData(String message, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    log.info("{} paymentReceiptId[{}} org/nav/iud[{}/{}/{}]",
      message,
      receiptDTO.getPaymentReceiptId(),
      receiptDTO.getOrgFiscalCode(),
      receiptDTO.getNoticeNumber(),
      receiptDTO.getIud());
  }

  private ReceiptDTO checkIfAlreadyStored(ReceiptDTO receiptDTO) {
    ReceiptNoPII receiptInDb = receiptNoPIIRepository.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId());
    if (receiptInDb != null) {
      if(!receiptInDb.getReceiptOrigin().equals(receiptDTO.getReceiptOrigin())){
        throw new ConflictErrorException("Receipt having paymentReceiptId " + receiptDTO.getPaymentReceiptId() +
          " has already been stored with origin " + receiptInDb.getReceiptOrigin() + " and id " + receiptInDb.getReceiptId() +
          " while the requested Receipt has origin " + receiptDTO.getReceiptOrigin());
      }
      log.info("Receipt with paymentReceiptId[{}] already present in DB id[{}]", receiptDTO.getPaymentReceiptId(), receiptInDb.getReceiptId());
      receiptDTO.setReceiptId(receiptInDb.getReceiptId());
      receiptDTO.setNoPII(receiptInDb);
      return receiptDTO;
    }
    return null;
  }

  private boolean isManualImport(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    return !StringUtils.isEmpty(receiptDTO.getIud());
  }

  private void saveReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    ReceiptDTO storedReceipt = receiptPIIRepository.save(receiptDTO);
    receiptDTO.setReceiptId(storedReceipt.getReceiptId());
    receiptDTO.setNoPII(storedReceipt.getNoPII());
    log.debug("Receipt paymentReceiptId[{}} persisted with id[{}]", receiptDTO.getPaymentReceiptId(), storedReceipt.getReceiptId());
  }

}
