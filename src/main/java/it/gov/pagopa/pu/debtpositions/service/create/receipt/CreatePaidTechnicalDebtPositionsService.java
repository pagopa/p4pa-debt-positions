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

  public CreatePaidTechnicalDebtPositionsService(OrganizationService organizationService) {
    this.organizationService = organizationService;
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
        //TODO task P4ADEV-2027
        log.info("TODO P4ADEV-2027 create technical debt position for org[{}/{}] nav[{}]", organization.getOrganizationId(),
          organization.getOrgFiscalCode(), receiptDTO.getNoticeNumber())
      );
  }
}
