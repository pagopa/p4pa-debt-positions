package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.stereotype.Service;

@Service
public class PrimaryOrgPaymentHandlerService {
  DebtPosition handle(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    return null; // TODO
  }
}
