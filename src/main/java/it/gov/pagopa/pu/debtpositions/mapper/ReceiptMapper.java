package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class ReceiptMapper {

  private final PersonMapper personMapper;

  public ReceiptMapper(PersonMapper personMapper) {
    this.personMapper = personMapper;
  }

  public Receipt mapToModel(ReceiptDTO dto) {
    return Receipt.builder()
      .receiptId(dto.getReceiptId())
      .ingestionFlowFileId(dto.getIngestionFlowFileId())
      .receiptOrigin(dto.getReceiptOrigin())
      .paymentReceiptId(dto.getPaymentReceiptId())
      .noticeNumber(dto.getNoticeNumber())
      .paymentNote(dto.getPaymentNote())
      .orgFiscalCode(dto.getOrgFiscalCode())
      .outcome(dto.getOutcome())
      .creditorReferenceId(dto.getCreditorReferenceId())
      .paymentAmountCents(dto.getPaymentAmountCents())
      .description(dto.getDescription())
      .companyName(dto.getCompanyName())
      .officeName(dto.getOfficeName())
      .idPsp(dto.getIdPsp())
      .pspFiscalCode(dto.getPspFiscalCode())
      .pspPartitaIva(dto.getPspPartitaIva())
      .pspCompanyName(dto.getPspCompanyName())
      .idChannel(dto.getIdChannel())
      .channelDescription(dto.getChannelDescription())
      .paymentMethod(dto.getPaymentMethod())
      .feeCents(dto.getFeeCents())
      .paymentDateTime(dto.getPaymentDateTime())
      .applicationDate(dto.getApplicationDate())
      .transferDate(dto.getTransferDate())
      .standin(dto.getStandin())
      .debtor(personMapper.mapToModel(dto.getDebtor()))
      .payer(personMapper.mapToModel(dto.getPayer()))
      .creationDate(dto.getCreationDate().toLocalDateTime())
      .updateDate(dto.getUpdateDate().toLocalDateTime())
      .build();
  }

  public ReceiptDTO mapToDto(Receipt receipt) {
    return ReceiptDTO.builder()
      .receiptId(receipt.getReceiptId())
      .ingestionFlowFileId(receipt.getIngestionFlowFileId())
      .receiptOrigin(receipt.getReceiptOrigin())
      .paymentReceiptId(receipt.getPaymentReceiptId())
      .noticeNumber(receipt.getNoticeNumber())
      .paymentNote(receipt.getPaymentNote())
      .orgFiscalCode(receipt.getOrgFiscalCode())
      .outcome(receipt.getOutcome())
      .creditorReferenceId(receipt.getCreditorReferenceId())
      .paymentAmountCents(receipt.getPaymentAmountCents())
      .description(receipt.getDescription())
      .companyName(receipt.getCompanyName())
      .officeName(receipt.getOfficeName())
      .idPsp(receipt.getIdPsp())
      .pspFiscalCode(receipt.getPspFiscalCode())
      .pspPartitaIva(receipt.getPspPartitaIva())
      .pspCompanyName(receipt.getPspCompanyName())
      .idChannel(receipt.getIdChannel())
      .channelDescription(receipt.getChannelDescription())
      .paymentMethod(receipt.getPaymentMethod())
      .feeCents(receipt.getFeeCents())
      .paymentDateTime(receipt.getPaymentDateTime())
      .applicationDate(receipt.getApplicationDate())
      .transferDate(receipt.getTransferDate())
      .standin(receipt.isStandin())
      .debtor(personMapper.mapToDto(receipt.getDebtor()))
      .payer(personMapper.mapToDto(receipt.getPayer()))
      .creationDate(localDatetimeToOffsetDateTime(receipt.getCreationDate()))
      .updateDate(localDatetimeToOffsetDateTime(receipt.getUpdateDate()))
      .build();
  }


}
