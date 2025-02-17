package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.GenerateIuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateDebtPositionServiceImplTest {

  @Mock
  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeServiceMock;
  @Mock
  private ValidateDebtPositionService validateDebtPositionServiceMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private GenerateIuvService generateIuvServiceMock;
  @Mock
  private DebtPositionSyncService debtPositionSyncServiceMock;
  @Mock
  private DebtPositionProcessorService debtPositionProcessorServiceMock;

  private CreateDebtPositionService createDebtPositionService;

  private final Long debtPositionTypeOrgId = 2L;

  @BeforeEach
  void setUp() {
    createDebtPositionService = new CreateDebtPositionServiceImpl(authorizeOperatorOnDebtPositionTypeServiceMock,
      validateDebtPositionServiceMock, debtPositionServiceMock, generateIuvServiceMock, debtPositionSyncServiceMock, installmentNoPIIRepositoryMock,
      debtPositionProcessorServiceMock);
  }

  @Test
  void givenDebtPositionWhenCreateThenOk() {
    String accessToken = "ACCESSTOKEN";
    boolean massive = false;
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    String operatorExternalId = "OPERATOREXTERNALID";

    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(debtPositionTypeOrgId, operatorExternalId)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, accessToken);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);
    Mockito.when(debtPositionProcessorServiceMock.updateAmounts(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, massive, PaymentEventType.DP_CREATED, accessToken)).thenReturn(WorkflowCreatedDTO.builder().workflowId("1000").build());
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO)).thenReturn(debtPositionDTO);

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, massive, accessToken, operatorExternalId);

    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(debtPositionDTO, result);
    assertEquals(DebtPositionStatus.TO_SYNC, result.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID), result.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
  }

  @Test
  void givenDebtPositionOrdinarySilWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL);

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);
    Mockito.when(debtPositionProcessorServiceMock.updateAmounts(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, false, PaymentEventType.DP_CREATED, null)).thenReturn(WorkflowCreatedDTO.builder().workflowId("1000").build());
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO)).thenReturn(debtPositionDTO);

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, false, null, null);

    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(debtPositionDTO, result);
    assertEquals(DebtPositionStatus.TO_SYNC, result.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID), result.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
  }

  @Test
  void givenDebtPositionSpontaneousWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);
    Mockito.when(debtPositionProcessorServiceMock.updateAmounts(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, false, PaymentEventType.DP_CREATED, null)).thenReturn(WorkflowCreatedDTO.builder().workflowId("1000").build());

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, false, null, null);

    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(debtPositionDTO, result);
  }

  @Test
  void givenDebtPositionOtherOriginWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_FILE);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_FILE);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);
    Mockito.when(debtPositionProcessorServiceMock.updateAmounts(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO)).thenReturn(debtPositionDTO);

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, false, null, null);

    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(debtPositionDTO, result);
    assertEquals(DebtPositionStatus.PAID, result.getStatus());
    assertEquals(PaymentOptionStatus.PAID, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.PAID, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
  }

  @Test
  void givenDebtPositionWithDuplicatesWhenCreateThenThrowConflictErrorException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(2L);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class, () ->
      createDebtPositionService.createDebtPosition(debtPositionDTO, false, null, null)
    );
    assertEquals("Duplicate records found: the provided data conflicts with existing records.", exception.getMessage());
  }

  @Test
  void givenDebtPositionWhenGenerateIuvThenAssignIuvToInstallments() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);

    DebtPositionDTO debtPositionIuvDTO = buildGeneratedIuvDebtPositionDTO();
    debtPositionIuvDTO.setFlagPagoPaPayment(true);
    debtPositionIuvDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionIuvDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.TO_SYNC);
    debtPositionIuvDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionIuvDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID));

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setFlagPagoPaPayment(true);
    debtPosition.setStatus(DebtPositionStatus.TO_SYNC);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);
    Mockito.when(generateIuvServiceMock.generateIuv(debtPositionDTO.getOrganizationId(), null)).thenReturn("generatedIuv");
    Mockito.when(generateIuvServiceMock.iuv2Nav("generatedIuv")).thenReturn("generatedNav");
    Mockito.when(debtPositionProcessorServiceMock.updateAmounts(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO)).thenReturn(debtPositionIuvDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionIuvDTO, false, PaymentEventType.DP_CREATED, null)).thenReturn(WorkflowCreatedDTO.builder().workflowId("1000").build());

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, false, null, null);

    result.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .forEach(inst -> assertEquals("generatedIuv", inst.getIuv()));
    reflectionEqualsByName(debtPositionIuvDTO, result);
  }

  @Test
  void givenCreateDPWhenStatusDraftThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.DRAFT);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionServiceMock).validate(debtPositionDTO, null);
    Mockito.when(installmentNoPIIRepositoryMock.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);
    Mockito.when(debtPositionProcessorServiceMock.updateAmounts(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO)).thenReturn(debtPositionDTO);

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, false, null, null);

    assertEquals(DebtPositionStatus.DRAFT, result.getStatus());
    assertEquals(PaymentOptionStatus.DRAFT, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.DRAFT, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    verify(debtPositionSyncServiceMock, times(0)).syncDebtPosition(debtPositionDTO, false, PaymentEventType.DP_CREATED, null);
  }
}
