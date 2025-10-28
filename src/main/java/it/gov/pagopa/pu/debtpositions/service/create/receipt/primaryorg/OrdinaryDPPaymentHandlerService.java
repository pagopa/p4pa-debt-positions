package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.stereotype.Service;

@Service
public class OrdinaryDPPaymentHandlerService {
  public void handlePayment(DebtPosition dp, Long installment, ReceiptDTO receiptDTO){
    // TODO
  }
}
