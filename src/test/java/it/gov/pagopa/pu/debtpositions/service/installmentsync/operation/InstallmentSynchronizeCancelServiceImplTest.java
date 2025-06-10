package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionCancelInstallmentService;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeCancelServiceImplTest {

  @Mock
  private DebtPositionCancelInstallmentService debtPositionCancelInstallmentServiceMock;

  private InstallmentSynchronizeCancelService installmentSynchronizeCancelService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeCancelService = new InstallmentSynchronizeCancelService(debtPositionCancelInstallmentServiceMock);
  }

  @Test
  void testInstallmentSyncCancelThenOk() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);
    InstallmentDTO installmentDTO = buildInstallmentDTO();
    installmentDTO.setIngestionFlowFileLineNumber(101L);
    installmentDTO.setIun(null);

    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO, List.of(installmentDTO), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }

  @Test
  void testInstallmentSyncCancelWithDPNullThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, null, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals(String.format("The debt position related to iupd %s was not found", installmentSynchronizeDTO.getIupdOrg()), notFoundException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithPaymentOptionNotFoundThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setPaymentOptionIndex(2);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals(String.format("The payment option with index %s of debt position with iupd %s not found",
      installmentSynchronizeDTO.getPaymentOptionIndex(), installmentSynchronizeDTO.getIupdOrg()), notFoundException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentNotFoundThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIud("iud2");
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with iud %s not found",
      installmentSynchronizeDTO.getIud()), notFoundException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentAlreadyElaboratedThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileLineNumber(100L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    WorkflowCreatedDTO result = installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertNull(result);
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentNotifiedThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals("The installment with id 100 cannot be updated or cancelled because is been notified by SEND", conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentPaidThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with iud %s cannot be updated or cancelled because is not in an allowed status: %s",
      installmentSynchronizeDTO.getIud(), InstallmentStatus.PAID), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentToSyncThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileId(3L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with iud %s cannot be updated or cancelled because there was an error in the previous synchronization",
      installmentSynchronizeDTO.getIud()), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentWithSyncStatusToPaidThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(
      InstallmentSyncStatus.builder().syncStatusTo(InstallmentStatus.PAID).build());
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);


    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with iud %s cannot be updated or cancelled because is not in an allowed status to: %s",
      installmentSynchronizeDTO.getIud(), InstallmentStatus.PAID), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentWithSyncStatusNullThenOk() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentWithSyncStatusValidThenOk() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()
      .setSyncStatus(InstallmentSyncStatus.builder().syncStatusTo(InstallmentStatus.UNPAID).build());
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }
}
