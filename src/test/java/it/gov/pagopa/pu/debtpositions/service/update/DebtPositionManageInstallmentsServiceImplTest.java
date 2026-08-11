package it.gov.pagopa.pu.debtpositions.service.update;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowHubService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.common.ConflictException;
import it.gov.pagopa.pu.debtpositions.exception.common.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.exception.custom.*;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.service.update.applier.DebtPositionManageApplierService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.ManageDebtPositionFaker.buildManageDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.ManageDebtPositionFaker.buildManageUpdateInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionManageInstallmentsServiceImplTest {

  @Mock
  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeServiceMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private DebtPositionSyncService debtPositionSyncServiceMock;
  @Mock
  private DebtPositionProcessorService debtPositionProcessorServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerServiceMock;
  @Mock
  private DebtPositionManageApplierService debtPositionManageApplierServiceMock;
  @Mock
  private DebtPositionAddInstallmentService debtPositionAddInstallmentServiceMock;
  @Mock
  private DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentServiceMock;
  @Mock
  private DebtPositionCancelInstallmentService debtPositionCancelInstallmentServiceMock;
  @Mock
  private WorkflowHubService workflowHubServiceMock;
  @Mock
  private ValidateDebtPositionService validateDebtPositionServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

  private DebtPositionManageInstallmentsService debtPositionManageInstallmentsService;

  private static final String OPERATOR_EXTERNAL_ID = "operatorExternalId";
  private static final String ACCESS_TOKEN = "accessToken";
  private static final WorkflowCreatedDTO WORKFLOW = new WorkflowCreatedDTO("workflowId", "runId");
  private static final WorkflowCreatedDTO WORKFLOW_ADD = new WorkflowCreatedDTO("workflowId_ADD", "runId");
  private static final WorkflowCreatedDTO WORKFLOW_UPDATE = new WorkflowCreatedDTO("workflowId_UPDATE", "runId");
  private static final WorkflowCreatedDTO WORKFLOW_CANCEL = new WorkflowCreatedDTO("workflowId_CANCEL", "runId");
  private static final String WORKFLOW_STATUS_COMPLETED_VALUE = "WORKFLOW_EXECUTION_STATUS_COMPLETED";
  private int retryDelayMs;
  private int maxRetries;

  @BeforeEach
  void setUp() {
    int maxWaitingMinutes = 5;
    retryDelayMs = 1000;
    maxRetries = (int) (((double) maxWaitingMinutes * 60_000) / retryDelayMs);
    debtPositionManageInstallmentsService = new DebtPositionManageInstallmentsServiceImpl(authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock, debtPositionSyncServiceMock, debtPositionProcessorServiceMock,
      organizationServiceMock, debtPositionHierarchyStatusAlignerServiceMock, debtPositionManageApplierServiceMock,
      debtPositionAddInstallmentServiceMock, debtPositionUpdateInstallmentServiceMock, debtPositionCancelInstallmentServiceMock,
      validateDebtPositionServiceMock, debtPositionTypeOrgRepositoryMock,
      workflowHubServiceMock, maxWaitingMinutes, retryDelayMs, objectMapper
      );
  }

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Test
  void givenDebtPositionInPaidStatusWhenManageThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictException exception = assertThrows(ConflictException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("INVALID_DEBT_POSITION_STATUS",exception.getCode());
    assertEquals("Debt position with id 1 cannot be modified because it is not in an allowed status: PAID", exception.getMessage());
  }

  @Test
  void givenPaymentOptionNotFoundWhenManageThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    manageDebtPositionDTO.setPaymentOptionId(1111L);
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    NotFoundException exception = assertThrows(NotFoundException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("PAYMENT_OPTION_NOT_FOUND",exception.getCode());
    assertEquals("Payment option having id 1111 not found", exception.getMessage());
  }

  @Test
  void givenPaymentOptionWithPaidStatusWhenManageThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.PAID);

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictException exception = assertThrows(ConflictException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("INVALID_PAYMENT_OPTION_STATUS",exception.getCode());
    assertEquals("Payment option having id 10 cannot be modified because is not in allowed status: PAID", exception.getMessage());
  }

  @Test
  void givenInstallmentWithPaidStatusWhenManageUpdateThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InstallmentDTO installment2update = buildInstallmentDTO().installmentId(2L).iud("iud2").status(InstallmentStatus.PAID);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2update);

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictException exception = assertThrows(ConflictException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("INVALID_INSTALLMENT_STATUS",exception.getCode());
    assertEquals("Installment having id 2 cannot be modified because is not in allowed status: PAID", exception.getMessage());
  }

  @Test
  void givenInstallmentNotFoundWhenManageUpdateThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    NotFoundException exception = assertThrows(NotFoundException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("INSTALLMENT_NOT_FOUND",exception.getCode());
    assertEquals("The installment with id 2 not found", exception.getMessage());
  }

  @Test
  void givenInstallmentAlreadyNotifiedWhenManageUpdateThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InstallmentDTO installment2update = buildInstallmentDTO().installmentId(2L).iun("iun")
      .status(InstallmentStatus.UNPAID).syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2update);

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictException exception = assertThrows(ConflictException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("INVALID_INSTALLMENT_STATUS",exception.getCode());
    assertEquals("The installment with id 2 cannot be modified because is been notified by SEND", exception.getMessage());
  }

  @Test
  void givenWorkflowNotCompletedWhenManageUpdateThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = ManageDebtPositionDTO.builder()
      .debtPositionDescription("debtPositionDescription")
      .paymentOptionDescription("paymentOptionDescription")
      .paymentOptionId(10L)
      .installments(new ArrayList<>(List.of(buildManageUpdateInstallmentDTO())))
      .build();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    WfExecutionParameters wfExecutionParametersPartial = WfExecutionParameters.builder().massive(false).partialChange(true).build();

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionTypeOrgId(2L);
    debtPositionDTO.setOrganizationId(500L);

    InstallmentDTO installment2update = buildInstallmentDTO().installmentId(2L).iun(null).status(InstallmentStatus.UNPAID).syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2update);
    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setCode("ORG_TYPE_CODE");

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), ACCESS_TOKEN))
      .thenReturn(Optional.of(buildOrganization()));

    when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId()))
      .thenReturn(Optional.of(typeOrg));

    when(debtPositionUpdateInstallmentServiceMock.updateInstallment(Mockito.any(), Mockito.any(), Mockito.eq(wfExecutionParametersPartial), Mockito.eq(ACCESS_TOKEN), Mockito.eq(OPERATOR_EXTERNAL_ID)))
      .thenReturn(WORKFLOW_UPDATE);
    when(workflowHubServiceMock.waitWorkflowCompletion(ACCESS_TOKEN, WORKFLOW_UPDATE.getWorkflowId(), maxRetries, retryDelayMs)).thenReturn("FAILED");

    WorkflowErrorException exception = assertThrows(WorkflowErrorException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("WORKFLOW_EXECUTION_ERROR",exception.getCode());
    assertEquals("Workflow with id workflowId_UPDATE terminated with error", exception.getMessage());
  }

  @Test
  void givenValidActionOnDpWhenManageThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParametersPartial = WfExecutionParameters.builder().massive(false).partialChange(true).build();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder().massive(false).partialChange(false).build();

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionTypeOrgId(2L);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()
      .status(InstallmentStatus.UNPAID).syncStatus(null);
    InstallmentDTO installment2insert = buildInstallmentDTO().installmentId(1L).iud("iud1")
      .status(InstallmentStatus.UNPAID).syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2insert);
    InstallmentDTO installment2update = buildInstallmentDTO().installmentId(2L).iud("iud2").iun(null)
      .status(InstallmentStatus.UNPAID).syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2update);
    InstallmentDTO installment2cancel = buildInstallmentDTO().installmentId(3L).iud("iud3").iun(null)
      .status(InstallmentStatus.UNPAID).syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2cancel);

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setCode("ORG_TYPE_CODE");

    when(debtPositionServiceMock.getDebtPosition(debtPositionId))
      .thenReturn(debtPositionDTO);

    when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), ACCESS_TOKEN))
      .thenReturn(Optional.of(buildOrganization()));

    when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId()))
      .thenReturn(Optional.of(typeOrg));

    Mockito.doNothing().when(debtPositionManageApplierServiceMock).merge(Mockito.any(), Mockito.any());

    when(debtPositionAddInstallmentServiceMock.addInstallment(debtPositionDTO, List.of(installment2insert), wfExecutionParametersPartial, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_ADD);
    when(workflowHubServiceMock.waitWorkflowCompletion(ACCESS_TOKEN, WORKFLOW_ADD.getWorkflowId(), maxRetries, retryDelayMs)).thenReturn(WORKFLOW_STATUS_COMPLETED_VALUE);
    when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO, List.of(installment2update), wfExecutionParametersPartial, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_UPDATE);
    when(workflowHubServiceMock.waitWorkflowCompletion(ACCESS_TOKEN, WORKFLOW_UPDATE.getWorkflowId(), maxRetries, retryDelayMs)).thenReturn(WORKFLOW_STATUS_COMPLETED_VALUE);
    when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO, List.of(installment2cancel), wfExecutionParametersPartial, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_CANCEL);
    when(workflowHubServiceMock.waitWorkflowCompletion(ACCESS_TOKEN, WORKFLOW_CANCEL.getWorkflowId(), maxRetries, retryDelayMs)).thenReturn(WORKFLOW_STATUS_COMPLETED_VALUE);

    when(debtPositionSyncServiceMock.syncDebtPosition(Mockito.any(), Mockito.eq(wfExecutionParameters), Mockito.eq(PaymentEventType.DP_UPDATED), Mockito.any(), Mockito.eq(ACCESS_TOKEN)))
      .thenReturn(WORKFLOW);

    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertSame(WORKFLOW, result.getRight());
    assertEquals(debtPositionDTO, result.getLeft());
  }

  @Test
  void givenInvalidInstallmentDataWhenManageUpdateThenValidationException() {
    // Given
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = ManageDebtPositionDTO.builder()
      .debtPositionDescription("Desc")
      .paymentOptionDescription("PoDesc")
      .paymentOptionId(10L)
      .installments(new ArrayList<>(List.of(buildManageUpdateInstallmentDTO())))
      .build();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionTypeOrgId(2L);

    InstallmentDTO installment2update = buildInstallmentDTO()
      .installmentId(2L).iun(null)
      .status(InstallmentStatus.UNPAID)
      .syncStatus(null);

    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2update);

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setCode("ORG_TYPE_CODE");

    Organization org = buildOrganization();

    InvalidValueException expectedException = new InvalidValueException("CODE", "Taxonomy invalid");

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);
    when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), ACCESS_TOKEN))
      .thenReturn(Optional.of(org));
    when(debtPositionTypeOrgRepositoryMock.findById(debtPositionDTO.getDebtPositionTypeOrgId()))
      .thenReturn(Optional.of(typeOrg));
    doThrow(expectedException)
      .when(validateDebtPositionServiceMock).validateInstallment(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    // When
    InvalidValueException resultException = assertThrows(InvalidValueException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    // Then
    assertSame(expectedException, resultException);

    verify(debtPositionUpdateInstallmentServiceMock, never())
      .updateInstallment(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void givenJsonProcessingErrorWhenManageUpdateThenServerErrorException() throws Exception {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = ManageDebtPositionDTO.builder()
      .debtPositionDescription("Desc")
      .paymentOptionDescription("PoDesc")
      .paymentOptionId(10L)
      .installments(new ArrayList<>(List.of(buildManageUpdateInstallmentDTO())))
      .build();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionTypeOrgId(2L);
    InstallmentDTO installment2update = buildInstallmentDTO()
      .installmentId(2L).iun(null)
      .status(InstallmentStatus.UNPAID)
      .syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2update);

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setCode("ORG_TYPE_CODE");

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    doThrow(new com.fasterxml.jackson.databind.JsonMappingException(null, "Error"))
      .when(objectMapper).writeValueAsString(Mockito.any());

    InstallmentCloningException exception = assertThrows(InstallmentCloningException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("INSTALLMENT_CLONING_ERROR",exception.getCode());
    assertEquals("Error cloning installment with id 2", exception.getMessage());
  }

  @Test
  void givenTypeOrgNotFoundWhenManageThenNotFoundException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = ManageDebtPositionDTO.builder()
      .debtPositionDescription("Desc")
      .paymentOptionDescription("PoDesc")
      .paymentOptionId(10L)
      .installments(new ArrayList<>(List.of(buildManageUpdateInstallmentDTO())))
      .build();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionTypeOrgId(99L);
    debtPositionDTO.setOrganizationId(500L);

    InstallmentDTO installment2update = buildInstallmentDTO()
      .installmentId(2L)
      .iun(null)
      .status(InstallmentStatus.UNPAID)
      .syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2update);

    when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);
    when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), ACCESS_TOKEN))
      .thenReturn(Optional.of(buildOrganization()));
    when(debtPositionTypeOrgRepositoryMock.findById(99L)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("DEBT_POSITION_TYPE_ORG_NOT_FOUND",exception.getCode());
    assertEquals("The debt position type org with id 99 was not found for organization id 500", exception.getMessage());
  }
}
