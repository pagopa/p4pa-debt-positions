package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusTransitionException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition.DebtPositionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption.PaymentOptionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransfer;
import static org.junit.jupiter.api.Assertions.*;
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
  @Mock
  private DebtPositionSyncService syncServiceMock;

  private DebtPositionHierarchyStatusAlignerServiceImpl service;

  @BeforeEach
  void setUp() {
    service = Mockito.spy(
      new DebtPositionHierarchyStatusAlignerServiceImpl(
        debtPositionRepositoryMock,
        installmentNoPIIRepositoryMock,
        paymentOptionInnerStatusAlignerServiceMock,
        debtPositionInnerStatusAlignerServiceMock,
        debtPositionMapperMock,
        syncServiceMock)
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      debtPositionRepositoryMock,
      installmentNoPIIRepositoryMock,
      paymentOptionInnerStatusAlignerServiceMock,
      debtPositionInnerStatusAlignerServiceMock,
      debtPositionMapperMock,
      syncServiceMock
    );
  }

  @Test
  void givenFinalizeSyncStatusThenOk() {
    Long debtPositionId = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatus(100L, newStatus);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().syncStatus(null);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = Map.of("iud", IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .build());

    DebtPositionDTO result = service.finalizeSyncStatus(debtPositionId, syncStatusDTO);

    assertEquals(DebtPositionStatus.UNPAID, result.getStatus());
    assertEquals(PaymentOptionStatus.UNPAID, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.UNPAID, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertNull(result.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    reflectionEqualsByName(debtPositionDTO, result);
  }

  @Test
  void givenFinalizeSyncStatusWhenIsNotSyncThenDoNotUpdateStatus() {
    Long debtPositionId = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    Mockito.doReturn(expectedResult)
      .when(service).alignHierarchyStatusAndRemap(Mockito.same(debtPosition));

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .build();

    syncStatusDTO.put("iud", iupdSyncStatusUpdateDTO);

    DebtPositionDTO result = service.finalizeSyncStatus(debtPositionId, syncStatusDTO);

    assertSame(expectedResult, result);
  }

  @Test
  void givenFinalizeSyncStatusWhenDoesNotHaveIudThenDoNotUpdateStatus() {
    Long debtPositionId = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    Mockito.doReturn(expectedResult)
      .when(service).alignHierarchyStatusAndRemap(Mockito.same(debtPosition));

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .build();

    syncStatusDTO.put("fake-iud", iupdSyncStatusUpdateDTO);

    DebtPositionDTO result = service.finalizeSyncStatus(debtPositionId, syncStatusDTO);

    assertSame(expectedResult, result);
  }

  @Test
  void givenFinalizeSyncStatusWhenDebtPositionNotFoundThenThrowsException() {
    Long debtPositionId = 1L;
    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.finalizeSyncStatus(debtPositionId, syncStatusDTO),
      "Debt position related to the id 1 does not found");
  }

  @Test
  void givenNotifyReportedTransferIdWhenDebtPositionNotFoundThenThrowsException() {
    Long transferId = 1000L;
    String accessToken = "ACCESSTOKEN";
    TransferReportedRequest request = TransferReportedRequest.builder()
      .iuf("IUF")
      .build();

    Mockito.when(debtPositionRepositoryMock.findByTransferId(transferId)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.notifyReportedTransferId(transferId, request, accessToken),
      "Debt position related to the transfer with id 1 does not found");
  }

  @Test
  void givenNotifyReportedTransferIdWhenInstallmentIsNotPaidThenThrowsException() {
    Long transferId = 1000L;
    String accessToken = "ACCESSTOKEN";
    TransferReportedRequest request = TransferReportedRequest.builder()
      .iuf("IUF")
      .build();
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(new TreeSet<>(new ArrayList<>(List.of(buildTransfer()))));

    Mockito.when(debtPositionRepositoryMock.findByTransferId(transferId)).thenReturn(debtPosition);

    assertThrows(InvalidStatusTransitionException.class, () -> service.notifyReportedTransferId(transferId, request, accessToken),
      "The installment with id 1 is in TO_SYNC status and cannot be set to reported status");
  }

  @Test
  void givenNotifyReportedTransferIdWhenInstallmentAlreadyReportedThenOk() {
    Long transferId = 1000L;
    String accessToken = "ACCESSTOKEN";
    TransferReportedRequest request = TransferReportedRequest.builder()
      .iuf("IUF")
      .build();
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

    Pair<DebtPositionDTO, String> result = service.notifyReportedTransferId(transferId, request, accessToken);

    assertEquals(DebtPositionStatus.REPORTED, result.getLeft().getStatus());
    assertEquals(PaymentOptionStatus.REPORTED, result.getLeft().getPaymentOptions().getFirst().getStatus());
    reflectionEqualsByName(debtPositionDTOexpected, result);
    assertNull(result.getRight());
  }

  @Test
  void givenNotifyReportedTransferIdThenOk() {
    Long transferId = 1000L;
    String accessToken = "ACCESSTOKEN";
    TransferReportedRequest request = TransferReportedRequest.builder()
      .iuf("IUF")
      .build();
    DebtPosition debtPosition = buildDebtPosition();
    InstallmentNoPII reportedInstallment = debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst();
    reportedInstallment.setStatus(InstallmentStatus.PAID);
    reportedInstallment.setTransfers(new TreeSet<>(new ArrayList<>(List.of(buildTransfer()))));

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.setStatus(DebtPositionStatus.REPORTED);
    debtPositionDTOexpected.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.REPORTED);
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.REPORTED);
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setIuf(request.getIuf());

    Mockito.when(debtPositionRepositoryMock.findByTransferId(transferId)).thenReturn(debtPosition);
    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatusAndIuf(100L, InstallmentStatus.REPORTED, request.getIuf());
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.eq(PaymentEventType.DPI_REPORTED), Mockito.eq("IUD:"+reportedInstallment.getIud()), Mockito.same(accessToken)))
      .thenReturn(new WorkflowCreatedDTO("WFID"));

    Pair<DebtPositionDTO, String> result = service.notifyReportedTransferId(transferId, request, accessToken);

    assertEquals(DebtPositionStatus.REPORTED, result.getLeft().getStatus());
    assertEquals(PaymentOptionStatus.REPORTED, result.getLeft().getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.REPORTED, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertEquals(request.getIuf(), result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getIuf());
    reflectionEqualsByName(debtPositionDTOexpected, result);
    assertEquals("WFID", result.getRight());

    assertEquals(InstallmentStatus.REPORTED, reportedInstallment.getStatus());
    assertEquals(request.getIuf(), reportedInstallment.getIuf());
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenDebtPositionNotFoundThenThrowsException() {
    Long debtPositionId = 1L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.checkAndUpdateInstallmentExpiration(debtPositionId, accessToken),
      "Debt position related to the id 1 does not found");
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenInstallmentIsInvalidThenOk() {
    Long debtPositionId = 1L;
    String accessToken = "ACCESSTOKEN";
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.INVALID);

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.INVALID);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.isNull(), Mockito.isNull(), Mockito.same(accessToken)))
      .thenReturn(new WorkflowCreatedDTO("WFID"));

    Pair<DebtPositionDTO, String> result = service.checkAndUpdateInstallmentExpiration(debtPositionId, accessToken);

    assertEquals(InstallmentStatus.INVALID, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertEquals("WFID", result.getRight());
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenDueDateIsBeforeNowThenOk() {
    Long debtPositionId = 1L;
    String accessToken = "ACCESSTOKEN";
    LocalDate dueDate = LocalDate.of(2025, 1, 1);
    DebtPosition debtPosition = buildDebtPosition();
    InstallmentNoPII expiredInstallment = debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst();
    expiredInstallment.setStatus(InstallmentStatus.UNPAID);
    expiredInstallment.setDueDate(dueDate);

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.EXPIRED);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.eq(PaymentEventType.DPI_EXPIRED), Mockito.eq("IUD:"+expiredInstallment.getIud()), Mockito.same(accessToken)))
      .thenReturn(new WorkflowCreatedDTO("WFID"));

    Pair<DebtPositionDTO, String> result = service.checkAndUpdateInstallmentExpiration(debtPositionId, accessToken);

    assertEquals(InstallmentStatus.EXPIRED, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertEquals("WFID", result.getRight());

    verify(installmentNoPIIRepositoryMock).updateStatus(expiredInstallment.getInstallmentId(), InstallmentStatus.EXPIRED);
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenDueDateIsAfterNowThenOk() {
    Long debtPositionId = 1L;
    String accessToken = "ACCESSTOKEN";
    LocalDate dueDate = LocalDate.now().plusDays(2);
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.UNPAID);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(dueDate);

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.UNPAID);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.isNull(), Mockito.isNull(), Mockito.same(accessToken)))
      .thenReturn(new WorkflowCreatedDTO("WFID"));

    Pair<DebtPositionDTO, String> result = service.checkAndUpdateInstallmentExpiration(debtPositionId, accessToken);

    assertEquals(InstallmentStatus.UNPAID, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertEquals("WFID", result.getRight());
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenDueDateIsNullNowThenOk() {
    Long debtPositionId = 1L;
    String accessToken = "ACCESSTOKEN";
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.UNPAID);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(null);

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.UNPAID);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.isNull(), Mockito.isNull(), Mockito.same(accessToken)))
      .thenReturn(new WorkflowCreatedDTO("WFID"));

    Pair<DebtPositionDTO, String> result = service.checkAndUpdateInstallmentExpiration(debtPositionId, accessToken);

    assertEquals(InstallmentStatus.UNPAID, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertEquals("WFID", result.getRight());
  }

  @Test
  void whenAlignHierarchyStatusAndRemapThenReturnRemap(){
    // Given
    DebtPosition debtPosition = new DebtPosition();
    DebtPositionDTO expectedResult = new DebtPositionDTO();

    Mockito.when(debtPositionMapperMock.mapToDto(Mockito.same(debtPosition)))
      .thenReturn(expectedResult);

    Mockito.doNothing()
      .when(service)
      .alignHierarchyStatus(Mockito.same(debtPosition));

    // When
    DebtPositionDTO result = service.alignHierarchyStatusAndRemap(debtPosition);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
