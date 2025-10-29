package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class OrdinaryPaidDPHierarchyUpdateService {

  private final DebtPositionProcessorService debtPositionProcessorService;
  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  public OrdinaryPaidDPHierarchyUpdateService(DebtPositionProcessorService debtPositionProcessorService, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService) {
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
  }

  public void updateHierarchy(DebtPosition dp, InstallmentNoPII installment) {
    dp.getPaymentOptions().forEach(po -> {
      if (po.getPaymentOptionId().equals(installment.getPaymentOptionId())) {
        replacePaidInstallmentIntoPO(installment, po);
      } else {
        invalidateOtherPOs(po);
      }
    });
    debtPositionProcessorService.updateAmounts(dp);
    debtPositionHierarchyStatusAlignerService.alignHierarchyStatus(dp);
  }

  private static void replacePaidInstallmentIntoPO(InstallmentNoPII installment, PaymentOption po) {
    po.setInstallments(Stream.concat(
        po.getInstallments().stream().filter(i -> !i.getInstallmentId().equals(installment.getInstallmentId())),
        Stream.of(installment)
      )
      .collect(Collectors.toCollection(TreeSet::new)));
  }

  private void invalidateOtherPOs(PaymentOption paymentOption) {
    paymentOption.getInstallments().forEach(i -> {
      if (InstallmentUtils.isInvalidable(i)) {
        InstallmentUtils.setStatus(i, InstallmentStatus.INVALID);
      }
    });
  }
}
