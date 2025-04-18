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
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.ManageDebtPositionFaker.buildManageDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.ManageDebtPositionFaker.buildManageUpdateInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
  private static final String WORKFLOW_ID = "workflowId";
  private static final String WORKFLOW_ID_ADD = "workflowId_ADD";
  private static final String WORKFLOW_ID_UPDATE = "workflowId_UPDATE";
  private static final String WORKFLOW_ID_CANCEL = "workflowId_CANCEL";

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
    Organization organization = buildOrganization();

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO installment2insert = buildInstallmentDTO().installmentId(1L).iud("iud1");
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2insert);
    InstallmentDTO installment2update = buildInstallmentDTO().installmentId(2L).iud("iud2");
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2update);
    InstallmentDTO installment2cancel = buildInstallmentDTO().installmentId(3L).iud("iud3");
    debtPositionDTO.getPaymentOptions().getFirst().addInstallmentsItem(installment2cancel);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);
    Mockito.doNothing().when(debtPositionManageApplierServiceMock).merge(buildManageUpdateInstallmentDTO().getInstallment(), installment2update);
    Mockito.when(debtPositionAddInstallmentServiceMock.addInstallment(debtPositionDTO, List.of(installment2insert), wfExecutionParametersPartial, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_ID_ADD);
    Mockito.when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO, List.of(installment2update), wfExecutionParametersPartial, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_ID_UPDATE);
    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO, List.of(installment2cancel), wfExecutionParametersPartial, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_ID_CANCEL);

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), ACCESS_TOKEN)).thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), 2L, OPERATOR_EXTERNAL_ID)).thenReturn(buildDebtPositionTypeOrg());
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString()))
      .thenReturn(WorkflowCreatedDTO.builder().workflowId(WORKFLOW_ID).build());

    Pair<DebtPositionDTO, String> result = debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertEquals(WORKFLOW_ID, result.getRight());
    assertEquals(debtPositionDTO, result.getLeft());

    Mockito.verify(debtPositionProcessorServiceMock).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

}
