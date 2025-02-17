package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusTransitionException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition.DebtPositionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption.PaymentOptionInnerStatusAlignerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransfer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DebtPositionHierarchyStatusAlignerServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private PaymentOptionInnerStatusAlignerService paymentOptionInnerStatusAlignerServiceMock;
  @Mock
  private DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerServiceMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  private DebtPositionHierarchyStatusAlignerService service;

  @BeforeEach
  void setUp() {
    service = new DebtPositionHierarchyStatusAlignerServiceImpl(debtPositionRepositoryMock, installmentNoPIIRepositoryMock, paymentOptionInnerStatusAlignerServiceMock, debtPositionInnerStatusAlignerServiceMock, debtPositionMapperMock);
  }

  @Test
  void givenFinalizeSyncStatusThenOk() {
    Long id = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    String iupdPagoPa = "iupdPagoPa";
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);
    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatusAndIupdPagopa(id, iupdPagoPa, newStatus);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().syncStatus(null);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = Map.of("iud", IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa(iupdPagoPa)
      .build());

    DebtPositionDTO result = service.finalizeSyncStatus(id, syncStatusDTO);

    assertEquals(DebtPositionStatus.UNPAID, result.getStatus());
    assertEquals(PaymentOptionStatus.UNPAID, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.UNPAID, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertNull(result.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    reflectionEqualsByName(debtPositionDTO, result);
  }

  @Test
  void givenFinalizeSyncStatusWhenIsNotSyncThenDoNotUpdateStatus() {
    Long id = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    String iupdPagoPa = "iupdPagoPa";
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa(iupdPagoPa)
      .build();

    syncStatusDTO.put("iud", iupdSyncStatusUpdateDTO);

    DebtPositionDTO result = service.finalizeSyncStatus(id, syncStatusDTO);

    assertNull(result);
    verify(debtPositionRepositoryMock).findOneWithAllDataByDebtPositionId(id);
    verify(installmentNoPIIRepositoryMock, times(0)).updateStatusAndIupdPagopa(id, iupdPagoPa, newStatus);
  }

  @Test
  void givenFinalizeSyncStatusWhenDoesNotHaveIudThenDoNotUpdateStatus() {
    Long id = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    String iupdPagoPa = "iupdPagoPa";
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa(iupdPagoPa)
      .build();

    syncStatusDTO.put("fake-iud", iupdSyncStatusUpdateDTO);

    DebtPositionDTO result = service.finalizeSyncStatus(id, syncStatusDTO);

    assertNull(result);
    verify(debtPositionRepositoryMock).findOneWithAllDataByDebtPositionId(id);
    verify(installmentNoPIIRepositoryMock, times(0)).updateStatusAndIupdPagopa(id, iupdPagoPa, newStatus);
  }

  @Test
  void givenFinalizeSyncStatusWhenDebtPositionNotFoundThenThrowsException() {
    Long id = 1L;
    String iupdPagoPa = "iupdPagoPa";
    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.finalizeSyncStatus(id, syncStatusDTO),
      "Debt position related to the id 1 does not found");

    verify(installmentNoPIIRepositoryMock, times(0)).updateStatusAndIupdPagopa(id, iupdPagoPa, InstallmentStatus.REPORTED);
    verify(paymentOptionInnerStatusAlignerServiceMock, times(0)).updatePaymentOptionStatus(any());
    verify(debtPositionInnerStatusAlignerServiceMock, times(0)).updateDebtPositionStatus(any());
  }

  @Test
  void givenNotifyReportedTransferIdWhenDebtPositionNotFoundThenThrowsException() {
    Long transferId = 1L;

    Mockito.when(debtPositionRepositoryMock.findByTransferId(transferId)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.notifyReportedTransferId(transferId),
      "Debt position related to the transfer with id 1 does not found");

    verify(installmentNoPIIRepositoryMock, times(0)).updateStatus(transferId, InstallmentStatus.REPORTED);
    verify(paymentOptionInnerStatusAlignerServiceMock, times(0)).updatePaymentOptionStatus(any());
    verify(debtPositionInnerStatusAlignerServiceMock, times(0)).updateDebtPositionStatus(any());
  }

  @Test
  void givenNotifyReportedTransferIdWhenInstallmentIsNotPaidThenThrowsException() {
    Long transferId = 1L;
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(new TreeSet<>(new ArrayList<>(List.of(buildTransfer()))));

    Mockito.when(debtPositionRepositoryMock.findByTransferId(transferId)).thenReturn(debtPosition);

    assertThrows(InvalidStatusTransitionException.class, () -> service.notifyReportedTransferId(transferId),
      "The installment with id 1 is in TO_SYNC status and cannot be set to reported status");

    verify(installmentNoPIIRepositoryMock, times(0)).updateStatus(transferId, InstallmentStatus.REPORTED);
    verify(paymentOptionInnerStatusAlignerServiceMock, times(0)).updatePaymentOptionStatus(any());
    verify(debtPositionInnerStatusAlignerServiceMock, times(0)).updateDebtPositionStatus(any());
  }

  @Test
  void givenNotifyReportedTransferIdWhenInstallmentAlreadyReportedThenOk() {
    Long transferId = 1L;
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.REPORTED);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(new TreeSet<>(new ArrayList<>(List.of(buildTransfer()))));

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.REPORTED);
    debtPositionDTOexpected.setStatus(DebtPositionStatus.REPORTED);

    Mockito.when(debtPositionRepositoryMock.findByTransferId(transferId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);

    DebtPositionDTO result = service.notifyReportedTransferId(transferId);

    assertEquals(DebtPositionStatus.REPORTED, result.getStatus());
    assertEquals(PaymentOptionStatus.REPORTED, result.getPaymentOptions().getFirst().getStatus());
    reflectionEqualsByName(debtPositionDTOexpected, result);
    verify(installmentNoPIIRepositoryMock, times(0)).updateStatus(transferId, InstallmentStatus.REPORTED);
  }

  @Test
  void givenNotifyReportedTransferIdThenOk() {
    Long transferId = 1L;
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(new TreeSet<>(new ArrayList<>(List.of(buildTransfer()))));

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.setStatus(DebtPositionStatus.REPORTED);
    debtPositionDTOexpected.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.REPORTED);
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.REPORTED);

    Mockito.when(debtPositionRepositoryMock.findByTransferId(transferId)).thenReturn(debtPosition);
    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatus(1L, InstallmentStatus.REPORTED);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);

    DebtPositionDTO result = service.notifyReportedTransferId(transferId);

    assertEquals(DebtPositionStatus.REPORTED, result.getStatus());
    assertEquals(PaymentOptionStatus.REPORTED, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.REPORTED, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    reflectionEqualsByName(debtPositionDTOexpected, result);
    verify(installmentNoPIIRepositoryMock, times(1)).updateStatus(transferId, InstallmentStatus.REPORTED);
    verify(paymentOptionInnerStatusAlignerServiceMock, times(1)).updatePaymentOptionStatus(any());
    verify(debtPositionInnerStatusAlignerServiceMock, times(1)).updateDebtPositionStatus(any());
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenDebtPositionNotFoundThenThrowsException() {
    Long debtPositionId = 1L;

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.checkAndUpdateInstallmentExpiration(debtPositionId),
      "Debt position related to the id 1 does not found");

    verify(installmentNoPIIRepositoryMock, times(0)).updateStatus(debtPositionId, InstallmentStatus.EXPIRED);
    verify(paymentOptionInnerStatusAlignerServiceMock, times(0)).updatePaymentOptionStatus(any());
    verify(debtPositionInnerStatusAlignerServiceMock, times(0)).updateDebtPositionStatus(any());
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenInstallmentIsInvalidThenOk() {
    Long debtPositionId = 1L;
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.INVALID);

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.INVALID);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);

    DebtPositionDTO result = service.checkAndUpdateInstallmentExpiration(debtPositionId);

    assertEquals(InstallmentStatus.INVALID, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    verify(installmentNoPIIRepositoryMock, times(0)).updateStatus(debtPositionId, InstallmentStatus.EXPIRED);
    verify(paymentOptionInnerStatusAlignerServiceMock, times(1)).updatePaymentOptionStatus(any());
    verify(debtPositionInnerStatusAlignerServiceMock, times(1)).updateDebtPositionStatus(any());
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenDueDateIsBeforeNowThenOk() {
    Long debtPositionId = 1L;
    OffsetDateTime dueDate = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.UNPAID);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(dueDate);

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.EXPIRED);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);

    DebtPositionDTO result = service.checkAndUpdateInstallmentExpiration(debtPositionId);

    assertEquals(InstallmentStatus.EXPIRED, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    verify(installmentNoPIIRepositoryMock, times(1)).updateStatus(debtPositionId, InstallmentStatus.EXPIRED);
    verify(paymentOptionInnerStatusAlignerServiceMock, times(1)).updatePaymentOptionStatus(any());
    verify(debtPositionInnerStatusAlignerServiceMock, times(1)).updateDebtPositionStatus(any());
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenDueDateIsAfterNowThenOk() {
    Long debtPositionId = 1L;
    OffsetDateTime dueDate = OffsetDateTime.now().plusDays(2);
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.UNPAID);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(dueDate);

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.UNPAID);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);

    DebtPositionDTO result = service.checkAndUpdateInstallmentExpiration(debtPositionId);

    assertEquals(InstallmentStatus.UNPAID, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    verify(installmentNoPIIRepositoryMock, times(0)).updateStatus(debtPositionId, InstallmentStatus.EXPIRED);
    verify(paymentOptionInnerStatusAlignerServiceMock, times(1)).updatePaymentOptionStatus(any());
    verify(debtPositionInnerStatusAlignerServiceMock, times(1)).updateDebtPositionStatus(any());
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenDueDateIsNullNowThenOk() {
    Long debtPositionId = 1L;
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.UNPAID);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(null);

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.UNPAID);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);

    DebtPositionDTO result = service.checkAndUpdateInstallmentExpiration(debtPositionId);

    assertEquals(InstallmentStatus.UNPAID, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    verify(installmentNoPIIRepositoryMock, times(0)).updateStatus(debtPositionId, InstallmentStatus.EXPIRED);
    verify(paymentOptionInnerStatusAlignerServiceMock, times(1)).updatePaymentOptionStatus(any());
    verify(debtPositionInnerStatusAlignerServiceMock, times(1)).updateDebtPositionStatus(any());
  }

}
