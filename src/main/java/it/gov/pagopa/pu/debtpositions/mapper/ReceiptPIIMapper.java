package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import org.springframework.stereotype.Service;

@Service
public class ReceiptPIIMapper extends BasePIIMapper<ReceiptDTO, ReceiptNoPII, ReceiptPIIDTO> {

  private final DataCipherService dataCipherService;

  public ReceiptPIIMapper(DataCipherService dataCipherService, PersonalDataService personalDataService) {
    super(ReceiptPIIDTO.class, personalDataService);
    this.dataCipherService = dataCipherService;
  }

  @Override
  protected ReceiptNoPII extractNoPiiEntity(ReceiptDTO fullDTO) {
    ReceiptNoPII receiptNoPII = new ReceiptNoPII();

    receiptNoPII.setReceiptId(fullDTO.getReceiptId());
    receiptNoPII.setIngestionFlowFileId(fullDTO.getIngestionFlowFileId());
    receiptNoPII.setReceiptOrigin(fullDTO.getReceiptOrigin());
    receiptNoPII.setRtFilePath(fullDTO.getRtFilePath());
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
    receiptNoPII.setStandin(Boolean.TRUE.equals(fullDTO.getStandin()));
    receiptNoPII.setCreationDate(Utilities.offsetDateTimeToLocalDateTime(fullDTO.getCreationDate()));
    receiptNoPII.setUpdateDate(Utilities.offsetDateTimeToLocalDateTime(fullDTO.getUpdateDate()));
    receiptNoPII.setUpdateOperatorExternalId(fullDTO.getUpdateOperatorExternalId());
    receiptNoPII.setUpdateTraceId(fullDTO.getUpdateTraceId());
    receiptNoPII.setDebtorFiscalCodeHash(dataCipherService.hash(fullDTO.getDebtor().getFiscalCode()));
    receiptNoPII.setDebtorEntityType(fullDTO.getDebtor().getEntityType());

    return receiptNoPII;
  }

  @Override
  protected ReceiptPIIDTO extractPiiDto(ReceiptDTO fullDTO) {
    return ReceiptPIIDTO.builder()
      .debtor(fullDTO.getDebtor())
      .payer(fullDTO.getPayer())
      .build();
  }

  @Override
  public ReceiptDTO map(ReceiptNoPII noPii) {
    ReceiptPIIDTO pii = personalDataService.get(noPii.getPersonalDataId(), ReceiptPIIDTO.class);
    return map(noPii, pii);
  }

  @Override
  protected ReceiptDTO map(ReceiptNoPII noPii, ReceiptPIIDTO pii) {
    return ReceiptDTO.builder()
      .receiptId(noPii.getReceiptId())
      .ingestionFlowFileId(noPii.getIngestionFlowFileId())
      .receiptOrigin(noPii.getReceiptOrigin())
      .rtFilePath(noPii.getRtFilePath())
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
      .creationDate(Utilities.localDatetimeToOffsetDateTime(noPii.getCreationDate()))
      .updateDate(Utilities.localDatetimeToOffsetDateTime(noPii.getUpdateDate()))
      .updateOperatorExternalId(noPii.getUpdateOperatorExternalId())
      .updateTraceId(noPii.getUpdateTraceId())
      .debtor(pii.getDebtor())
      .payer(pii.getPayer())
      .noPII(noPii)
      .build();
  }

}
