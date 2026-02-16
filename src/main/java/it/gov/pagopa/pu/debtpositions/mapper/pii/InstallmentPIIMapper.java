package it.gov.pagopa.pu.debtpositions.mapper.pii;

import it.gov.pagopa.pu.common.pii.citizen.service.DataCipherService;
import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.mapper.BaseEntityPIIMapper;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.mapper.TransferMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.localDatetimeToOffsetDateTime;

@Service
public class InstallmentPIIMapper extends BaseEntityPIIMapper<InstallmentDTO, InstallmentNoPII, InstallmentPIIDTO> {

  private final DataCipherService dataCipherService;

  private final TransferMapper transferMapper;

  public InstallmentPIIMapper(DataCipherService dataCipherService, PersonalDataService personalDataService, TransferMapper transferMapper) {
    super(InstallmentPIIDTO.class, personalDataService);
    this.dataCipherService = dataCipherService;
    this.transferMapper = transferMapper;
  }

  @Override
  protected InstallmentNoPII extractNoPiiEntity(InstallmentDTO fullDTO) {
    InstallmentNoPII noPII = new InstallmentNoPII();

    noPII.setInstallmentId(fullDTO.getInstallmentId());
    noPII.setPaymentOptionId(fullDTO.getPaymentOptionId());
    noPII.setStatus(fullDTO.getStatus());
    noPII.setSyncStatus(fullDTO.getSyncStatus());
    noPII.setIupdPagopa(fullDTO.getIupdPagopa());
    noPII.setGenerateNotice(Optional.ofNullable(fullDTO.getGenerateNotice()).orElse(false));
    noPII.setIud(fullDTO.getIud());
    noPII.setIuv(fullDTO.getIuv());
    noPII.setIur(fullDTO.getIur());
    noPII.setIuf(fullDTO.getIuf());
    noPII.setNav(fullDTO.getNav());
    noPII.setIun(fullDTO.getIun());
    noPII.setDueDate(fullDTO.getDueDate());
    noPII.setSwitchToExpired(Optional.ofNullable(fullDTO.getSwitchToExpired()).orElse(false));
    noPII.setNotificationFeeCents(fullDTO.getNotificationFeeCents());
    noPII.setAmountCents(fullDTO.getAmountCents());
    noPII.setRemittanceInformation(fullDTO.getRemittanceInformation());
    noPII.setBalance(fullDTO.getBalance());
    noPII.setLegacyPaymentMetadata(fullDTO.getLegacyPaymentMetadata());
    noPII.setDebtorEntityType(fullDTO.getDebtor().getEntityType());
    noPII.setDebtorFiscalCodeHash(dataCipherService.hash(fullDTO.getDebtor().getFiscalCode()));
    noPII.setNotificationDate(fullDTO.getNotificationDate());
    noPII.setIngestionFlowFileId(fullDTO.getIngestionFlowFileId());
    noPII.setIngestionFlowFileLineNumber(fullDTO.getIngestionFlowFileLineNumber());
    noPII.setIngestionFlowFileAction(fullDTO.getIngestionFlowFileAction());
    noPII.setSourceFlowName(fullDTO.getSourceFlowName());
    noPII.setReceiptId(fullDTO.getReceiptId());
    noPII.setCreationDate(Utilities.offsetDateTimeToLocalDateTime(fullDTO.getCreationDate()));
    noPII.setUpdateDate(Utilities.offsetDateTimeToLocalDateTime(fullDTO.getUpdateDate()));
    noPII.setUpdateOperatorExternalId(fullDTO.getUpdateOperatorExternalId());
    noPII.setUpdateTraceId(fullDTO.getUpdateTraceId());
    noPII.setTransfers(fullDTO.getTransfers().stream().map(transferMapper::mapToModel).collect(Collectors.toCollection(TreeSet::new)));

    return noPII;
  }

  @Override
  protected InstallmentPIIDTO extractPiiDto(InstallmentDTO fullDTO) {
    return InstallmentPIIDTO.builder()
      .debtor(fullDTO.getDebtor())
      .originalRemittanceInformation(fullDTO.getOriginalRemittanceInformation())
      .build();
  }

  @Override
  public InstallmentDTO map(InstallmentNoPII noPii) {
    InstallmentPIIDTO pii = personalDataService.get(noPii.getPersonalDataId(), InstallmentPIIDTO.class);
    return map(noPii, pii);
  }

  @Override
  protected InstallmentDTO map(InstallmentNoPII noPii, InstallmentPIIDTO pii) {
    return InstallmentDTO.builder()
      .installmentId(noPii.getInstallmentId())
      .paymentOptionId(noPii.getPaymentOptionId())
      .status(noPii.getStatus())
      .syncStatus(noPii.getSyncStatus())
      .iupdPagopa(noPii.getIupdPagopa())
      .generateNotice(noPii.isGenerateNotice())
      .iud(noPii.getIud())
      .iuv(noPii.getIuv())
      .iur(noPii.getIur())
      .iuf(noPii.getIuf())
      .nav(noPii.getNav())
      .iun(noPii.getIun())
      .dueDate(noPii.getDueDate())
      .switchToExpired(noPii.isSwitchToExpired())
      .notificationFeeCents(noPii.getNotificationFeeCents())
      .amountCents(noPii.getAmountCents())
      .remittanceInformation(noPii.getRemittanceInformation())
      .balance(noPii.getBalance())
      .legacyPaymentMetadata(noPii.getLegacyPaymentMetadata())
      .notificationDate(noPii.getNotificationDate())
      .ingestionFlowFileId(noPii.getIngestionFlowFileId())
      .ingestionFlowFileLineNumber(noPii.getIngestionFlowFileLineNumber())
      .ingestionFlowFileAction(noPii.getIngestionFlowFileAction())
      .sourceFlowName(noPii.getSourceFlowName())
      .receiptId(noPii.getReceiptId())
      .creationDate(Utilities.localDatetimeToOffsetDateTime(noPii.getCreationDate()))
      .updateDate(Utilities.localDatetimeToOffsetDateTime(noPii.getUpdateDate()))
      .updateOperatorExternalId(noPii.getUpdateOperatorExternalId())
      .updateTraceId(noPii.getUpdateTraceId())
      .debtor(pii.getDebtor())
      .transfers(Optional.ofNullable(noPii.getTransfers())
        .map(ts -> ts.stream().map(transferMapper::mapToDto).toList())
        .orElse(List.of()))
      .originalRemittanceInformation(pii.getOriginalRemittanceInformation())
      .noPII(noPii)
      .build();
  }

  public static void setToDtoAutoDbFields(InstallmentDTO installmentDTO, InstallmentNoPII installment){
    installmentDTO.setInstallmentId(installment.getInstallmentId());
    installmentDTO.setCreationDate(localDatetimeToOffsetDateTime(installment.getCreationDate()));
    installmentDTO.setUpdateDate(localDatetimeToOffsetDateTime(installment.getUpdateDate()));
    installmentDTO.setUpdateOperatorExternalId(installment.getUpdateOperatorExternalId());
    installmentDTO.setUpdateTraceId(installment.getUpdateTraceId());
  }
}
