package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CreatePaidTechnicalDebtPositionsServiceTest {

  @Mock
  private OrganizationService organizationServiceMock;

  @InjectMocks
  private CreatePaidTechnicalDebtPositionsService createPaidTechnicalDebtPositionsService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private static final String ACCESS_TOKEN = "ACCESS_TOKEN";

  @Test
  void givenIncludePrimaryOrgWhenCreatePaidTechnicalDebtPositionsFromReceiptThenOk() {
    handleCreatePaidTechnicalDebtPositionsFromReceipt(true);
  }

  @Test
  void givenDontIncludePrimaryOrgWhenCreatePaidTechnicalDebtPositionsFromReceiptThenOk() {
    handleCreatePaidTechnicalDebtPositionsFromReceipt(false);
  }

  private void handleCreatePaidTechnicalDebtPositionsFromReceipt(boolean includePrimaryOrg) {
    // given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    List<Organization> orgList = receiptDTO.getTransfers().stream().map(transfer -> {
      Organization org = podamFactory.manufacturePojo(Organization.class);
      transfer.setFiscalCodePA(org.getOrgFiscalCode());
      Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(org.getOrgFiscalCode(), ACCESS_TOKEN)).thenReturn(Optional.of(org));
      return org;
    }).toList();

    //when
    createPaidTechnicalDebtPositionsService.createPaidTechnicalDebtPositionsFromReceipt(receiptDTO, includePrimaryOrg, ACCESS_TOKEN);

    //verify
    orgList.forEach(org ->
      Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(org.getOrgFiscalCode(), ACCESS_TOKEN));
  }

}
