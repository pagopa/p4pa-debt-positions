package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.ReceiptBasedTechnicalDpHandlerService;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PrimaryOrgPaymentHandlerService {

  private final PrimaryOrgInstallmentRetrieverService installmentRetrieverService;
  private final PrimaryOrgInstallmentPaymentHandlerService installmentPaymentHandlerService;
  private final ReceiptBasedTechnicalDpHandlerService technicalDpCreationService;

  public PrimaryOrgPaymentHandlerService(PrimaryOrgInstallmentRetrieverService installmentRetrieverService, PrimaryOrgInstallmentPaymentHandlerService installmentPaymentHandlerService, ReceiptBasedTechnicalDpHandlerService technicalDpCreationService) {
    this.installmentRetrieverService = installmentRetrieverService;
    this.installmentPaymentHandlerService = installmentPaymentHandlerService;
    this.technicalDpCreationService = technicalDpCreationService;
  }

  public Optional<DebtPosition> handlePayment(Organization primaryOrg, ReceiptWithAdditionalNodeDataDTO receiptDTO, Broker broker, String accessToken) {
    if(primaryOrg==null){
      log.info("Received a Receipt (paymentReceiptId {}) on a not handled primary organization {}",
        receiptDTO.getPaymentReceiptId(), receiptDTO.getOrgFiscalCode());
      return Optional.empty();
    }

    return Optional.of(handlePuPrimaryOrgPayment(receiptDTO, primaryOrg, broker, accessToken));
  }

  private DebtPosition handlePuPrimaryOrgPayment(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization primaryOrg, Broker broker, String accessToken) {
    Optional<InstallmentNoPII> installment = installmentRetrieverService.retrieve(primaryOrg, receiptDTO.getNoticeNumber(), receiptDTO.getIud());
    if(installment.isPresent()){
      return installmentPaymentHandlerService.handlePayment(installment.get(), receiptDTO, primaryOrg, accessToken);
    } else {
      return technicalDpCreationService.createAndPublishTechDp(primaryOrg, receiptDTO);
    }
  }
}
