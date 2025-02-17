package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CreateReceiptServiceImpl implements CreateReceiptService {


  private final ReceiptNoPIIRepository receiptNoPIIRepository;
  private final ReceiptPIIRepository receiptPIIRepository;
  private final ReceiptMapper receiptMapper;
  private final UpdatePaidDebtPositionService updateInstallmentStatusOfDebtPosition;
  private final CreatePaidTechnicalDebtPositionsService createPaidTechnicalDebtPositionsService;

  public CreateReceiptServiceImpl(ReceiptNoPIIRepository receiptNoPIIRepository, ReceiptPIIRepository receiptPIIRepository, ReceiptMapper receiptMapper, UpdatePaidDebtPositionService updateInstallmentStatusOfDebtPosition, CreatePaidTechnicalDebtPositionsService createPaidTechnicalDebtPositionsService) {
    this.receiptNoPIIRepository = receiptNoPIIRepository;
    this.receiptPIIRepository = receiptPIIRepository;
    this.receiptMapper = receiptMapper;
    this.updateInstallmentStatusOfDebtPosition = updateInstallmentStatusOfDebtPosition;
    this.createPaidTechnicalDebtPositionsService = createPaidTechnicalDebtPositionsService;
  }


  @Override
  @Transactional
  public ReceiptDTO createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    log.info("createReceipt paymentReceiptId[{}} org/nav[{}/{}]", receiptDTO.getPaymentReceiptId(), receiptDTO.getOrgFiscalCode(), receiptDTO.getNoticeNumber());

    //check if the same receipt is already present on DB
    Optional<ReceiptDTO> receiptInDb = checkIfAlreadyStored(receiptDTO);
    if (receiptInDb.isPresent())
      return receiptInDb.get();

    //persist receipt
    saveReceipt(receiptDTO);

    //check if organization who handles the notice is managed by PU and update the installment status
    boolean primaryOrgFound = updateInstallmentStatusOfDebtPosition.handleReceiptReceived(receiptDTO, accessToken);

    //for every organization handled by PU and mentioned in the receipt
    createPaidTechnicalDebtPositionsService.createPaidTechnicalDebtPositionsFromReceipt(receiptDTO, !primaryOrgFound, accessToken);

    return receiptDTO;
  }

  // check if the same receipt is already present on DB.
  // in this case just ignore it since it simply means that the same receipt has been broadcast
  // to multiple organizations managed by PU
  private Optional<ReceiptDTO> checkIfAlreadyStored(ReceiptDTO receiptDTO) {
    ReceiptNoPII receiptInDb = receiptNoPIIRepository.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId());
    if (receiptInDb != null) {
      log.info("Receipt with paymentReceiptId[{}] already present in DB id[{}]", receiptDTO.getPaymentReceiptId(), receiptInDb.getReceiptId());
      receiptDTO.setReceiptId(receiptInDb.getReceiptId());
      return Optional.of(receiptDTO);
    }
    return Optional.empty();
  }

  private void saveReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    Receipt receipt = receiptMapper.mapToModel(receiptDTO);
    long newId = receiptPIIRepository.save(receipt);
    receiptDTO.setReceiptId(newId);
    log.debug("Receipt paymentReceiptId[{}} persisted with id[{}]", receiptDTO.getPaymentReceiptId(), newId);
  }




}
