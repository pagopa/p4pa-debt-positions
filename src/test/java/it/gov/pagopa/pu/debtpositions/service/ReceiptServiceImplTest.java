package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidInstallmentStatusException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceImplTest {

  @Mock
  private ReceiptPIIRepository receiptPIIRepositoryMock;
  @Mock
  private ReceiptNoPIIRepository receiptNoPIIRepositoryMock;
  @Mock
  private ReceiptMapper receiptMapperMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private BrokerService brokerServiceMock;
  @Mock
  private DebtPositionSyncService debtPositionSyncServiceMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  @InjectMocks
  private ReceiptServiceImpl receiptService;

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

      if (!info.getTags().contains("exception")) {
        receipt.getTransfers().stream()
          .filter(transfer -> !receipt.getOrgFiscalCode().equals(transfer.getFiscalCodePA()))
          .forEach(transfer -> {
            Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(transfer.getFiscalCodePA(), accessToken))
              .thenReturn(Optional.of(podamFactory.manufacturePojo(Organization.class)));
          });
      }
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
      installmentNoPIIRepositoryMock, debtPositionRepositoryMock, debtPositionMapperMock, debtPositionSyncServiceMock);
  }

  @Test
  void givenValidReceiptAndUnpaidInstallmentWhenCreateReceiptThenOk() {
    List<InstallmentNoPII> additionalInstallments = new ArrayList<>();
    //add multiple to sync installments
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.TO_SYNC);
    installment.setSyncStatus(InstallmentSyncStatus.builder().syncStatusFrom(InstallmentStatus.EXPIRED).syncStatusTo(InstallmentStatus.UNPAID).build());
    additionalInstallments.add(installment);
    installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.TO_SYNC);
    installment.setSyncStatus(InstallmentSyncStatus.builder().syncStatusFrom(InstallmentStatus.DRAFT).syncStatusTo(InstallmentStatus.UNPAID).build());
    additionalInstallments.add(installment);

    handleValidReceiptImpl(InstallmentStatus.UNPAID, null, null, additionalInstallments, true);
  }

  @Test
  void givenValidReceiptAndInSyncInstallmentWhenCreateReceiptThenOk() {
    handleValidReceiptImpl(InstallmentStatus.TO_SYNC,
      InstallmentSyncStatus.builder().syncStatusFrom(InstallmentStatus.EXPIRED).syncStatusTo(InstallmentStatus.UNPAID).build(),
      null, null, true);
  }

  @Test
  void givenValidReceiptAndInSyncInstallmentAndInvokeWorkflowKoWhenCreateReceiptThenOk() {
    handleValidReceiptImpl(InstallmentStatus.TO_SYNC,
      InstallmentSyncStatus.builder().syncStatusFrom(InstallmentStatus.EXPIRED).syncStatusTo(InstallmentStatus.UNPAID).build(),
      null, null, false);
  }

  @Test
  void givenValidReceiptAndExpiredInstallmentsWhenCreateReceiptThenOk() {
    List<InstallmentNoPII> additionalInstallments = new ArrayList<>();
    //add multiple to sync installments
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.EXPIRED);
    installment.setUpdateDate(LocalDateTime.of(2025, 2, 11, 1, 0));
    installment.setSyncStatus(InstallmentSyncStatus.builder().syncStatusFrom(InstallmentStatus.DRAFT).syncStatusTo(InstallmentStatus.UNPAID).build());
    additionalInstallments.add(installment);

    handleValidReceiptImpl(InstallmentStatus.EXPIRED, null, LocalDateTime.of(2025, 2, 11, 3, 0), additionalInstallments, true);
  }

  private void handleValidReceiptImpl(InstallmentStatus status, InstallmentSyncStatus syncStatus, LocalDateTime updateDate, List<InstallmentNoPII> additionalInstallments, boolean invokeWorkflowOk) {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());

    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    debtPosition.setPaymentOptions(keepFirstElements(debtPosition.getPaymentOptions(), 2));
    debtPosition.getPaymentOptions().getFirst().setInstallments(keepFirstElements(debtPosition.getPaymentOptions().getFirst().getInstallments(), 1));
    //sync id
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.getInstallments().forEach(installment -> {
        installment.setPaymentOptionId(paymentOption.getPaymentOptionId());
      });
      paymentOption.setDebtPositionId(debtPosition.getDebtPositionId());
    });

    InstallmentNoPII installment = debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst();
    installment.setStatus(status);
    installment.setSyncStatus(syncStatus);
    if (updateDate != null)
      installment.setUpdateDate(updateDate);
    DebtPositionDTO debtPositionDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);
    WorkflowCreatedDTO workflowCreatedDTO = invokeWorkflowOk ? podamFactory.manufacturePojo(WorkflowCreatedDTO.class) : null;
    List<InstallmentNoPII> installments = new ArrayList<>();
    installments.add(installment);
    //add a paid installment
    InstallmentNoPII otherInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    otherInstallment.setStatus(InstallmentStatus.PAID);
    installments.add(otherInstallment);
    //add a reported installment
    otherInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    otherInstallment.setStatus(InstallmentStatus.REPORTED);
    installments.add(otherInstallment);
    //add other test-case related installments
    if (additionalInstallments != null)
      installments.addAll(additionalInstallments);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(brokerServiceMock.findById(broker.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndNav(organization.getOrganizationId(), receipt.getNoticeNumber())).thenReturn(installments);
    Mockito.when(debtPositionRepositoryMock.findByInstallmentId(installment.getInstallmentId())).thenReturn(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.invokeWorkFlow(debtPositionDTO, accessToken, false)).thenReturn(workflowCreatedDTO);

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptId, response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verify(receiptPIIRepositoryMock, Mockito.times(1)).save(receiptModel);
    Mockito.verify(receiptMapperMock, Mockito.times(1)).mapToModel(receipt);
    Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), accessToken);
    Mockito.verify(brokerServiceMock, Mockito.times(1)).findById(broker.getBrokerId(), accessToken);
    Mockito.verify(installmentNoPIIRepositoryMock, Mockito.times(1)).getByOrganizationIdAndNav(organization.getOrganizationId(), receipt.getNoticeNumber());
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1)).findByInstallmentId(installment.getInstallmentId());
    Mockito.verify(debtPositionMapperMock, Mockito.times(1)).mapToDto(debtPosition);
    Mockito.verify(debtPositionSyncServiceMock, Mockito.times(1)).invokeWorkFlow(debtPositionDTO, accessToken, false);
  }

  @Test
  void givenValidReceiptAndDraftInstallmentWhenCreateReceiptThenOk() {
    handleNotValidInstallmentReceiptImpl(null);
  }

  @Test
  void givenValidReceiptAndDraftAndToSyncInstallmentWhenCreateReceiptThenOk() {
    List<InstallmentNoPII> additionalInstallments = new ArrayList<>();
    //add multiple to sync installments
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.TO_SYNC);
    installment.setSyncStatus(InstallmentSyncStatus.builder().syncStatusFrom(InstallmentStatus.UNPAID).syncStatusTo(InstallmentStatus.PAID).build());
    additionalInstallments.add(installment);
    handleNotValidInstallmentReceiptImpl(additionalInstallments);
  }

  private void handleNotValidInstallmentReceiptImpl(List<InstallmentNoPII> additionalInstallments) {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());

    List<InstallmentNoPII> installments = new ArrayList<>();
    //add a paid installment
    InstallmentNoPII otherInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    otherInstallment.setStatus(InstallmentStatus.PAID);
    installments.add(otherInstallment);
    //add a draft installment
    otherInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    otherInstallment.setStatus(InstallmentStatus.DRAFT);
    installments.add(otherInstallment);
    //add other test-case related installments
    if (additionalInstallments != null)
      installments.addAll(additionalInstallments);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(brokerServiceMock.findById(broker.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndNav(organization.getOrganizationId(), receipt.getNoticeNumber())).thenReturn(installments);

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptId, response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verify(receiptPIIRepositoryMock, Mockito.times(1)).save(receiptModel);
    Mockito.verify(receiptMapperMock, Mockito.times(1)).mapToModel(receipt);
    Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), accessToken);
    Mockito.verify(brokerServiceMock, Mockito.times(1)).findById(broker.getBrokerId(), accessToken);
    Mockito.verify(installmentNoPIIRepositoryMock, Mockito.times(1)).getByOrganizationIdAndNav(organization.getOrganizationId(), receipt.getNoticeNumber());

    Mockito.verifyNoInteractions(debtPositionRepositoryMock, debtPositionMapperMock, debtPositionSyncServiceMock);
  }

  @Test
  void givenNotFoundOrganizationWhenCreateReceiptThenOk() {
    //give
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.empty());

    //when
    ReceiptDTO response = receiptService.createReceipt(receipt, accessToken);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receiptId, response.getReceiptId());
    TestUtils.reflectionEqualsByName(receipt, response, "receiptId");

    Mockito.verify(receiptPIIRepositoryMock, Mockito.times(1)).save(receiptModel);
    Mockito.verify(receiptMapperMock, Mockito.times(1)).mapToModel(receipt);
  }

  @Test
  @Tag("exception")
  void givenMultipleUnpaidInstallmentsWhenCreateReceiptThenException() {
    handleMultipleIncoherentInstallmentImpl(InstallmentStatus.UNPAID, null);
  }

  @Test
  @Tag("exception")
  void givenMultipleToSyncInstallmentsWhenCreateReceiptThenException() {
    handleMultipleIncoherentInstallmentImpl(InstallmentStatus.TO_SYNC,
      InstallmentSyncStatus.builder().syncStatusFrom(InstallmentStatus.EXPIRED).syncStatusTo(InstallmentStatus.UNPAID).build());
  }

  private void handleMultipleIncoherentInstallmentImpl(InstallmentStatus status, InstallmentSyncStatus syncStatus) {
    //given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(receipt.getOrgFiscalCode());
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    organization.setBrokerId(broker.getBrokerId());

    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    debtPosition.setPaymentOptions(keepFirstElements(debtPosition.getPaymentOptions(), 1));
    debtPosition.getPaymentOptions().getFirst().setInstallments(keepFirstElements(debtPosition.getPaymentOptions().getFirst().getInstallments(), 1));
    InstallmentNoPII installment = debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst();
    installment.setStatus(status);
    installment.setSyncStatus(syncStatus);
    //add a second installment (paid)
    InstallmentNoPII secondInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    secondInstallment.setStatus(InstallmentStatus.PAID);
    //add a third installment (unpaid)
    InstallmentNoPII thirdInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    thirdInstallment.setStatus(status);
    thirdInstallment.setSyncStatus(syncStatus);

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(receipt.getOrgFiscalCode(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(brokerServiceMock.findById(broker.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndNav(organization.getOrganizationId(), receipt.getNoticeNumber()))
      .thenReturn(List.of(installment, secondInstallment, thirdInstallment));

    //when
    InvalidInstallmentStatusException exception = Assertions.assertThrows(InvalidInstallmentStatusException.class, () -> receiptService.createReceipt(receipt, accessToken));

    //verify
    Assertions.assertNotNull(exception);
    Assertions.assertTrue(exception.getMessage().startsWith("Multiple installments with status " + status.getValue()));

    Mockito.verify(receiptPIIRepositoryMock, Mockito.times(1)).save(receiptModel);
    Mockito.verify(receiptMapperMock, Mockito.times(1)).mapToModel(receipt);
    Mockito.verify(organizationServiceMock, Mockito.times(1)).getOrganizationByFiscalCode(organization.getOrgFiscalCode(), accessToken);
    Mockito.verify(brokerServiceMock, Mockito.times(1)).findById(broker.getBrokerId(), accessToken);
    Mockito.verify(installmentNoPIIRepositoryMock, Mockito.times(1)).getByOrganizationIdAndNav(organization.getOrganizationId(), receipt.getNoticeNumber());
  }


  private <T> SortedSet<T> keepFirstElements(SortedSet<T> set, int elementsToKeep) {
    if (set == null)
      return null;
    SortedSet<T> newSet = new TreeSet<>(set.comparator());
    if (!set.isEmpty()) {
      Spliterator<T> spliterator = set.spliterator();
      while (newSet.size() < elementsToKeep && spliterator.tryAdvance(newSet::add))
        ;
    }
    return newSet;
  }
}
