package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StandardPaymentUpdateService {

  private final OrdinaryInstallmentPaymentHandlerService installmentPaymentHandlerService;
  private final OrdinaryPaidDPHierarchyUpdateService hierarchyUpdateService;
  private final DebtPositionService debtPositionService;

  public StandardPaymentUpdateService(OrdinaryInstallmentPaymentHandlerService installmentPaymentHandlerService,
                                      OrdinaryPaidDPHierarchyUpdateService hierarchyUpdateService,
                                      DebtPositionService debtPositionService) {
    this.installmentPaymentHandlerService = installmentPaymentHandlerService;
    this.hierarchyUpdateService = hierarchyUpdateService;
    this.debtPositionService = debtPositionService;
  }

  public void performStandardUpdate(DebtPosition dp, InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    installmentPaymentHandlerService.updateInstallment(installment, receiptDTO, accessToken);
    hierarchyUpdateService.updateHierarchy(dp, installment);
    debtPositionService.saveDebtPosition(dp);
  }
}
