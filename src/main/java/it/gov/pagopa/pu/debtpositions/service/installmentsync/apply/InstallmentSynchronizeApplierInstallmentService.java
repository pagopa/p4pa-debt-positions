package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import org.springframework.stereotype.Service;

import java.util.*;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.checkImmutableField;

@Service
public class InstallmentSynchronizeApplierInstallmentService {

  public void merge(InstallmentSynchronizeDTO installmentSynchronizeDTO, InstallmentDTO installmentDTO) {
    installmentDTO.setDueDate(installmentSynchronizeDTO.getDueDate());
    installmentDTO.setAmountCents(installmentSynchronizeDTO.getAmountCents());
    installmentDTO.setRemittanceInformation(installmentSynchronizeDTO.getRemittanceInformation());
    installmentDTO.setBalance(installmentSynchronizeDTO.getBalance());
    installmentDTO.setLegacyPaymentMetadata(installmentSynchronizeDTO.getLegacyPaymentMetadata());
    installmentDTO.setNotificationDate(installmentSynchronizeDTO.getNotificationDate());

    Set<String> modifiedFields = new HashSet<>();
    checkImmutableField("iuv", installmentSynchronizeDTO.getIuv(), installmentDTO.getIuv(), modifiedFields);
    checkImmutableField("paymentTypeCode", installmentSynchronizeDTO.getPaymentTypeCode(), installmentDTO.getPaymentTypeCode(), modifiedFields);
    checkImmutableField("ingestionFlowFileId", installmentSynchronizeDTO.getIngestionFlowFileId(), installmentDTO.getIngestionFlowFileId(), modifiedFields);
    checkImmutableField("ingestionFlowFileLineNumber", installmentSynchronizeDTO.getIngestionFlowFileLineNumber(), installmentDTO.getIngestionFlowFileLineNumber(), modifiedFields);

    if (!modifiedFields.isEmpty()) {
      throw new ConflictErrorException("These fields for installment are not mutable: " + modifiedFields);
    }

  }

  //TODO
  private void updateTransferList(List<TransferDTO> transferDTOList, List<TransferSynchronizeDTO> transferSynchronizeDTOList) {

    Map<Integer, TransferSynchronizeDTO> mapIndexTransferSync = new HashMap<>();
    for (TransferSynchronizeDTO transferSynchronizeDTO : transferSynchronizeDTOList) {
      mapIndexTransferSync.put(transferSynchronizeDTO.getTransferIndex(), transferSynchronizeDTO);
    }

    for(TransferDTO transferDTO : transferDTOList){
      if(mapIndexTransferSync.containsKey(transferDTO.getTransferIndex())){
        mergeTransfer(transferDTO, mapIndexTransferSync.get(transferDTO.getTransferIndex()));
      } else {
        transferDTOList.remove(transferDTO);
      }
    }

  }

  private void mergeTransfer(TransferDTO transferDTO, TransferSynchronizeDTO transferSynchronizeDTO) {
    transferDTO.setAmountCents(transferSynchronizeDTO.getAmountCents());
    transferDTO.setRemittanceInformation(transferSynchronizeDTO.getRemittanceInformation());

    Set<String> modifiedFields = new HashSet<>();
    checkImmutableField("orgFiscalCode", transferDTO.getOrgFiscalCode(), transferSynchronizeDTO.getOrgFiscalCode(), modifiedFields);
    checkImmutableField("orgName", transferDTO.getOrgName(), transferSynchronizeDTO.getOrgName(), modifiedFields);
    checkImmutableField("iban", transferDTO.getIban(), transferSynchronizeDTO.getIban(), modifiedFields);
    checkImmutableField("category", transferDTO.getCategory(), transferSynchronizeDTO.getCategory(), modifiedFields);

    if (!modifiedFields.isEmpty()) {
      throw new ConflictErrorException("These fields for transfer are not mutable: " + modifiedFields);
    }
  }


  private TransferSynchronizeDTO findTransferByIndex(List<TransferSynchronizeDTO> transfers, Integer transferIndex) {
    return transfers.stream()
      .filter(transferSynchronizeDTO -> transferSynchronizeDTO.getTransferIndex().equals(transferIndex))
      .findFirst()
      .orElse(null);
  }
}
