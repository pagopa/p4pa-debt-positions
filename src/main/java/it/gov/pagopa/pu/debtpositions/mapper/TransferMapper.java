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

}
