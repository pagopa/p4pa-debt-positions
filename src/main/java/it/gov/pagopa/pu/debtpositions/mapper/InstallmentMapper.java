package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class InstallmentMapper {

  private final PersonMapper personMapper;

  private final TransferMapper transferMapper;

  public InstallmentMapper(PersonMapper personMapper, TransferMapper transferMapper) {
    this.personMapper = personMapper;
    this.transferMapper = transferMapper;
  }

  public Installment mapToModel(InstallmentDTO dto) {
    Installment installment = new Installment();
    installment.setInstallmentId(dto.getInstallmentId());
    installment.setPaymentOptionId(dto.getPaymentOptionId());
    installment.setStatus(dto.getStatus());
    installment.setSyncStatus(dto.getSyncStatus());
    installment.setIupdPagopa(dto.getIupdPagopa());
    installment.setIud(dto.getIud());
    installment.setIuv(dto.getIuv());
    installment.setIur(dto.getIur());
    installment.setIuf(dto.getIuf());
    installment.setNav(dto.getNav());
    installment.setDueDate(dto.getDueDate());
    installment.setPaymentTypeCode(dto.getPaymentTypeCode());
    installment.setAmountCents(dto.getAmountCents());
    installment.setRemittanceInformation(dto.getRemittanceInformation());
    installment.setBalance(dto.getBalance());
    installment.setLegacyPaymentMetadata(dto.getLegacyPaymentMetadata());
    installment.setDebtor(personMapper.mapToModel(dto.getDebtor()));
    installment.setTransfers(dto.getTransfers().stream()
      .map(transferMapper::mapToModel)
      .toList());
    installment.setNotificationDate(dto.getNotificationDate());
    installment.setIngestionFlowFileId(dto.getIngestionFlowFileId());
    installment.setIngestionFlowFileLineNumber(dto.getIngestionFlowFileLineNumber());
    installment.setReceiptId(dto.getReceiptId());
    installment.setCreationDate(dto.getCreationDate().toLocalDateTime());
    installment.setUpdateDate(dto.getUpdateDate().toLocalDateTime());
    return installment;
  }

  public InstallmentDTO mapToDto(InstallmentNoPII installment) {
    InstallmentDTO installmentDTO = InstallmentDTO.builder()
      .installmentId(installment.getInstallmentId())
      .paymentOptionId(installment.getPaymentOptionId())
      .status(installment.getStatus())
      .iupdPagopa(installment.getIupdPagopa())
      .iud(installment.getIud())
      .iuv(installment.getIuv())
      .iur(installment.getIur())
      .iuf(installment.getIuf())
      .nav(installment.getNav())
      .dueDate(installment.getDueDate())
      .paymentTypeCode(installment.getPaymentTypeCode())
      .amountCents(installment.getAmountCents())
      .remittanceInformation(installment.getRemittanceInformation())
      .balance(installment.getBalance())
      .legacyPaymentMetadata(installment.getLegacyPaymentMetadata())
      .transfers(installment.getTransfers().stream()
        .map(transferMapper::mapToDto)
        .toList())
      .notificationDate(installment.getNotificationDate())
      .ingestionFlowFileId(installment.getIngestionFlowFileId())
      .ingestionFlowFileLineNumber(installment.getIngestionFlowFileLineNumber())
      .receiptId(installment.getReceiptId())
      .creationDate(localDatetimeToOffsetDateTime(installment.getCreationDate()))
      .updateDate(localDatetimeToOffsetDateTime(installment.getUpdateDate()))
      .build();

    if(installment.getSyncStatus() != null) {
      InstallmentSyncStatus installmentSyncStatus = InstallmentSyncStatus.builder()
        .syncStatusFrom(installment.getSyncStatus().getSyncStatusFrom())
        .syncStatusTo(installment.getSyncStatus().getSyncStatusTo()).build();
      installmentDTO.setSyncStatus(installmentSyncStatus);
    }

    return installmentDTO;
  }

  public InstallmentDTO mapToDto(Installment installment) {
    return InstallmentDTO.builder()
      .installmentId(installment.getInstallmentId())
      .paymentOptionId(installment.getPaymentOptionId())
      .status(installment.getStatus())
      .syncStatus(installment.getSyncStatus())
      .iupdPagopa(installment.getIupdPagopa())
      .iud(installment.getIud())
      .iuv(installment.getIuv())
      .iur(installment.getIur())
      .iuf(installment.getIuf())
      .nav(installment.getNav())
      .dueDate(installment.getDueDate())
      .paymentTypeCode(installment.getPaymentTypeCode())
      .amountCents(installment.getAmountCents())
      .remittanceInformation(installment.getRemittanceInformation())
      .balance(installment.getBalance())
      .legacyPaymentMetadata(installment.getLegacyPaymentMetadata())
      .debtor(null) //TODO P4ADEV-2028
      .transfers(installment.getTransfers().stream()
        .map(transferMapper::mapToDto)
        .toList())
      .notificationDate(installment.getNotificationDate())
      .ingestionFlowFileId(installment.getIngestionFlowFileId())
      .ingestionFlowFileLineNumber(installment.getIngestionFlowFileLineNumber())
      .receiptId(installment.getReceiptId())
      .creationDate(localDatetimeToOffsetDateTime(installment.getCreationDate()))
      .updateDate(localDatetimeToOffsetDateTime(installment.getUpdateDate()))
      .build();
  }


}
