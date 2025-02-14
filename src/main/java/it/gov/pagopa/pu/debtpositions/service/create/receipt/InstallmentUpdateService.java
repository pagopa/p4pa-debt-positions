package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.organization.dto.generated.Broker;

public interface InstallmentUpdateService {
  DebtPosition updateInstallmentStatusOfDebtPosition(InstallmentNoPII installment, Broker broker, ReceiptDTO receiptDTO);
}
