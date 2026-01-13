package it.gov.pagopa.pu.debtpositions.service.update.applier;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.checkImmutableField;

@Service
public class DebtPositionManageApplierService {

  public void merge(InstallmentDTO updatedInstallment, InstallmentDTO storedInstallment) {
    storedInstallment.setDueDate(updatedInstallment.getDueDate());
    storedInstallment.setRemittanceInformation(updatedInstallment.getRemittanceInformation());
    storedInstallment.setBalance(updatedInstallment.getBalance());
    storedInstallment.setLegacyPaymentMetadata(updatedInstallment.getLegacyPaymentMetadata());
    storedInstallment.setAmountCents(updatedInstallment.getAmountCents());
    storedInstallment.setSourceFlowName(updatedInstallment.getSourceFlowName());
    List<String> modifiedFields = new ArrayList<>();
    checkImmutableField("paymentOptionId", updatedInstallment.getPaymentOptionId(), storedInstallment.getPaymentOptionId(), modifiedFields);
    checkImmutableField("iupdPagopa", updatedInstallment.getIupdPagopa(), storedInstallment.getIupdPagopa(), modifiedFields);
    checkImmutableField("iuv", updatedInstallment.getIuv(), storedInstallment.getIuv(), modifiedFields);
    checkImmutableField("iud", updatedInstallment.getIud(), storedInstallment.getIud(), modifiedFields);
    checkImmutableField("iur", updatedInstallment.getIur(), storedInstallment.getIur(), modifiedFields);
    checkImmutableField("iuf", updatedInstallment.getIuf(), storedInstallment.getIuf(), modifiedFields);
    checkImmutableField("nav", updatedInstallment.getNav(), storedInstallment.getNav(), modifiedFields);
    checkImmutableField("ingestionFlowFileId", updatedInstallment.getIngestionFlowFileId(), storedInstallment.getIngestionFlowFileId(), modifiedFields);
    checkImmutableField("ingestionFlowFileLineNumber", updatedInstallment.getIngestionFlowFileLineNumber(), storedInstallment.getIngestionFlowFileLineNumber(), modifiedFields);
    checkImmutableField("receiptId", updatedInstallment.getReceiptId(), storedInstallment.getReceiptId(), modifiedFields);
    checkImmutableField("notificationDate", updatedInstallment.getNotificationDate(), storedInstallment.getNotificationDate(), modifiedFields);
    checkImmutableField("notificationFeeCents", updatedInstallment.getNotificationFeeCents(), storedInstallment.getNotificationFeeCents(), modifiedFields);

    mergeDebtorFields(updatedInstallment.getDebtor(), storedInstallment.getDebtor(), modifiedFields);

    if (!modifiedFields.isEmpty()) {
      throw new ConflictErrorException(String.format("[IMMUTABLE_FIELD] These fields for installment having id %s are not mutable: %s", storedInstallment.getInstallmentId(), modifiedFields));
    }

    if (storedInstallment.getTransfers().size() != updatedInstallment.getTransfers().size()) {
      throw new ConflictErrorException(String.format("[IMMUTABLE_FIELD] The number of beneficiary for installment having id %s cannot be modified", storedInstallment.getInstallmentId()));
    }

    updateTransferList(storedInstallment, updatedInstallment);
  }

  private void mergeDebtorFields(PersonDTO updatedDebtor, PersonDTO storedDebtor, List<String> modifiedFields) {
    storedDebtor.setFullName(updatedDebtor.getFullName());
    storedDebtor.setAddress(updatedDebtor.getAddress());
    storedDebtor.setCivic(updatedDebtor.getCivic());
    storedDebtor.setPostalCode(updatedDebtor.getPostalCode());
    storedDebtor.setLocation(updatedDebtor.getLocation());
    storedDebtor.setProvince(updatedDebtor.getProvince());
    storedDebtor.setNation(updatedDebtor.getNation());
    storedDebtor.setEmail(updatedDebtor.getEmail());

    List<String> modifiedDebtorFields = new ArrayList<>();
    checkImmutableField("entityType", updatedDebtor.getEntityType(), storedDebtor.getEntityType(), modifiedDebtorFields);
    checkImmutableField("fiscalCode", updatedDebtor.getFiscalCode(), storedDebtor.getFiscalCode(), modifiedDebtorFields);

    if(!modifiedDebtorFields.isEmpty()) {
      modifiedFields.add("debtor: " + modifiedDebtorFields);
    }
  }

  private void updateTransferList(InstallmentDTO storedInstallment, InstallmentDTO updatedInstallment) {
    Map<Long, TransferDTO> mapIndexTransferUpdated = updatedInstallment.getTransfers().stream()
      .collect(Collectors.toMap(TransferDTO::getTransferId, Function.identity()));

    Long totalAmountOtherTransfersUpdated = updatedInstallment.getTransfers().stream()
      .filter(transferDTO -> transferDTO.getTransferIndex() != 1)
      .mapToLong(TransferDTO::getAmountCents).sum();

    storedInstallment.getTransfers()
      .forEach(transferDTO -> {
        if (mapIndexTransferUpdated.get(transferDTO.getTransferId()) == null){
          throw new ConflictErrorException(String.format("[TRANSFER_NOT_FOUND] The transfer having id %s of installment having id %s does not found", transferDTO.getTransferId(), storedInstallment.getInstallmentId()));
        }
        if(transferDTO.getTransferIndex() == 1){
          mapIndexTransferUpdated.get(transferDTO.getTransferId()).setAmountCents(storedInstallment.getAmountCents() - totalAmountOtherTransfersUpdated);
          mapIndexTransferUpdated.get(transferDTO.getTransferId()).setRemittanceInformation(storedInstallment.getRemittanceInformation());
        }

        mergeTransfer(transferDTO, mapIndexTransferUpdated.get(transferDTO.getTransferId()), storedInstallment.getInstallmentId());
      });
  }

  private void mergeTransfer(TransferDTO storedTransfer, TransferDTO updatedTransfer, Long installmentId) {
    storedTransfer.setAmountCents(updatedTransfer.getAmountCents());
    storedTransfer.setRemittanceInformation(updatedTransfer.getRemittanceInformation());
    storedTransfer.setIban(updatedTransfer.getIban());
    storedTransfer.setPostalIban(updatedTransfer.getPostalIban());

    List<String> modifiedFields = new ArrayList<>();
    checkImmutableField("installmentId", storedTransfer.getInstallmentId(), updatedTransfer.getInstallmentId(), modifiedFields);
    checkImmutableField("transferIndex", storedTransfer.getTransferIndex(), updatedTransfer.getTransferIndex(), modifiedFields);
    checkImmutableField("orgFiscalCode", storedTransfer.getOrgFiscalCode(), updatedTransfer.getOrgFiscalCode(), modifiedFields);
    checkImmutableField("orgName", storedTransfer.getOrgName(), updatedTransfer.getOrgName(), modifiedFields);
    checkImmutableField("category", storedTransfer.getCategory(), updatedTransfer.getCategory(), modifiedFields);
    checkImmutableField("stampType", storedTransfer.getStampType(), updatedTransfer.getStampType(), modifiedFields);
    checkImmutableField("stampHashDocument", storedTransfer.getStampHashDocument(), updatedTransfer.getStampHashDocument(), modifiedFields);
    checkImmutableField("stampProvincialResidence", storedTransfer.getStampProvincialResidence(), updatedTransfer.getStampProvincialResidence(), modifiedFields);

    if (!modifiedFields.isEmpty()) {
      throw new ConflictErrorException(String.format("[IMMUTABLE_FIELD] These fields for transfer with index %s of installment having id %s are not mutable: %s",
        storedTransfer.getTransferIndex(), installmentId, modifiedFields));
    }
  }

}
