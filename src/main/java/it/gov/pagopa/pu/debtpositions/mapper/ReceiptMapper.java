package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

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
      .rtFilePath(dto.getRtFilePath())
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
      .standin(BooleanUtils.isTrue(dto.getStandin()))
      .debtor(personMapper.mapToModel(dto.getDebtor()))
      .payer(Optional.ofNullable(dto.getPayer()).map(personMapper::mapToModel).orElse(null))
      .creationDate(Optional.ofNullable(dto.getCreationDate()).map(OffsetDateTime::toLocalDateTime).orElse(null))
      .updateDate(Optional.ofNullable(dto.getUpdateDate()).map(OffsetDateTime::toLocalDateTime).orElse(null))
      .build();
  }

  public ReceiptDTO mapToDto(Receipt receipt) {
    return ReceiptDTO.builder()
      .receiptId(receipt.getReceiptId())
      .ingestionFlowFileId(receipt.getIngestionFlowFileId())
      .receiptOrigin(receipt.getReceiptOrigin())
      .rtFilePath(receipt.getRtFilePath())
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
      .payer(Optional.ofNullable(receipt.getPayer()).map(personMapper::mapToDto).orElse(null))
      .creationDate(localDatetimeToOffsetDateTime(receipt.getCreationDate()))
      .updateDate(localDatetimeToOffsetDateTime(receipt.getUpdateDate()))
      .build();
  }


}
