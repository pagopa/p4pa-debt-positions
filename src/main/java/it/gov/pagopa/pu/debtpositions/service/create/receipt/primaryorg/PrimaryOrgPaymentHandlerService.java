package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PrimaryOrgPaymentHandlerService {

  private final OrganizationService organizationService;
  private final PrimaryOrgInstallmentRetrieverService installmentRetrieverService;
  private final PrimaryOrgInstallmentPaymentHandlerService installmentPaymentHandlerService;
  private final PrimaryOrgTechnicalDpCreationService technicalDpCreationService;

  public PrimaryOrgPaymentHandlerService(OrganizationService organizationService, PrimaryOrgInstallmentRetrieverService installmentRetrieverService, PrimaryOrgInstallmentPaymentHandlerService installmentPaymentHandlerService, PrimaryOrgTechnicalDpCreationService technicalDpCreationService) {
    this.organizationService = organizationService;
    this.installmentRetrieverService = installmentRetrieverService;
    this.installmentPaymentHandlerService = installmentPaymentHandlerService;
    this.technicalDpCreationService = technicalDpCreationService;
  }

  public Optional<DebtPosition> handlePayment(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    Organization primaryOrg = organizationService.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken).orElse(null);
    if(primaryOrg==null){
      log.info("Received a Receipt (paymentReceiptId {}) on a not handled primary organization {}",
        receiptDTO.getPaymentReceiptId(), receiptDTO.getOrgFiscalCode());
      return Optional.empty();
    }

    return Optional.of(handlePuPrimaryOrgPayment(receiptDTO, primaryOrg));
  }

  private DebtPosition handlePuPrimaryOrgPayment(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization primaryOrg) {
    InstallmentNoPII installment = installmentRetrieverService.retrieve(primaryOrg, receiptDTO.getNoticeNumber(), receiptDTO.getIud());
    if(installment!=null){
      return installmentPaymentHandlerService.handlePayment(installment, receiptDTO);
    } else {
      return technicalDpCreationService.create(receiptDTO);
    }
  }
}
