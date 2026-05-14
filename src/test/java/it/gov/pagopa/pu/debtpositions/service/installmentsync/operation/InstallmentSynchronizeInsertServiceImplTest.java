package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.apply.InstallmentSynchronizeApplierService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionAddInstallmentService;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
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
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildSyncDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildSyncInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeInsertServiceImplTest {

  @Mock
  private InstallmentSynchronizeApplierService installmentSynchronizeApplierServiceMock;
  @Mock
  private DebtPositionCreationService debtPositionCreationServiceMock;
  @Mock
  private DebtPositionAddInstallmentService debtPositionAddInstallmentServiceMock;

  private InstallmentSynchronizeInsertService installmentSynchronizeInsertService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeInsertService = new InstallmentSynchronizeInsertService(installmentSynchronizeApplierServiceMock,
      debtPositionCreationServiceMock, debtPositionAddInstallmentServiceMock);
  }

  @Test
  void testInstallmentSyncAddOneInstallment() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIud("IUD_NEW");

    InstallmentDTO newInstallmentDTO = buildSyncInstallmentDTO();

    Mockito.when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, debtPositionDTO,
        debtPositionDTO.getPaymentOptions().getFirst(), null, accessToken))
      .thenReturn(Pair.of(debtPositionDTO, newInstallmentDTO));

    Mockito.when(debtPositionAddInstallmentServiceMock.addInstallment(debtPositionDTO, List.of(newInstallmentDTO), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
    verify(debtPositionCreationServiceMock, times(0)).createDebtPosition(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void testInstallmentSyncAddOneAlreadyElaboratedInstallment() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileLineNumber(100L);

    WorkflowCreatedDTO result = installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertNull(result);
  }

  @Test
  void testInstallmentSyncCreateDebtPositionInstallment() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO newDebtPositionDTO = buildSyncDebtPositionDTO();
    newDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.CANCELLED);
    newDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIngestionFlowFileLineNumber(32L);

    Mockito.when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, newDebtPositionDTO,
        newDebtPositionDTO.getPaymentOptions().getFirst(), newDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken))
      .thenReturn(Pair.of(newDebtPositionDTO, newDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()));

    Mockito.when(debtPositionCreationServiceMock.createDebtPosition(newDebtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, newDebtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
    verify(debtPositionAddInstallmentServiceMock, times(0)).addInstallment(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void testInstallmentSyncDPStatusNotAllowedInstallment() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals("INVALID_DEBT_POSITION_STATUS",conflictException.getCode());
    assertEquals("The installment cannot be created because the debt position with iupd IUPD_ORG is not in an allowed status: PAID", conflictException.getMessage());
  }

  @Test
  void testDPToSyncAndIsNotDPToSyncAllowedInstallment() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileId(5L);

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals("INVALID_DEBT_POSITION_STATUS",conflictException.getCode());
    assertEquals("The installment cannot be created because the debt position with iupd IUPD_ORG is in TO_SYNC status for a previous synchronization", conflictException.getMessage());
  }

  @Test
  void testPOToSyncAndIsNotDPToSyncAllowedInstallment() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.TO_SYNC);
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileId(5L);

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals("INVALID_PAYMENT_OPTION_STATUS",conflictException.getCode());
    assertEquals("The installment cannot be created because the payment option with index 1 is in TO_SYNC status for a previous synchronization", conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncPOStatusNotAllowedInstallment() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.PAID);
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals("INVALID_PAYMENT_OPTION_STATUS",conflictException.getCode());
    assertEquals("The installment cannot be created because the payment option with index 1 is not in an allowed status: PAID", conflictException.getMessage());
  }


  @Test
  void testInstallmentSyncInstallmentStatusNotAllowedInstallment() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals("INVALID_INSTALLMENT_STATUS",conflictException.getCode());
    assertEquals("The installment with iud iud cannot be created because it already exists in a not modifiable status: PAID", conflictException.getMessage());
  }

  @Test
  void givenPartiallyPaidStatusAndNewPOWhensyncInstallmentThenConflictErrorException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.PARTIALLY_PAID);
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setPaymentOptionIndex(99);

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals(ErrorCodeConstants.ERROR_CODE_INVALID_DEBT_POSITION_STATUS,conflictException.getCode());
    assertEquals("The installment and its payment option cannot be created because the debt position with iupd "+debtPositionDTO.getIupdOrg()+" is in PARTIALLY_PAID status", conflictException.getMessage());
  }

  @Test
  void givenNewPOAndUnpaidDPStatusWhensyncInstallmentThenOk() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setPaymentOptionIndex(99);
    InstallmentDTO newInstallmentDTO = buildSyncInstallmentDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, debtPositionDTO,
        null, null, accessToken))
      .thenReturn(Pair.of(debtPositionDTO, newInstallmentDTO));

    Mockito.when(debtPositionAddInstallmentServiceMock.addInstallment(debtPositionDTO, List.of(newInstallmentDTO), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
    verify(debtPositionCreationServiceMock, times(0)).createDebtPosition(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void givenNewPOAndNoDPStatusWhensyncInstallmentThenOk() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setPaymentOptionIndex(99);
    InstallmentDTO newInstallmentDTO = buildSyncInstallmentDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, null,
        null, null, accessToken))
      .thenReturn(Pair.of(debtPositionDTO, newInstallmentDTO));

    Mockito.when(debtPositionAddInstallmentServiceMock.addInstallment(debtPositionDTO, List.of(newInstallmentDTO), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, null, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
    verify(debtPositionCreationServiceMock, times(0)).createDebtPosition(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }
}
