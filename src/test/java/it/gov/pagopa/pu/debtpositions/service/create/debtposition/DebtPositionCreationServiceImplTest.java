package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.CategoryResolverService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.IuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DebtPositionCreationServiceImplTest {

  @Mock
  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeServiceMock;
  @Mock
  private ValidateDebtPositionService validateDebtPositionServiceMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private IuvService iuvServiceMock;
  @Mock
  private DebtPositionSyncService debtPositionSyncServiceMock;
  @Mock
  private DebtPositionProcessorService debtPositionProcessorServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private CategoryResolverService categoryResolverServiceMock;

  private DebtPositionCreationService createDebtPositionService;

  private final Long debtPositionTypeOrgId = 2L;

  @BeforeEach
  void setUp() {
    createDebtPositionService = new DebtPositionCreationServiceImpl(authorizeOperatorOnDebtPositionTypeServiceMock,
      validateDebtPositionServiceMock, debtPositionServiceMock, iuvServiceMock, debtPositionSyncServiceMock, installmentNoPIIRepositoryMock,
      debtPositionProcessorServiceMock, organizationServiceMock, debtPositionHierarchyStatusAlignerServiceMock,
      debtPositionTypeOrgRepositoryMock, categoryResolverServiceMock);
  }

  @Test
  void givenDebtPositionWhenCreateThenOk() {
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    String operatorExternalId = "OPERATOREXTERNALID";
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, operatorExternalId)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, accessToken, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav(), InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:" + iud, accessToken))
      .thenReturn(workflow);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalId);

    assertSame(workflow, result);
    assertEquals(DebtPositionStatus.TO_SYNC, debtPosition.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, debtPosition.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID), debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDebtPositionWithNoTransferWhenCreateThenPopulateFirstTransfer() {
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    String operatorExternalId = "OPERATOREXTERNALID";
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(new ArrayList<>());
    TransferDTO expectedTransferDTO = TransferDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .category("001122233")
      .iban(debtPositionTypeOrg.getIban())
      .amountCents(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getAmountCents())
      .remittanceInformation(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getRemittanceInformation())
      .build();
    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, operatorExternalId)).thenReturn(debtPositionTypeOrg);
    Mockito.when(categoryResolverServiceMock.resolveCategory(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getLegacyPaymentMetadata(),
      debtPositionTypeOrg.getDebtPositionTypeId(), organization.getOrgTypeCode())).thenReturn("9/001122233/");
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, accessToken, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav(), InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:" + iud, accessToken))
      .thenReturn(workflow);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalId);

    assertSame(workflow, result);
    assertEquals(expectedTransferDTO, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst());

    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDebtPositionOrdinarySilWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPuPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    List<TransferDTO> transfersList = new ArrayList<>();
    transfersList.add(buildTransferDTO().transferIndex(2));
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(transfersList);

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null, InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:" + iud, null))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null);

    assertSame(expectedResult, result);
    assertEquals(DebtPositionStatus.TO_SYNC, debtPosition.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, debtPosition.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID), debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDebtPositionSpontaneousWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPuPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null, InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:" + iud, null))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null);

    assertSame(expectedResult, result);

    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDebtPositionOtherOriginWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setTransferIndex(1);
    debtPositionDTO.setFlagPuPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_FILE);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setIban("");
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPositionDTO.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null, InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null);

    assertNull(result);
    assertEquals(DebtPositionStatus.PAID, debtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.PAID, debtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.PAID, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDebtPositionWithDuplicatesWhenCreateThenThrowConflictErrorException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav(), InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(true);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class, () ->
      createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null)
    );
    assertEquals("[INSTALLMENT_ALREADY_EXISTS] Duplicate records found: the provided data conflicts with existing records", exception.getMessage());

    Mockito.verify(debtPositionProcessorServiceMock).updateAmounts(debtPositionDTO);
  }

  @Test
  void givenDebtPositionWhenGenerateIuvThenAssignIuvAndIupdToInstallments() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS_SIL);
    debtPositionDTO.setFlagPuPagoPaPayment(true);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIud("");
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIuv(null);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setBalance("");
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setTransferIndex(1);
    debtPositionDTO.setIupdOrg("");

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, null, debtPositionTypeOrg);
    Mockito.when(iuvServiceMock.generateIuv(organization)).thenReturn("generatedIuv");
    Mockito.when(iuvServiceMock.iuv2Nav("generatedIuv")).thenReturn("generatedNav");
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPositionDTO.getOrganizationId(), "randomIUD", "generatedIuv", "generatedNav", InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:randomIUD", null))
      .thenReturn(expectedResult);

    try (MockedStatic<Utilities> mockedStatic = Mockito.mockStatic(Utilities.class)) {
      mockedStatic.when(Utilities::getRandomicUUID).thenReturn("randomUUID");
      mockedStatic.when(Utilities::getRandomIUD).thenReturn("randomIUD");
      mockedStatic.when(() -> Utilities.generateRandomIupd(organization.getOrgFiscalCode())).thenReturn("randomIUPD");

      WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null);

      assertSame(expectedResult, result);
      debtPositionDTO.getPaymentOptions().stream()
        .flatMap(po -> po.getInstallments().stream())
        .forEach(inst -> assertEquals("generatedIuv", inst.getIuv()));

      Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
      Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
      Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
    }
  }

  @Test
  void givenCreateDPWhenStatusDraftThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPositionDTO.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav(), InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null);

    assertNull(result);
    assertEquals(DebtPositionStatus.DRAFT, debtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.DRAFT, debtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.DRAFT, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    verify(debtPositionSyncServiceMock, times(0)).syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:" + iud, null);

    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDebtPositionWithInvalidOrgWhenCreateThenThrowInvalidValueException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.empty());

    InvalidValueException exception = assertThrows(InvalidValueException.class, () ->
      createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null)
    );
    assertEquals("[INVALID_ORGANIZATION] Provided organization id not found on db.", exception.getMessage());
  }

  @Test
  void givenNotActiveOrgWhenCreateThenThrowInvalidValueException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    organization.setStatus(OrganizationStatus.DRAFT);

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));

    InvalidValueException exception = assertThrows(InvalidValueException.class, () ->
      createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null)
    );
    assertEquals("[INVALID_ORGANIZATION_STATUS] Provided organization is not ACTIVE", exception.getMessage());
  }

  @Test
  void givenInactiveDebtPositionTypeOrgWhenCreateThenKo() {
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    String operatorExternalId = "OPERATOREXTERNALID";

    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagActive(false);

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, operatorExternalId)).thenReturn(debtPositionTypeOrg);

    assertThrows(OperatorNotAuthorizedException.class, () ->
      createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalId));
  }

  @Test
  void givenDPWithOrdinaryOriginAndInstallmentWithNullSourceFlowNameWhenCreateDebtPositionThenSetDefaultValue() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setTransferIndex(1);
    debtPositionDTO.setFlagPuPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSourceFlowName(null);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPositionDTO.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null, InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null);

    assertNull(result);
    assertEquals(organization.getIpaCode() + "_IMPORT-DOVUTO_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSourceFlowName());
  }

  @Test
  void givenDPWithSpontaneousOriginAndInstallmentWithNullSourceFlowNameWhenCreateDebtPositionThenSetDefaultValue() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setTransferIndex(1);
    debtPositionDTO.setFlagPuPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSourceFlowName(null);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPositionDTO.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null, InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null);

    assertNull(result);
    assertEquals(organization.getIpaCode() + "_SPONTANEO_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSourceFlowName());
  }

  @Test
  void givenDPWithSpontaneousSilOriginAndInstallmentWithNullSourceFlowNameWhenCreateDebtPositionThenSetDefaultValue() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setTransferIndex(1);
    debtPositionDTO.setFlagPuPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS_SIL);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSourceFlowName(null);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPositionDTO.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null, InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null);

    assertNull(result);
    assertEquals(organization.getIpaCode() + "_SPONTANEO-SIL_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSourceFlowName());
  }

  @Test
  void givenDPWithSpontaneousPspOriginAndInstallmentWithNullSourceFlowNameWhenCreateDebtPositionThenSetDefaultValue() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setTransferIndex(1);
    debtPositionDTO.setFlagPuPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS_PSP);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSourceFlowName(null);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, organization, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.isInstallmentExists(debtPositionDTO.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null, InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)).thenReturn(false);

    WorkflowCreatedDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null);

    assertNull(result);
    assertEquals(organization.getIpaCode() + "_SPONTANEO-PSP_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSourceFlowName());
  }
}
