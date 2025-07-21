package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ManagePaidDebtPositionServiceTest {

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
  @Mock
  private ReceiptWithAdditionalInfoMapper receiptWithAdditionalInfoMapperMock;
  @Mock
  private PaymentsProducerService paymentsProducerServiceMock;
  @Mock
  private DebtPositionProcessorService debtPositionProcessorServiceMock;

  @InjectMocks
  private ManagePaidDebtPositionService managePaidDebtPositionService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private String accessToken;
  private ReceiptWithAdditionalNodeDataDTO receipt;

  @BeforeEach
  void setup() {
    accessToken = "ACCESS_TOKEN";
    receipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      organizationServiceMock,
      debtPositionSyncServiceMock,
      primaryOrgInstallmentPaidVerifierServiceMock,
      installmentUpdateServiceMock,
      debtPositionServiceMock,
      debtPositionHierarchyStatusAlignerServiceMock,
      receiptWithAdditionalInfoMapperMock,
      paymentsProducerServiceMock,
      debtPositionProcessorServiceMock
    );
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenFoundOrgAndPrimaryOrgInstallmentWhenHandleReceiptReceivedPrimaryOrgThenOk(boolean workflowInvokeOk) {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());

    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    DebtPositionDTO debtPositionDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    WorkflowCreatedDTO workflowCreatedDTO = workflowInvokeOk ? podamFactory.manufacturePojo(WorkflowCreatedDTO.class) : null;

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(primaryOrgInstallmentPaidVerifierServiceMock.findAndValidatePrimaryOrgInstallment(organization, receipt.getNoticeNumber(), receipt.getIud())).thenReturn(Pair.of(Optional.of(installment), true));
    Mockito.when(installmentUpdateServiceMock.updateInstallmentStatusOfDebtPosition(installment, receipt, accessToken)).thenReturn(debtPosition);
    Mockito.when(debtPositionServiceMock.mapDebtPosition(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, new WfExecutionParameters(), PaymentEventType.RT_RECEIVED, "receiptId:"+receipt.getReceiptId(), accessToken)).thenReturn(workflowCreatedDTO);

    //when
    boolean response = managePaidDebtPositionService.handleReceiptReceivedPrimaryOrg(receipt, accessToken);

    //verify
    Assertions.assertTrue(response);

    Mockito.verify(debtPositionProcessorServiceMock).updateAmounts(debtPosition);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPosition);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPosition);
  }

  @Test
  void givenFoundOrgAndNotFoundPrimaryOrgInstallmentWhenHandleReceiptReceivedPrimaryOrgThenOk() {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(primaryOrgInstallmentPaidVerifierServiceMock.findAndValidatePrimaryOrgInstallment(organization, receipt.getNoticeNumber(), receipt.getIud())).thenReturn(Pair.of(Optional.empty(), false));

    //when
    boolean response = managePaidDebtPositionService.handleReceiptReceivedPrimaryOrg(receipt, accessToken);

    //verify
    Assertions.assertFalse(response);
  }

  @Test
  void givenNotFoundOrgWhenCreateReceiptThenOk() {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.empty());

    //when
    boolean response = managePaidDebtPositionService.handleReceiptReceivedPrimaryOrg(receipt, accessToken);

    //verify
    Assertions.assertFalse(response);
  }

  @Test
  void givenReceiptWhenPersistTechnicalDebtPositionFromReceiptAndNotifyEventThenOk(){
    //given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO debtPositionDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);

    Mockito.when(receiptWithAdditionalInfoMapperMock.mapToDebtPosition(receiptDTO, organization)).thenReturn(debtPositionDTO);
    Mockito.doNothing().when(paymentsProducerServiceMock).notifyPaymentsEvent(debtPositionDTO, PaymentEventType.RT_RECEIVED, "receiptId:"+receiptDTO.getReceiptId());

    //when
    managePaidDebtPositionService.persistTechnicalDebtPositionFromReceiptAndNotifyEvent(receiptDTO, organization);

    //verify
    Mockito.verify(debtPositionServiceMock, Mockito.times(1)).saveDebtPosition(debtPositionDTO);
  }

}
