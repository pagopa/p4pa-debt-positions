package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Stamp;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import org.springframework.stereotype.Service;

@Service
public class TransferMapper {

  public Transfer mapToModel(TransferDTO dto) {
    Transfer transfer = new Transfer();
    transfer.setTransferId(dto.getTransferId());
    transfer.setInstallmentId(dto.getInstallmentId());
    transfer.setOrgFiscalCode(dto.getOrgFiscalCode());
    transfer.setOrgName(dto.getOrgName());
    transfer.setAmountCents(dto.getAmountCents());
    transfer.setRemittanceInformation(dto.getRemittanceInformation());
    transfer.setStamp(new Stamp(dto.getStampType(), dto.getStampHashDocument(), dto.getStampProvincialResidence()));
    transfer.setIban(dto.getIban());
    transfer.setPostalIban(dto.getPostalIban());
    transfer.setCategory(dto.getCategory());
    transfer.setTransferIndex(dto.getTransferIndex());
    return transfer;
  }

  public TransferDTO mapToDto(Transfer transfer) {
    return TransferDTO.builder()
      .transferId(transfer.getTransferId())
      .installmentId(transfer.getInstallmentId())
      .orgFiscalCode(transfer.getOrgFiscalCode())
      .orgName(transfer.getOrgName())
      .amountCents(transfer.getAmountCents())
      .remittanceInformation(transfer.getRemittanceInformation())
      .stampType(transfer.getStamp().getStampType())
      .stampHashDocument(transfer.getStamp().getStampHashDocument())
      .stampProvincialResidence(transfer.getStamp().getStampProvincialResidence())
      .iban(transfer.getIban())
      .postalIban(transfer.getPostalIban())
      .category(transfer.getCategory())
      .transferIndex(transfer.getTransferIndex())
      .build();
  }


}
