package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.OrdinaryDPPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.ReceiptBasedTechnicalDpHandlerService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.stereotype.Service;

@Service
public class PrimaryOrgInstallmentPaymentHandlerService {

  private final DebtPositionRepository debtPositionRepository;
  private final OrdinaryDPPaymentHandlerService ordinaryDPPaymentHandlerService;
  private final ReceiptBasedTechnicalDpHandlerService technicalDpHandlerService;

  public PrimaryOrgInstallmentPaymentHandlerService(DebtPositionRepository debtPositionRepository, OrdinaryDPPaymentHandlerService ordinaryDPPaymentHandlerService, ReceiptBasedTechnicalDpHandlerService technicalDpHandlerService) {
    this.debtPositionRepository = debtPositionRepository;
    this.ordinaryDPPaymentHandlerService = ordinaryDPPaymentHandlerService;
    this.technicalDpHandlerService = technicalDpHandlerService;
  }

  public DebtPosition handlePayment(InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization, String accessToken){
    DebtPosition dp = debtPositionRepository.findEntityGraphByInstallmentId(installment.getInstallmentId());
    if (dp == null) {
      throw new NotFoundException("debt position not found for installment " + installment.getInstallmentId());
    }

    if(InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS.contains(dp.getDebtPositionOrigin())){
      ordinaryDPPaymentHandlerService.handlePayment(dp, installment, receiptDTO, accessToken);
      return dp;
    } else {
      return technicalDpHandlerService.updateAndPublishTechDp(organization, dp, installment, receiptDTO, accessToken);
    }
  }
}
