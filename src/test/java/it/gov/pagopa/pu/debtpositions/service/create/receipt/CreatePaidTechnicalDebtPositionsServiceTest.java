package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

  @Mock
  private ManagePaidDebtPositionService managePaidDebtPositionServiceMock;

  @InjectMocks
  private CreatePaidTechnicalDebtPositionsService createPaidTechnicalDebtPositionsService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private static final String ACCESS_TOKEN = "ACCESS_TOKEN";

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void whenCreatePaidTechnicalDebtPositionsFromReceiptThenOk(boolean includePrimaryOrg) {
    // given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setOrgFiscalCode(receiptDTO.getTransfers().getFirst().getFiscalCodePA());
    List<Organization> orgList = receiptDTO.getTransfers().stream().skip(includePrimaryOrg ? 0 : 1).map(transfer -> {
      Organization org = podamFactory.manufacturePojo(Organization.class);
      transfer.setFiscalCodePA(org.getOrgFiscalCode());
      Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(org.getOrgFiscalCode(), ACCESS_TOKEN)).thenReturn(Optional.of(org));
      Mockito.doNothing().when(managePaidDebtPositionServiceMock).persistTechnicalDebtPositionFromReceipt(receiptDTO, org);
      return org;
    }).toList();

    //when
    createPaidTechnicalDebtPositionsService.createPaidTechnicalDebtPositionsFromReceipt(receiptDTO, includePrimaryOrg, ACCESS_TOKEN);

    //verify
    orgList.forEach(org -> {
      Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(org.getOrgFiscalCode(), ACCESS_TOKEN);
      Mockito.verify(managePaidDebtPositionServiceMock, Mockito.times(1)).persistTechnicalDebtPositionFromReceipt(receiptDTO, org);
    });
  }

}
