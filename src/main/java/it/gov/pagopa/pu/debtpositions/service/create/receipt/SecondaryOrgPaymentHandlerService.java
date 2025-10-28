package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptTransferDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecondaryOrgPaymentHandlerService {
  void handle(DebtPosition primaryOrgDp, List<ReceiptTransferDTO> receiptTransfers, String accessToken) {
    // TODO
  }
}
