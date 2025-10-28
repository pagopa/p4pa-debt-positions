package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.springframework.stereotype.Service;

@Service
public class PrimaryOrgInstallmentPaymentHandlerService {

  public DebtPosition handlePayment(InstallmentNoPII installment, ReceiptDTO receiptDTO){
    return null; //TODO
  }
}
