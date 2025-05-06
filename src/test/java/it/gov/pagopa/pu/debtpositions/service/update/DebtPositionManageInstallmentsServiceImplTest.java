package it.gov.pagopa.pu.debtpositions.service.update;


import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.service.update.applier.DebtPositionManageApplierService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.ManageDebtPositionFaker.buildManageDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.ManageDebtPositionFaker.buildManageUpdateInstallmentDTO;
import static org.junit.jupiter.api.Assertions.*;

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

  private DebtPositionManageInstallmentsService debtPositionManageInstallmentsService;

  private static final String OPERATOR_EXTERNAL_ID = "operatorExternalId";
  private static final String ACCESS_TOKEN = "accessToken";
  private static final WorkflowCreatedDTO WORKFLOW = new WorkflowCreatedDTO("workflowId", "runId");
  private static final WorkflowCreatedDTO WORKFLOW_ADD = new WorkflowCreatedDTO("workflowId_ADD", "runId");
  private static final WorkflowCreatedDTO WORKFLOW_UPDATE = new WorkflowCreatedDTO("workflowId_UPDATE", "runId");
  private static final WorkflowCreatedDTO WORKFLOW_CANCEL = new WorkflowCreatedDTO("workflowId_CANCEL", "runId");

  @BeforeEach
  void setUp() {
    debtPositionManageInstallmentsService = new DebtPositionManageInstallmentsServiceImpl(authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock, debtPositionSyncServiceMock, debtPositionProcessorServiceMock,
      organizationServiceMock, debtPositionHierarchyStatusAlignerServiceMock, debtPositionManageApplierServiceMock,
      debtPositionAddInstallmentServiceMock, debtPositionUpdateInstallmentServiceMock, debtPositionCancelInstallmentServiceMock);
  }

  @Test
  void givenDebtPositionInPaidStatusWhenManageThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("Debt position with id 1 cannot be modified because it is not in an allowed status: PAID", exception.getMessage());
  }

  @Test
  void givenPaymentOptionNotFoundWhenManageThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    manageDebtPositionDTO.setPaymentOptionId(1111L);
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    NotFoundException exception = assertThrows(NotFoundException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("Payment option having id 1111 not found", exception.getMessage());
  }

  @Test
  void givenPaymentOptionWithPaidStatusWhenManageThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.PAID);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

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

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("Installment having id 2 cannot be modified because is not in allowed status: PAID", exception.getMessage());
  }

  @Test
  void givenInstallmentNotFoundWhenManageUpdateThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    NotFoundException exception = assertThrows(NotFoundException.class,
      () -> debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("The installment with id 2 not found", exception.getMessage());
  }

  @Test
  void givenValidActionOnDpWhenManageThenException() {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO manageDebtPositionDTO = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParametersPartial = WfExecutionParameters.builder().massive(false).partialChange(true).build();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder().massive(false).partialChange(false).build();

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()
      .status(InstallmentStatus.UNPAID).syncStatus(null);
    InstallmentDTO installment2insert = buildInstallmentDTO().installmentId(1L).iud("iud1")
      .status(InstallmentStatus.UNPAID).syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2insert);
    InstallmentDTO installment2update = buildInstallmentDTO().installmentId(2L).iud("iud2")
      .status(InstallmentStatus.UNPAID).syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2update);
    InstallmentDTO installment2cancel = buildInstallmentDTO().installmentId(3L).iud("iud3")
      .status(InstallmentStatus.UNPAID).syncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2cancel);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId))
      .thenReturn(debtPositionDTO);
    Mockito.doNothing().when(debtPositionManageApplierServiceMock).merge(buildManageUpdateInstallmentDTO().getInstallment(), installment2update);
    Mockito.when(debtPositionAddInstallmentServiceMock.addInstallment(debtPositionDTO, List.of(installment2insert), wfExecutionParametersPartial, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_ADD);
    Mockito.when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO, List.of(installment2update), wfExecutionParametersPartial, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_UPDATE);
    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO, List.of(installment2cancel), wfExecutionParametersPartial, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_CANCEL);

    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_UPDATED, "IUD:iud1,iud2,iud3", ACCESS_TOKEN))
      .thenReturn(WORKFLOW);

    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertSame(WORKFLOW, result.getRight());
    assertEquals(debtPositionDTO, result.getLeft());
  }

}
