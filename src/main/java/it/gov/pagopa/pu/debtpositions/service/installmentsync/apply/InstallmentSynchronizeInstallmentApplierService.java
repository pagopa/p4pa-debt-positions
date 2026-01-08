package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.checkImmutableField;

@Service
public class InstallmentSynchronizeInstallmentApplierService {

  public void merge(InstallmentSynchronizeDTO installmentSynchronizeDTO, InstallmentDTO installmentDTO) {
    installmentDTO.setDueDate(installmentSynchronizeDTO.getDueDate());
    installmentDTO.setAmountCents(installmentSynchronizeDTO.getAmountCents());
    installmentDTO.setRemittanceInformation(installmentSynchronizeDTO.getRemittanceInformation());
    installmentDTO.setBalance(installmentSynchronizeDTO.getBalance());
    installmentDTO.setNotificationDate(installmentSynchronizeDTO.getNotificationDate());
    installmentDTO.setIngestionFlowFileId(installmentSynchronizeDTO.getIngestionFlowFileId());
    installmentDTO.setIngestionFlowFileLineNumber(installmentSynchronizeDTO.getIngestionFlowFileLineNumber());
    installmentDTO.setIngestionFlowFileAction(installmentSynchronizeDTO.getAction());
    installmentDTO.setSourceFlowName(installmentSynchronizeDTO.getIngestionFlowFileName());

    List<String> modifiedFields = new ArrayList<>();
    checkImmutableField("iuv", installmentSynchronizeDTO.getIuv(), installmentDTO.getIuv(), modifiedFields);
    checkImmutableField("generateNotice", installmentSynchronizeDTO.getGenerateNotice(), installmentDTO.getGenerateNotice(), modifiedFields);
    checkImmutableField("legacyPaymentMetadata", installmentSynchronizeDTO.getLegacyPaymentMetadata(), installmentDTO.getLegacyPaymentMetadata(), modifiedFields);

    mergeDebtorFields(installmentSynchronizeDTO, installmentDTO.getDebtor(), modifiedFields);

    if (!modifiedFields.isEmpty()) {
      throw new ConflictErrorException(String.format("[UNMODIFIABLE_FIELD] These fields for installment with iud %s are not mutable: %s", installmentDTO.getIud(), modifiedFields));
    }

    if(installmentSynchronizeDTO.getAdditionalTransfers().size() != installmentSynchronizeDTO.getNumberBeneficiary()) {
      throw new ConflictErrorException(String.format("[INSTALLMENT_INCONSISTENCY] The number of beneficiary for installment with iud %s does not match with the size of the list", installmentDTO.getIud()));
    }

    if (installmentDTO.getTransfers().size() != installmentSynchronizeDTO.getNumberBeneficiary()) {
      throw new ConflictErrorException(String.format("[UNMODIFIABLE_FIELD] The number of beneficiary for installment with iud %s cannot be modified", installmentDTO.getIud()));
    }

    updateTransferList(installmentDTO, installmentSynchronizeDTO);
  }

  private void updateTransferList(InstallmentDTO installmentDTO, InstallmentSynchronizeDTO installmentSynchronizeDTO) {
    Map<Integer, TransferSynchronizeDTO> mapIndexTransferSync = installmentSynchronizeDTO.getAdditionalTransfers().stream()
      .collect(Collectors.toMap(TransferSynchronizeDTO::getTransferIndex, Function.identity()));

    installmentDTO.getTransfers()
      .forEach(transferDTO -> {
        if (mapIndexTransferSync.get(transferDTO.getTransferIndex()) == null){
          throw new ConflictErrorException(String.format("[INSTALLMENT_INCONSISTENCY] The transfer with index %s for installment with iud %s does not found", transferDTO.getTransferIndex(), installmentDTO.getIud()));
        }
        mergeTransfer(transferDTO, mapIndexTransferSync.get(transferDTO.getTransferIndex()), installmentDTO.getIud());
      });
  }

  private void mergeTransfer(TransferDTO transferDTO, TransferSynchronizeDTO transferSynchronizeDTO, String iud) {
    transferDTO.setAmountCents(transferSynchronizeDTO.getAmountCents());
    transferDTO.setRemittanceInformation(transferSynchronizeDTO.getRemittanceInformation());

    List<String> modifiedFields = new ArrayList<>();
    checkImmutableField("orgFiscalCode", transferDTO.getOrgFiscalCode(), transferSynchronizeDTO.getOrgFiscalCode(), modifiedFields);
    checkImmutableField("orgName", transferDTO.getOrgName(), transferSynchronizeDTO.getOrgName(), modifiedFields);
    checkImmutableField("iban", transferDTO.getIban(), transferSynchronizeDTO.getIban(), modifiedFields);
    checkImmutableField("category", transferDTO.getCategory(), transferSynchronizeDTO.getCategory(), modifiedFields);

    if (!modifiedFields.isEmpty()) {
      throw new ConflictErrorException(String.format("[UNMODIFIABLE_FIELD] These fields for transfer with index %s of installment with iud %s are not mutable: %s",
        transferDTO.getTransferIndex(), iud, modifiedFields));
    }
  }

  private void mergeDebtorFields(InstallmentSynchronizeDTO updatedInstallment, PersonDTO storedDebtor, List<String> modifiedFields) {
    storedDebtor.setFullName(updatedInstallment.getFullName());
    storedDebtor.setAddress(updatedInstallment.getAddress());
    storedDebtor.setCivic(updatedInstallment.getCivic());
    storedDebtor.setPostalCode(updatedInstallment.getPostalCode());
    storedDebtor.setLocation(updatedInstallment.getLocation());
    storedDebtor.setProvince(updatedInstallment.getProvince());
    storedDebtor.setNation(updatedInstallment.getNation());
    storedDebtor.setEmail(updatedInstallment.getEmail());

    List<String> modifiedDebtorFields = new ArrayList<>();
    checkImmutableField("entityType", updatedInstallment.getEntityType(), storedDebtor.getEntityType(), modifiedDebtorFields);
    checkImmutableField("fiscalCode", updatedInstallment.getFiscalCode(), storedDebtor.getFiscalCode(), modifiedDebtorFields);

    if(!modifiedDebtorFields.isEmpty()) {
      modifiedFields.add("debtor: " + modifiedDebtorFields);
    }
  }
}
