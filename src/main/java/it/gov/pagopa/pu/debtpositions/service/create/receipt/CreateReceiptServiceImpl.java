package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
    log.info("createReceipt paymentReceiptId[{}} org/nav/iud[{}/{}/{}]",
      receiptDTO.getPaymentReceiptId(),
      receiptDTO.getOrgFiscalCode(),
      receiptDTO.getNoticeNumber(),
      receiptDTO.getIud());

    ReceiptDTO receiptInDb = checkIfAlreadyStored(receiptDTO);
    if (receiptInDb != null) {
      if(isManualImport(receiptDTO)){
        if(!ReceiptOriginType.RECEIPT_FILE.equals(receiptDTO.getReceiptOrigin())){
          throw new ConflictErrorException("Receipt having paymentReceiptId " + receiptDTO.getPaymentReceiptId() + " has already been stored with origin " + receiptInDb.getReceiptOrigin() + " and id " + receiptInDb.getReceiptId());
        }
      } else {
        // Nothing to do (neither updating data), this event has already been handled
        return receiptInDb;
      }
    }

    saveReceipt(receiptDTO);

    DebtPosition primaryOrgDp = primaryOrgPaymentHandlerService.handle(receiptDTO, accessToken);
    secondaryOrgPaymentHandlerService.handle(primaryOrgDp, receiptDTO.getTransfers(), accessToken);
    mixedDpPaymentHandlerService.handle(primaryOrgDp);

    return receiptDTO;
  }

  private ReceiptDTO checkIfAlreadyStored(ReceiptDTO receiptDTO) {
    ReceiptNoPII receiptInDb = receiptNoPIIRepository.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId());
    if (receiptInDb != null) {
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
