package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

@Service
public class InstallmentPIIMapper extends BasePIIMapper<Installment, InstallmentNoPII, InstallmentPIIDTO> {

  private final DataCipherService dataCipherService;
  private final PersonalDataService personalDataService;

  public InstallmentPIIMapper(DataCipherService dataCipherService, PersonalDataService personalDataService) {
    this.dataCipherService = dataCipherService;
    this.personalDataService = personalDataService;
  }

  @Override
  protected InstallmentNoPII extractNoPiiEntity(Installment fullDTO) {
    InstallmentNoPII noPII = new InstallmentNoPII();

    noPII.setInstallmentId(fullDTO.getInstallmentId());
    noPII.setPaymentOptionId(fullDTO.getPaymentOptionId());
    noPII.setStatus(fullDTO.getStatus());
    noPII.setIupdPagopa(fullDTO.getIupdPagopa());
    noPII.setIud(fullDTO.getIud());
    noPII.setIuv(fullDTO.getIuv());
    noPII.setIur(fullDTO.getIur());
    noPII.setIuf(fullDTO.getIuf());
    noPII.setNav(fullDTO.getNav());
    noPII.setDueDate(fullDTO.getDueDate());
    noPII.setPaymentTypeCode(fullDTO.getPaymentTypeCode());
    noPII.setAmountCents(fullDTO.getAmountCents());
    noPII.setRemittanceInformation(fullDTO.getRemittanceInformation());
    noPII.setBalance(fullDTO.getBalance());
    noPII.setLegacyPaymentMetadata(fullDTO.getLegacyPaymentMetadata());
    noPII.setDebtorEntityType(fullDTO.getDebtor().getEntityType());
    noPII.setDebtorFiscalCodeHash(dataCipherService.hash(fullDTO.getDebtor().getFiscalCode()));
    noPII.setNotificationDate(fullDTO.getNotificationDate());
    noPII.setIngestionFlowFileId(fullDTO.getIngestionFlowFileId());
    noPII.setIngestionFlowFileLineNumber(fullDTO.getIngestionFlowFileLineNumber());
    noPII.setReceiptId(fullDTO.getReceiptId());
    noPII.setCreationDate(fullDTO.getCreationDate());
    noPII.setUpdateDate(fullDTO.getUpdateDate());
    noPII.setUpdateOperatorExternalId(fullDTO.getUpdateOperatorExternalId());
    noPII.setTransfers(new TreeSet<>(fullDTO.getTransfers()));

    if(fullDTO.getSyncStatus() != null){
      noPII.setSyncStatus(it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus.builder()
        .syncStatusFrom(fullDTO.getSyncStatus().getSyncStatusFrom())
        .syncStatusTo(fullDTO.getSyncStatus().getSyncStatusTo()).build());
    }

    return noPII;
  }

  @Override
  protected InstallmentPIIDTO extractPiiDto(Installment fullDTO) {
    return InstallmentPIIDTO.builder()
      .debtor(fullDTO.getDebtor())
      .build();
  }

  @Override
  public Installment map(InstallmentNoPII noPii) {
    InstallmentPIIDTO pii = personalDataService.get(noPii.getPersonalDataId(), InstallmentPIIDTO.class);
    Installment installment = Installment.builder()
      .installmentId(noPii.getInstallmentId())
      .paymentOptionId(noPii.getPaymentOptionId())
      .status(noPii.getStatus())
      .iupdPagopa(noPii.getIupdPagopa())
      .iud(noPii.getIud())
      .iuv(noPii.getIuv())
      .iur(noPii.getIur())
      .iuf(noPii.getIuf())
      .nav(noPii.getNav())
      .dueDate(noPii.getDueDate())
      .paymentTypeCode(noPii.getPaymentTypeCode())
      .amountCents(noPii.getAmountCents())
      .remittanceInformation(noPii.getRemittanceInformation())
      .balance(noPii.getBalance())
      .legacyPaymentMetadata(noPii.getLegacyPaymentMetadata())
      .notificationDate(noPii.getNotificationDate())
      .ingestionFlowFileId(noPii.getIngestionFlowFileId())
      .ingestionFlowFileLineNumber(noPii.getIngestionFlowFileLineNumber())
      .receiptId(noPii.getReceiptId())
      .creationDate(noPii.getCreationDate())
      .updateDate(noPii.getUpdateDate())
      .updateOperatorExternalId(noPii.getUpdateOperatorExternalId())
      .debtor(pii.getDebtor())
      .transfers(Optional.ofNullable(noPii.getTransfers()).map(List::copyOf).orElse(List.of()))
      .noPII(noPii)
      .build();

    if(noPii.getSyncStatus() != null) {
      installment.setSyncStatus(InstallmentSyncStatus.builder()
        .syncStatusFrom(noPii.getSyncStatus().getSyncStatusFrom())
        .syncStatusTo(noPii.getSyncStatus().getSyncStatusTo())
        .build());
    }
    return installment;
  }
}
