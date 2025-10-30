package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptTransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.ReceiptBasedTechnicalDpHandlerService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SecondaryOrgPaymentHandlerService {

  private final OrganizationService organizationService;
  private final DebtPositionRepository debtPositionRepository;
  private final ReceiptBasedTechnicalDpHandlerService receiptBasedTechnicalDpHandlerService;

  public SecondaryOrgPaymentHandlerService(OrganizationService organizationService, DebtPositionRepository debtPositionRepository, ReceiptBasedTechnicalDpHandlerService receiptBasedTechnicalDpHandlerService) {
    this.organizationService = organizationService;
    this.debtPositionRepository = debtPositionRepository;
    this.receiptBasedTechnicalDpHandlerService = receiptBasedTechnicalDpHandlerService;
  }

  void handle(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    receiptDTO.getTransfers().stream()
      .map(ReceiptTransferDTO::getFiscalCodePA)
      .filter(secondaryOrgFiscalCode -> !secondaryOrgFiscalCode.equals(receiptDTO.getOrgFiscalCode()))
      .distinct()
      .forEach(secondaryOrgFiscalCode -> handleSecondaryOrgTransfers(secondaryOrgFiscalCode, receiptDTO, accessToken));
  }

  private void handleSecondaryOrgTransfers(String secondaryOrgFiscalCode, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    Organization secondaryOrg = organizationService.getOrganizationByFiscalCode(secondaryOrgFiscalCode, accessToken).orElse(null);
    if (secondaryOrg == null) {
      log.info("Received a Receipt (paymentReceiptId {}) with a not handled Secondary org {}, skipping its SECONDARY_ORG tech DP creation",
        receiptDTO.getPaymentReceiptId(), secondaryOrgFiscalCode);
    } else {
      List<DebtPosition> secondaryDp = debtPositionRepository.findEntityGraphByOrganizationIdAndReceiptId(secondaryOrg.getOrganizationId(), receiptDTO.getReceiptId(), List.of(DebtPositionOrigin.SECONDARY_ORG));
      if (!secondaryDp.isEmpty()) {
        updateExistingSecondaryDp(secondaryOrg, secondaryDp, receiptDTO);
      } else {
        createNewSecondaryDp(secondaryOrg, receiptDTO);
      }
    }
  }

  private void updateExistingSecondaryDp(Organization secondaryOrg, List<DebtPosition> secondaryOrgDps, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    log.info("Found {} SECONDARY_ORG DP related to receipt [id={}, nav={}] on organization [id={}, fiscalCode={}]",
      secondaryOrgDps.size(),
      receiptDTO.getReceiptId(), receiptDTO.getNoticeNumber(),
      secondaryOrg.getOrganizationId(), secondaryOrg.getOrgFiscalCode());
    receiptBasedTechnicalDpHandlerService.updateAndPublishTechDp(secondaryOrg, secondaryOrgDps.getFirst(), receiptDTO);
  }

  private void createNewSecondaryDp(Organization secondaryOrg, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    log.info("Creating SECONDARY_ORG DP related to receipt [id={}, nav={}] on organization [id={}, fiscalCode={}]",
      receiptDTO.getReceiptId(), receiptDTO.getNoticeNumber(),
      secondaryOrg.getOrganizationId(), secondaryOrg.getOrgFiscalCode());
    receiptBasedTechnicalDpHandlerService.createAndPublishTechDp(secondaryOrg, receiptDTO);
  }
}
