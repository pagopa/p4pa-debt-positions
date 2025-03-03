package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.apply.InstallmentSynchronizeApplierService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionUpdateInstallmentService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeUpdateServiceImplTest {

  @Mock
  private DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentServiceMock;
  @Mock
  private InstallmentSynchronizeApplierService installmentSynchronizeApplierServiceMock;

  private InstallmentSynchronizeUpdateServiceImpl installmentSynchronizeUpdateService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeUpdateService = new InstallmentSynchronizeUpdateServiceImpl(debtPositionUpdateInstallmentServiceMock,
      installmentSynchronizeApplierServiceMock);
  }

  @Test
  void testInstallmentSyncUpdateThenOk() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    String workflowId = "workflowId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, debtPositionDTO,
        debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken))
      .thenReturn(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    Mockito.when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), massive, accessToken, operatorExternalUserId))
      .thenReturn(Pair.of(debtPositionDTO, workflowId));

    String result = installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId);

    assertEquals(workflowId, result);
  }

  @Test
  void testInstallmentSyncUpdateWithDPNullThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, null, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The debt position related to iupd %s was not found", installmentSynchronizeDTO.getIupdOrg()), notFoundException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithPaymentOptionNotFoundThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setPaymentOptionIndex(2);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The payment option with index %s not found",
      installmentSynchronizeDTO.getPaymentOptionIndex()), notFoundException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentNotFoundThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIud("iud2");
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with iud %s not found",
      installmentSynchronizeDTO.getIud()), notFoundException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentAlreadyElaboratedThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileLineNumber(100L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    String result = installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId);

    assertNull(result);
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentPaidThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with iud %s cannot be updated or cancelled because is not in an allowed status: %s",
      installmentSynchronizeDTO.getIud(), InstallmentStatus.PAID), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentToSyncThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileId(3L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with iud %s cannot be updated or cancelled because there was an error in the previous synchronization",
      installmentSynchronizeDTO.getIud()), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentWithSyncStatusToPaidThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(
      InstallmentSyncStatus.builder().syncStatusTo(InstallmentStatus.PAID).build());

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with iud %s cannot be updated or cancelled because is not in an allowed status to: %s",
      installmentSynchronizeDTO.getIud(), InstallmentStatus.PAID), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentWithSyncStatusNullThenOk() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    String workflowId = "workflowId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(null);

    Mockito.when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, debtPositionDTO,
      debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken))
      .thenReturn(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    Mockito.when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), massive, accessToken, operatorExternalUserId))
      .thenReturn(Pair.of(debtPositionDTO, workflowId));

    String result = installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId);

    assertEquals(workflowId, result);
  }

  @Test
  void testInstallmentSyncUpdateWithInstallmentWithSyncStatusValidThenOk() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    String workflowId = "workflowId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()
      .setSyncStatus(InstallmentSyncStatus.builder().syncStatusTo(InstallmentStatus.UNPAID).build());

    Mockito.when(installmentSynchronizeApplierServiceMock.apply(installmentSynchronizeDTO, debtPositionDTO,
        debtPositionDTO.getPaymentOptions().getFirst(), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken))
      .thenReturn(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    Mockito.when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), massive, accessToken, operatorExternalUserId))
      .thenReturn(Pair.of(debtPositionDTO, workflowId));

    String result = installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId);

    assertEquals(workflowId, result);
  }
}
