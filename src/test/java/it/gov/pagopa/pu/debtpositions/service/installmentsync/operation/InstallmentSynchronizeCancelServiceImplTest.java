package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.BaseInstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionCancelInstallmentService;
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
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeCancelServiceImplTest {

  @Mock
  private DebtPositionCancelInstallmentService debtPositionCancelInstallmentServiceMock;
  @Mock
  private BaseInstallmentSynchronizeService baseInstallmentSynchronizeServiceMock;

  private InstallmentSynchronizeCancelServiceImpl installmentSynchronizeCancelService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeCancelService = new InstallmentSynchronizeCancelServiceImpl(debtPositionCancelInstallmentServiceMock, baseInstallmentSynchronizeServiceMock);
  }

  @Test
  void testInstallmentSyncCancelThenOk() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    String workflowId = "workflowId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO installmentDTO = buildInstallmentDTO();

    Mockito.when(baseInstallmentSynchronizeServiceMock.findInstallment(debtPositionDTO, installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getPaymentOptionIndex()))
      .thenReturn(installmentDTO);
    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO, List.of(installmentDTO), massive, accessToken, operatorExternalUserId))
      .thenReturn(Pair.of(debtPositionDTO, workflowId));

    String result = installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId);

    assertEquals(workflowId, result);
  }

  @Test
  void testInstallmentSyncCancelWithDPNullThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, null, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The debt position related to iupd %s was not found", installmentSynchronizeDTO.getIupdOrg()), notFoundException.getMessage());
  }



  @Test
  void testInstallmentSyncCancelWithInstallmentPaidThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);

    Mockito.when(baseInstallmentSynchronizeServiceMock.findInstallment(debtPositionDTO, installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getPaymentOptionIndex()))
      .thenReturn(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with iud %s cannot be cancelled because is not in an allowed status: %s",
      installmentSynchronizeDTO.getIud(), InstallmentStatus.PAID), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentToSyncThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIngestionFlowFileId(3L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);

    Mockito.when(baseInstallmentSynchronizeServiceMock.findInstallment(debtPositionDTO, installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getPaymentOptionIndex()))
      .thenReturn(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with %s cannot be cancelled because there was an error in the previous synchronization",
      installmentSynchronizeDTO.getIud()), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentWithSyncStatusToPaidThenThrowException() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIud("iud");
    installmentSynchronizeDTO.setPaymentOptionIndex(1);
    installmentSynchronizeDTO.setIngestionFlowFileId(1L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(
      InstallmentSyncStatus.builder().syncStatusTo(InstallmentStatus.PAID).build());

    Mockito.when(baseInstallmentSynchronizeServiceMock.findInstallment(debtPositionDTO, installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getPaymentOptionIndex()))
      .thenReturn(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    ConflictErrorException conflictException = assertThrows(ConflictErrorException.class, () ->
      installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId));
    assertEquals(String.format("The installment with %s cannot be cancelled because is not in an allowed status to: %s",
      installmentSynchronizeDTO.getIud(), InstallmentStatus.PAID), conflictException.getMessage());
  }

  @Test
  void testInstallmentSyncCancelWithInstallmentWithSyncStatusNullThenOk() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    String workflowId = "workflowId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIud("iud");
    installmentSynchronizeDTO.setPaymentOptionIndex(1);
    installmentSynchronizeDTO.setIngestionFlowFileId(1L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(null);

    Mockito.when(baseInstallmentSynchronizeServiceMock.findInstallment(debtPositionDTO, installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getPaymentOptionIndex()))
      .thenReturn(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), massive, accessToken, operatorExternalUserId))
      .thenReturn(Pair.of(debtPositionDTO, workflowId));

    String result = installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId);

    assertEquals(workflowId, result);
  }
  @Test
  void testInstallmentSyncCancelWithInstallmentWithSyncStatusValidThenOk() {
    boolean massive = true;
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    String workflowId = "workflowId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIud("iud");
    installmentSynchronizeDTO.setPaymentOptionIndex(1);
    installmentSynchronizeDTO.setIngestionFlowFileId(1L);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()
            .setSyncStatus(InstallmentSyncStatus.builder().syncStatusTo(InstallmentStatus.UNPAID).build());

    Mockito.when(baseInstallmentSynchronizeServiceMock.findInstallment(debtPositionDTO, installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getPaymentOptionIndex()))
            .thenReturn(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());

    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO,
                    List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), massive, accessToken, operatorExternalUserId))
            .thenReturn(Pair.of(debtPositionDTO, workflowId));

    String result = installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId);

    assertEquals(workflowId, result);
  }

}
