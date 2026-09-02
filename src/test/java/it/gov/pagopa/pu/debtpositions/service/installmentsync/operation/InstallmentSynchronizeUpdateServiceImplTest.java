package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.common.ConflictException;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.apply.InstallmentSynchronizeApplierService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionUpdateInstallmentService;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeUpdateServiceImplTest {

  @Mock
  private DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentServiceMock;
  @Mock
  private InstallmentSynchronizeApplierService installmentSynchronizeApplierServiceMock;

  private InstallmentSynchronizeUpdateService installmentSynchronizeUpdateService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeUpdateService = new InstallmentSynchronizeUpdateService(debtPositionUpdateInstallmentServiceMock,
      installmentSynchronizeApplierServiceMock);
  }

  @Test
  void testInstallmentSyncUpdateThenOk() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, debtPositionDTO,
        debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken))
      .thenReturn(Pair.of(debtPositionDTO, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()));

    when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }

  @Test
  void testInstallmentSyncUpdateWithDPNullThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, null, wfExecutionParameters, accessToken, operatorExternalUserId));

    assertEquals("DEBT_POSITION_NOT_FOUND", notFoundException.getCode());
    assertEquals(String.format("The debt position related to iupd %s was not found", installmentSynchronizeDTO.getIupdOrg()), notFoundException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithPaymentOptionNotFoundThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setPaymentOptionIndex(2);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));

    assertEquals("PAYMENT_OPTION_NOT_FOUND", notFoundException.getCode());
    assertEquals(String.format("The payment option with index %s of debt position with iupd %s not found",
      installmentSynchronizeDTO.getPaymentOptionIndex(), installmentSynchronizeDTO.getIupdOrg()), notFoundException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentNotFoundThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIud("iud2");
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));

    assertEquals("INSTALLMENT_NOT_FOUND", notFoundException.getCode());
    assertEquals(String.format("The installment with iud %s not found",
      installmentSynchronizeDTO.getIud()), notFoundException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentAlreadyElaboratedThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileLineNumber(100L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    WorkflowCreatedDTO result = installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertNull(result);
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentNotifiedThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    ConflictException conflictException = assertThrows(ConflictException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));

    assertEquals("INVALID_INSTALLMENT_STATUS", conflictException.getCode());
    assertEquals("The installment with iud "+installmentSynchronizeDTO.getIud()+" cannot be updated or cancelled because is been notified by SEND", conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentPaidThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    ConflictException conflictException = assertThrows(ConflictException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));

    assertEquals("INVALID_INSTALLMENT_STATUS", conflictException.getCode());
    assertEquals(String.format("The installment with iud %s cannot be updated or cancelled because is not in an allowed status: %s",
      installmentSynchronizeDTO.getIud(), InstallmentStatus.PAID), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentToSyncThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileId(3L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    ConflictException conflictException = assertThrows(ConflictException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));

    assertEquals("INVALID_INSTALLMENT_STATUS", conflictException.getCode());
    assertEquals(String.format("The installment with iud %s cannot be updated or cancelled because there was an error in the previous synchronization",
      installmentSynchronizeDTO.getIud()), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentWithSyncStatusToPaidThenThrowException() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(
      InstallmentSyncStatus.builder().syncStatusTo(InstallmentStatus.PAID).build());
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    ConflictException conflictException = assertThrows(ConflictException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId));

    assertEquals("INVALID_INSTALLMENT_STATUS", conflictException.getCode());
    assertEquals(String.format("The installment with iud %s cannot be updated or cancelled because is not in an allowed status to: %s",
      installmentSynchronizeDTO.getIud(), InstallmentStatus.PAID), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentWithSyncStatusNullThenOk() {
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(null);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, debtPositionDTO,
      debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken))
      .thenReturn(Pair.of(debtPositionDTO, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()));

    when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentWithSyncStatusValidThenOk() {
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

    when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, debtPositionDTO,
        debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken))
      .thenReturn(Pair.of(debtPositionDTO, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()));

    when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }
}
