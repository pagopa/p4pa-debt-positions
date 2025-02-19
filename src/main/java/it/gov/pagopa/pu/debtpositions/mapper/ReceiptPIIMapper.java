package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class ReceiptPIIMapper extends BasePIIMapper<Receipt, ReceiptNoPII, ReceiptPIIDTO> {

  private final DataCipherService dataCipherService;
  private final PersonalDataService personalDataService;
  private final PersonMapper personMapper;

  public ReceiptPIIMapper(DataCipherService dataCipherService, PersonalDataService personalDataService,
    PersonMapper personMapper) {
    this.dataCipherService = dataCipherService;
    this.personalDataService = personalDataService;
    this.personMapper = personMapper;
  }

  @Override
  protected ReceiptNoPII extractNoPiiEntity(Receipt fullDTO) {
    ReceiptNoPII receiptNoPII = new ReceiptNoPII();

    receiptNoPII.setReceiptId(fullDTO.getReceiptId());
    receiptNoPII.setIngestionFlowFileId(fullDTO.getIngestionFlowFileId());
    receiptNoPII.setReceiptOrigin(fullDTO.getReceiptOrigin());
    receiptNoPII.setPaymentReceiptId(fullDTO.getPaymentReceiptId());
    receiptNoPII.setNoticeNumber(fullDTO.getNoticeNumber());
    receiptNoPII.setPaymentNote(fullDTO.getPaymentNote());
    receiptNoPII.setOrgFiscalCode(fullDTO.getOrgFiscalCode());
    receiptNoPII.setOutcome(fullDTO.getOutcome());
    receiptNoPII.setCreditorReferenceId(fullDTO.getCreditorReferenceId());
    receiptNoPII.setPaymentAmountCents(fullDTO.getPaymentAmountCents());
    receiptNoPII.setDescription(fullDTO.getDescription());
    receiptNoPII.setCompanyName(fullDTO.getCompanyName());
    receiptNoPII.setOfficeName(fullDTO.getOfficeName());
    receiptNoPII.setIdPsp(fullDTO.getIdPsp());
    receiptNoPII.setPspFiscalCode(fullDTO.getPspFiscalCode());
    receiptNoPII.setPspPartitaIva(fullDTO.getPspPartitaIva());
    receiptNoPII.setPspCompanyName(fullDTO.getPspCompanyName());
    receiptNoPII.setIdChannel(fullDTO.getIdChannel());
    receiptNoPII.setChannelDescription(fullDTO.getChannelDescription());
    receiptNoPII.setPaymentMethod(fullDTO.getPaymentMethod());
    receiptNoPII.setFeeCents(fullDTO.getFeeCents());
    receiptNoPII.setPaymentDateTime(fullDTO.getPaymentDateTime());
    receiptNoPII.setApplicationDate(fullDTO.getApplicationDate());
    receiptNoPII.setTransferDate(fullDTO.getTransferDate());
    receiptNoPII.setStandin(fullDTO.isStandin());
    receiptNoPII.setCreationDate(fullDTO.getCreationDate());
    receiptNoPII.setUpdateDate(fullDTO.getUpdateDate());
    receiptNoPII.setUpdateOperatorExternalId(fullDTO.getUpdateOperatorExternalId());
    receiptNoPII.setDebtorFiscalCodeHash(dataCipherService.hash(fullDTO.getDebtor().getFiscalCode()));
    receiptNoPII.setDebtorEntityType(fullDTO.getDebtor().getEntityType());

    return receiptNoPII;
  }

  @Override
  protected ReceiptPIIDTO extractPiiDto(Receipt fullDTO) {
    return ReceiptPIIDTO.builder()
      .debtor(fullDTO.getDebtor())
      .payer(fullDTO.getPayer())
      .build();
  }

  @Override
  public Receipt map(ReceiptNoPII noPii) {
    ReceiptPIIDTO pii = personalDataService.get(noPii.getPersonalDataId(), ReceiptPIIDTO.class);
    return Receipt.builder()
      .receiptId(noPii.getReceiptId())
      .ingestionFlowFileId(noPii.getIngestionFlowFileId())
      .receiptOrigin(noPii.getReceiptOrigin())
      .paymentReceiptId(noPii.getPaymentReceiptId())
      .noticeNumber(noPii.getNoticeNumber())
      .paymentNote(noPii.getPaymentNote())
      .orgFiscalCode(noPii.getOrgFiscalCode())
      .outcome(noPii.getOutcome())
      .creditorReferenceId(noPii.getCreditorReferenceId())
      .paymentAmountCents(noPii.getPaymentAmountCents())
      .description(noPii.getDescription())
      .companyName(noPii.getCompanyName())
      .officeName(noPii.getOfficeName())
      .idPsp(noPii.getIdPsp())
      .pspFiscalCode(noPii.getPspFiscalCode())
      .pspPartitaIva(noPii.getPspPartitaIva())
      .pspCompanyName(noPii.getPspCompanyName())
      .idChannel(noPii.getIdChannel())
      .channelDescription(noPii.getChannelDescription())
      .paymentMethod(noPii.getPaymentMethod())
      .feeCents(noPii.getFeeCents())
      .paymentDateTime(noPii.getPaymentDateTime())
      .applicationDate(noPii.getApplicationDate())
      .transferDate(noPii.getTransferDate())
      .standin(noPii.isStandin())
      .creationDate(noPii.getCreationDate())
      .updateDate(noPii.getUpdateDate())
      .updateOperatorExternalId(noPii.getUpdateOperatorExternalId())
      .debtor(pii.getDebtor())
      .payer(pii.getPayer())
      .noPII(noPii)
      .build();
  }

  public ReceiptDTO mapToReceiptDTO(ReceiptNoPII receiptNoPII) {
    return mapToReceiptDTO(map(receiptNoPII));
  }
  public ReceiptDTO mapToReceiptDTO(Receipt receipt) {
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
