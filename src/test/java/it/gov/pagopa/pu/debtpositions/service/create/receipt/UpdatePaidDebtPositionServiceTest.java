package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UpdatePaidDebtPositionServiceTest {

  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private DebtPositionSyncService debtPositionSyncServiceMock;
  @Mock
  private PrimaryOrgInstallmentPaidVerifierService primaryOrgInstallmentPaidVerifierServiceMock;
  @Mock
  private InstallmentUpdateService installmentUpdateServiceMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerServiceMock;

  @InjectMocks
  private UpdatePaidDebtPositionService updatePaidDebtPositionService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private String accessToken;
  private ReceiptWithAdditionalNodeDataDTO receipt;

  @BeforeEach
  void setup() {
    accessToken = "ACCESS_TOKEN";
    receipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
  }

  @Test
  void givenFoundOrgAndPrimaryOrgInstallmentAndWorkflowOkWhenHandleReceiptReceivedThenOk() {
    givenFoundOrgAndPrimaryOrgInstallmentWhenHandleReceiptReceivedThenOk(true);
  }

  @Test
  void givenFoundOrgAndPrimaryOrgInstallmentAndWorkflowKoWhenHandleReceiptReceivedThenOk() {
    givenFoundOrgAndPrimaryOrgInstallmentWhenHandleReceiptReceivedThenOk(false);
  }

  private void givenFoundOrgAndPrimaryOrgInstallmentWhenHandleReceiptReceivedThenOk(boolean workflowInvokeOk) {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());

    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    DebtPositionDTO debtPositionDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    WorkflowCreatedDTO workflowCreatedDTO = workflowInvokeOk ? podamFactory.manufacturePojo(WorkflowCreatedDTO.class) : null;

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(primaryOrgInstallmentPaidVerifierServiceMock.findAndValidatePrimaryOrgInstallment(organization, receipt.getNoticeNumber())).thenReturn(Pair.of(Optional.of(installment), true));
    Mockito.when(installmentUpdateServiceMock.updateInstallmentStatusOfDebtPosition(installment, receipt)).thenReturn(debtPosition);
    Mockito.when(debtPositionHierarchyStatusAlignerServiceMock.alignHierarchyStatus(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, false, PaymentEventType.RT_RECEIVED, accessToken)).thenReturn(workflowCreatedDTO);

    //when
    boolean response = updatePaidDebtPositionService.handleReceiptReceived(receipt, accessToken);

    //verify
    Assertions.assertTrue(response);

    Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), accessToken);
    Mockito.verify(primaryOrgInstallmentPaidVerifierServiceMock, Mockito.times(1)).findAndValidatePrimaryOrgInstallment(organization, receipt.getNoticeNumber());
    Mockito.verify(installmentUpdateServiceMock, Mockito.times(1)).updateInstallmentStatusOfDebtPosition(installment, receipt);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPosition);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionSyncServiceMock, Mockito.times(1)).syncDebtPosition(debtPositionDTO, false, PaymentEventType.RT_RECEIVED, accessToken);
  }

  @Test
  void givenFoundOrgAndNotFoundPrimaryOrgInstallmentWhenHandleReceiptReceivedThenOk() {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(primaryOrgInstallmentPaidVerifierServiceMock.findAndValidatePrimaryOrgInstallment(organization, receipt.getNoticeNumber())).thenReturn(Pair.of(Optional.empty(), false));

    //when
    boolean response = updatePaidDebtPositionService.handleReceiptReceived(receipt, accessToken);

    //verify
    Assertions.assertFalse(response);

    Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), accessToken);
    Mockito.verify(primaryOrgInstallmentPaidVerifierServiceMock, Mockito.times(1)).findAndValidatePrimaryOrgInstallment(organization, receipt.getNoticeNumber());
    Mockito.verifyNoInteractions(installmentUpdateServiceMock, debtPositionHierarchyStatusAlignerServiceMock, debtPositionServiceMock, debtPositionSyncServiceMock);
  }

  @Test
  void givenNotFoundOrgWhenCreateReceiptThenOk() {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.empty());

    //when
    boolean response = updatePaidDebtPositionService.handleReceiptReceived(receipt, accessToken);

    //verify
    Assertions.assertFalse(response);

    Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), accessToken);
    Mockito.verifyNoInteractions(primaryOrgInstallmentPaidVerifierServiceMock, installmentUpdateServiceMock, debtPositionHierarchyStatusAlignerServiceMock, debtPositionServiceMock, debtPositionSyncServiceMock);
  }

}
