package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptTransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CreatePaidTechnicalDebtPositionsService {

  private final OrganizationService organizationService;
  private final ManagePaidDebtPositionService managePaidDebtPositionService;

  public CreatePaidTechnicalDebtPositionsService(OrganizationService organizationService,
                                                 ManagePaidDebtPositionService managePaidDebtPositionService) {
    this.organizationService = organizationService;
    this.managePaidDebtPositionService = managePaidDebtPositionService;
  }

  void createPaidTechnicalDebtPositionsFromReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO, boolean includePrimaryOrg, String accessToken) {
    //for every organization handled by PU and mentioned in the receipt
    receiptDTO.getTransfers().stream()
      //get the fiscal code of the organization
      .map(ReceiptTransferDTO::getFiscalCodePA)
      .distinct()
      //exclude primary org in case it has a valid debt position associated to it
      .filter(fiscalCode -> includePrimaryOrg || !fiscalCode.equals(receiptDTO.getOrgFiscalCode()))
      //check if the organization is managed by PU
      .flatMap(fiscalCode -> organizationService.getOrganizationByFiscalCode(fiscalCode, accessToken).stream())
      //create a "technical" debt position, in status PAID
      .forEach(organization ->
        managePaidDebtPositionService.persistTechnicalDebtPositionFromReceiptAndNotifyEvent(receiptDTO, organization) );
  }
}
