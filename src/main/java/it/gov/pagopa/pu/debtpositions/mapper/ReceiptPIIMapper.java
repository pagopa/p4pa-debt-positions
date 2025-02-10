package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class ReceiptPIIMapper {

  private final DataCipherService dataCipherService;
  private final PersonalDataService personalDataService;

  public ReceiptPIIMapper(DataCipherService dataCipherService, PersonalDataService personalDataService) {
    this.dataCipherService = dataCipherService;
    this.personalDataService = personalDataService;
  }

  public Pair<ReceiptNoPII, ReceiptPIIDTO> map(Receipt receipt) {
    ReceiptNoPII receiptNoPII = new ReceiptNoPII();

    receiptNoPII.setReceiptId(receipt.getReceiptId());
    receiptNoPII.setIngestionFlowFileId(receipt.getIngestionFlowFileId());
    receiptNoPII.setReceiptOrigin(receipt.getReceiptOrigin());
    receiptNoPII.setPaymentReceiptId(receipt.getPaymentReceiptId());
    receiptNoPII.setNoticeNumber(receipt.getNoticeNumber());
    receiptNoPII.setPaymentNote(receipt.getPaymentNote());
    receiptNoPII.setOrgFiscalCode(receipt.getOrgFiscalCode());
    receiptNoPII.setOutcome(receipt.getOutcome());
    receiptNoPII.setCreditorReferenceId(receipt.getCreditorReferenceId());
    receiptNoPII.setPaymentAmountCents(receipt.getPaymentAmountCents());
    receiptNoPII.setDescription(receipt.getDescription());
    receiptNoPII.setCompanyName(receipt.getCompanyName());
    receiptNoPII.setOfficeName(receipt.getOfficeName());
    receiptNoPII.setIdPsp(receipt.getIdPsp());
    receiptNoPII.setPspFiscalCode(receipt.getPspFiscalCode());
    receiptNoPII.setPspPartitaIva(receipt.getPspPartitaIva());
    receiptNoPII.setPspCompanyName(receipt.getPspCompanyName());
    receiptNoPII.setIdChannel(receipt.getIdChannel());
    receiptNoPII.setChannelDescription(receipt.getChannelDescription());
    receiptNoPII.setPaymentMethod(receipt.getPaymentMethod());
    receiptNoPII.setFeeCents(receipt.getFeeCents());
    receiptNoPII.setPaymentDateTime(receipt.getPaymentDateTime());
    receiptNoPII.setApplicationDate(receipt.getApplicationDate());
    receiptNoPII.setTransferDate(receipt.getTransferDate());
    receiptNoPII.setStandin(receipt.isStandin());
    receiptNoPII.setCreationDate(receipt.getCreationDate());
    receiptNoPII.setUpdateDate(receipt.getUpdateDate());
    receiptNoPII.setUpdateOperatorExternalId(receipt.getUpdateOperatorExternalId());
    receiptNoPII.setDebtorFiscalCodeHash(dataCipherService.hash(receipt.getDebtor().getFiscalCode()));
    receiptNoPII.setDebtorEntityType(receipt.getDebtor().getEntityType());
    if (receipt.getNoPII() != null) {
      receiptNoPII.setPersonalDataId(receipt.getNoPII().getPersonalDataId());
    }

    ReceiptPIIDTO receiptPIIDTO = ReceiptPIIDTO.builder()
      .debtor(receipt.getDebtor())
      .payer(receipt.getPayer())
      .build();

    return Pair.of(receiptNoPII, receiptPIIDTO);
  }

  public Receipt map(ReceiptNoPII receiptNoPII) {
    ReceiptPIIDTO pii = personalDataService.get(receiptNoPII.getPersonalDataId(), ReceiptPIIDTO.class);
    return Receipt.builder()
      .receiptId(receiptNoPII.getReceiptId())
      .ingestionFlowFileId(receiptNoPII.getIngestionFlowFileId())
      .receiptOrigin(receiptNoPII.getReceiptOrigin())
      .paymentReceiptId(receiptNoPII.getPaymentReceiptId())
      .noticeNumber(receiptNoPII.getNoticeNumber())
      .paymentNote(receiptNoPII.getPaymentNote())
      .orgFiscalCode(receiptNoPII.getOrgFiscalCode())
      .outcome(receiptNoPII.getOutcome())
      .creditorReferenceId(receiptNoPII.getCreditorReferenceId())
      .paymentAmountCents(receiptNoPII.getPaymentAmountCents())
      .description(receiptNoPII.getDescription())
      .companyName(receiptNoPII.getCompanyName())
      .officeName(receiptNoPII.getOfficeName())
      .idPsp(receiptNoPII.getIdPsp())
      .pspFiscalCode(receiptNoPII.getPspFiscalCode())
      .pspPartitaIva(receiptNoPII.getPspPartitaIva())
      .pspCompanyName(receiptNoPII.getPspCompanyName())
      .idChannel(receiptNoPII.getIdChannel())
      .channelDescription(receiptNoPII.getChannelDescription())
      .paymentMethod(receiptNoPII.getPaymentMethod())
      .feeCents(receiptNoPII.getFeeCents())
      .paymentDateTime(receiptNoPII.getPaymentDateTime())
      .applicationDate(receiptNoPII.getApplicationDate())
      .transferDate(receiptNoPII.getTransferDate())
      .standin(receiptNoPII.isStandin())
      .creationDate(receiptNoPII.getCreationDate())
      .updateDate(receiptNoPII.getUpdateDate())
      .updateOperatorExternalId(receiptNoPII.getUpdateOperatorExternalId())
      .debtor(pii.getDebtor())
      .payer(pii.getPayer())
      .noPII(receiptNoPII)
      .build();
  }
}
