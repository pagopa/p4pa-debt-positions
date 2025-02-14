package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CreateReceiptServiceImplTest {

  @Mock
  private ReceiptPIIRepository receiptPIIRepositoryMock;
  @Mock
  private ReceiptNoPIIRepository receiptNoPIIRepositoryMock;
  @Mock
  private ReceiptMapper receiptMapperMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private BrokerService brokerServiceMock;
  @Mock
  private DebtPositionSyncService debtPositionSyncServiceMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;
  @Mock
  private PrimaryOrgInstallmentService primaryOrgInstallmentServiceMock;
  @Mock
  private InstallmentUpdateService installmentUpdateServiceMock;
  @Mock
  private CreatePaidTechnicalDebtPositionsService createPaidTechnicalDebtPositionsServiceMock;

  @InjectMocks
  private CreateReceiptServiceImpl receiptService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private String accessToken;
  private ReceiptWithAdditionalNodeDataDTO receipt;
  private Receipt receiptModel;
  private long receiptId;

  @BeforeEach
  void setup(TestInfo info) {
    accessToken = "ACCESS_TOKEN";

    receipt = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptModel = podamFactory.manufacturePojo(Receipt.class);
    receiptId = receiptModel.getReceiptId();
    receiptModel.setReceiptId(null);

    if (!info.getTags().contains("alreadyHandled")) {
      Mockito.when(receiptPIIRepositoryMock.save(receiptModel)).thenReturn(receiptId);
      Mockito.when(receiptMapperMock.mapToModel(receipt)).thenReturn(receiptModel);
      Mockito.when(receiptPIIRepositoryMock.save(receiptModel)).thenReturn(receiptId);
      Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receipt.getPaymentReceiptId())).thenReturn(null);
    }
  }

  @Test
  @Tag("alreadyHandled")
  void givenReceiptAlreadyHandledWhenCreateReceiptThenOk() {
    //given
    receipt.setPaymentReceiptId("NEW_PAYMENT_RECEIPT_ID");
    ReceiptNoPII receiptNoPII = podamFactory.manufacturePojo(ReceiptNoPII.class);
    receiptNoPII.setPaymentReceiptId(receipt.getPaymentReceiptId());
    Mockito.when(receiptNoPIIRepositoryMock.getByPaymentReceiptId(receipt.getPaymentReceiptId())).thenReturn(receiptNoPII);

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptNoPII.getReceiptId(), response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verifyNoInteractions(receiptPIIRepositoryMock, receiptMapperMock, organizationServiceMock, brokerServiceMock,
      debtPositionMapperMock, debtPositionSyncServiceMock, primaryOrgInstallmentServiceMock, installmentUpdateServiceMock);
  }

  @Test
  void givenFoundOrgAndPrimaryOrgInstallmentAndWorkflowOkWhenCreateReceiptThenOk() {
    givenFoundOrgAndPrimaryOrgInstallmentWhenCreateReceiptThenOk(true);
  }

  @Test
  void givenFoundOrgAndPrimaryOrgInstallmentAndWorkflowKoWhenCreateReceiptThenOk() {
    givenFoundOrgAndPrimaryOrgInstallmentWhenCreateReceiptThenOk(false);
  }

  private void givenFoundOrgAndPrimaryOrgInstallmentWhenCreateReceiptThenOk(boolean workflowInvokeOk) {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());

    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    DebtPositionDTO debtPositionDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    WorkflowCreatedDTO workflowCreatedDTO = workflowInvokeOk ? podamFactory.manufacturePojo(WorkflowCreatedDTO.class) : null;

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(primaryOrgInstallmentServiceMock.findPrimaryOrgInstallment(organization, receipt.getNoticeNumber())).thenReturn(Optional.of(installment));
    Mockito.when(brokerServiceMock.findById(broker.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(installmentUpdateServiceMock.updateInstallmentStatusOfDebtPosition(installment, broker, receipt)).thenReturn(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, false, PaymentEventType.RT_RECEIVED, accessToken)).thenReturn(workflowCreatedDTO);
    Mockito.doNothing().when(createPaidTechnicalDebtPositionsServiceMock).createPaidTechnicalDebtPositionsFromReceipt(receipt, false, accessToken);

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptId, response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verify(receiptPIIRepositoryMock, Mockito.times(1)).save(receiptModel);
    Mockito.verify(receiptMapperMock, Mockito.times(1)).mapToModel(receipt);
    Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), accessToken);
    Mockito.verify(primaryOrgInstallmentServiceMock, Mockito.times(1)).findPrimaryOrgInstallment(organization, receipt.getNoticeNumber());
    Mockito.verify(brokerServiceMock, Mockito.times(1)).findById(broker.getBrokerId(), accessToken);
    Mockito.verify(installmentUpdateServiceMock, Mockito.times(1)).updateInstallmentStatusOfDebtPosition(installment, broker, receipt);
    Mockito.verify(debtPositionMapperMock, Mockito.times(1)).mapToDto(debtPosition);
    Mockito.verify(debtPositionSyncServiceMock, Mockito.times(1)).syncDebtPosition(debtPositionDTO, false, PaymentEventType.RT_RECEIVED, accessToken);
    Mockito.verify(createPaidTechnicalDebtPositionsServiceMock, Mockito.times(1)).createPaidTechnicalDebtPositionsFromReceipt(receipt, false, accessToken);
  }

  @Test
  void givenFoundOrgAndNotFoundPrimaryOrgInstallmentWhenCreateReceiptThenOk() {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(primaryOrgInstallmentServiceMock.findPrimaryOrgInstallment(organization, receipt.getNoticeNumber())).thenReturn(Optional.empty());
    Mockito.doNothing().when(createPaidTechnicalDebtPositionsServiceMock).createPaidTechnicalDebtPositionsFromReceipt(receipt, true, accessToken);

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptId, response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verify(receiptPIIRepositoryMock, Mockito.times(1)).save(receiptModel);
    Mockito.verify(receiptMapperMock, Mockito.times(1)).mapToModel(receipt);
    Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), accessToken);
    Mockito.verify(primaryOrgInstallmentServiceMock, Mockito.times(1)).findPrimaryOrgInstallment(organization, receipt.getNoticeNumber());
    Mockito.verify(createPaidTechnicalDebtPositionsServiceMock, Mockito.times(1)).createPaidTechnicalDebtPositionsFromReceipt(receipt, true, accessToken);
    Mockito.verifyNoInteractions(brokerServiceMock,installmentUpdateServiceMock, debtPositionMapperMock, debtPositionSyncServiceMock);
  }

  @Test
  void givenNotFoundOrgWhenCreateReceiptThenOk() {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.empty());
    Mockito.doNothing().when(createPaidTechnicalDebtPositionsServiceMock).createPaidTechnicalDebtPositionsFromReceipt(receipt, true, accessToken);

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptId, response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verify(receiptPIIRepositoryMock, Mockito.times(1)).save(receiptModel);
    Mockito.verify(receiptMapperMock, Mockito.times(1)).mapToModel(receipt);
    Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), accessToken);
    Mockito.verify(createPaidTechnicalDebtPositionsServiceMock, Mockito.times(1)).createPaidTechnicalDebtPositionsFromReceipt(receipt, true, accessToken);
    Mockito.verifyNoInteractions(primaryOrgInstallmentServiceMock, brokerServiceMock,installmentUpdateServiceMock, debtPositionMapperMock, debtPositionSyncServiceMock);
  }

}
