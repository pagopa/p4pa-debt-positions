package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.IuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
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

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeFaker.buildDebtPositionType;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
  private DebtPositionTypeRepository debtPositionTypeRepositoryMock;

  private DebtPositionCreationService createDebtPositionService;

  private final Long debtPositionTypeOrgId = 2L;

  @BeforeEach
  void setUp() {
    createDebtPositionService = new DebtPositionCreationServiceImpl(authorizeOperatorOnDebtPositionTypeServiceMock,
      validateDebtPositionServiceMock, debtPositionServiceMock, iuvServiceMock, debtPositionSyncServiceMock, installmentNoPIIRepositoryMock,
      debtPositionProcessorServiceMock, organizationServiceMock, debtPositionHierarchyStatusAlignerServiceMock,
      debtPositionTypeOrgRepositoryMock, debtPositionTypeRepositoryMock);
  }

  @Test
  void givenDebtPositionWhenCreateThenOk() {
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    String operatorExternalId = "OPERATOREXTERNALID";

    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeId())).thenReturn(Optional.of(buildDebtPositionType()));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, operatorExternalId)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, accessToken, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:"+iud, accessToken)).thenReturn(WorkflowCreatedDTO.builder().workflowId("1000").build());

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalId).getLeft();

    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(debtPositionDTO, result);
    assertEquals(DebtPositionStatus.TO_SYNC, result.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID), result.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDebtPositionOrdinarySilWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeId())).thenReturn(Optional.of(buildDebtPositionType()));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null)).thenReturn(0L);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:"+iud, null)).thenReturn(WorkflowCreatedDTO.builder().workflowId("1000").build());

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null).getLeft();

    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(debtPositionDTO, result);
    assertEquals(DebtPositionStatus.TO_SYNC, result.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID), result.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDebtPositionSpontaneousWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeId())).thenReturn(Optional.of(buildDebtPositionType()));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null)).thenReturn(0L);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:"+iud, null)).thenReturn(WorkflowCreatedDTO.builder().workflowId("1000").build());

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null).getLeft();

    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(debtPositionDTO, result);

    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(2)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDebtPositionOtherOriginWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setTransferIndex(1);
    debtPositionDTO.setFlagPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_FILE);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_FILE);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setIban("");
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeId())).thenReturn(Optional.of(buildDebtPositionType()));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), null)).thenReturn(0L);

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null).getLeft();

    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(debtPositionDTO, result);
    assertEquals(DebtPositionStatus.PAID, result.getStatus());
    assertEquals(PaymentOptionStatus.PAID, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.PAID, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

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
    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeId())).thenReturn(Optional.of(buildDebtPositionType()));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(2L);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class, () ->
      createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null)
    );
    assertEquals("Duplicate records found: the provided data conflicts with existing records.", exception.getMessage());

    Mockito.verify(debtPositionProcessorServiceMock).updateAmounts(debtPositionDTO);
  }

  @Test
  void givenDebtPositionWhenGenerateIuvThenAssignIuvAndIupdToInstallments() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIud("");
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIuv(null);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setBalance("");
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setTransferIndex(1);
    debtPositionDTO.setIupdOrg("");

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    Organization organization = buildOrganization();

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setFlagPagoPaPayment(true);
    debtPosition.setStatus(DebtPositionStatus.TO_SYNC);

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeId())).thenReturn(Optional.of(buildDebtPositionType()));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null, debtPositionTypeOrg);
    Mockito.when(iuvServiceMock.generateIuv(organization)).thenReturn("generatedIuv");
    Mockito.when(iuvServiceMock.iuv2Nav("generatedIuv")).thenReturn("generatedNav");
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), "randomIUD", "generatedIuv", "generatedNav")).thenReturn(0L);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:randomIUD", null)).thenReturn(WorkflowCreatedDTO.builder().workflowId("1000").build());

    try (MockedStatic<Utilities> mockedStatic = Mockito.mockStatic(Utilities.class)) {
      mockedStatic.when(Utilities::getRandomicUUID).thenReturn("randomUUID");
      mockedStatic.when(Utilities::getRandomIUD).thenReturn("randomIUD");
      mockedStatic.when(() -> Utilities.generateRandomIupd(organization.getOrgFiscalCode())).thenReturn("randomIUPD");

      DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null).getLeft();

      result.getPaymentOptions().stream()
        .flatMap(po -> po.getInstallments().stream())
        .forEach(inst -> assertEquals("generatedIuv", inst.getIuv()));
      reflectionEqualsByName(debtPositionDTO, result);

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
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.DRAFT);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeId())).thenReturn(Optional.of(buildDebtPositionType()));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null, debtPositionTypeOrg);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, wfExecutionParameters, null, null).getLeft();

    assertEquals(DebtPositionStatus.DRAFT, result.getStatus());
    assertEquals(PaymentOptionStatus.DRAFT, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.DRAFT, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    verify(debtPositionSyncServiceMock, times(0)).syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:"+iud, null);

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
    assertEquals("Provided organization id not found on db.", exception.getMessage());
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
    assertEquals("Provided organization is not ACTIVE", exception.getMessage());
  }
}
