package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.BaseInstallment;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusTransitionException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition.DebtPositionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption.PaymentOptionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker;
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
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

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
        syncServiceMock,
        debtPositionTypeOrgRepositoryMock)
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
    DebtPosition debtPosition = buildTestDebtPosition();
    BaseInstallment installmentSyncError = debtPosition.getPaymentOptions().getFirst().getInstallments().getLast();

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatus(100L, newStatus, null);
    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatus(101L, installmentSyncError.getStatus(), new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID, "SYNCERROR"));
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    installmentDTO.setStatus(InstallmentStatus.UNPAID);
    installmentDTO.syncStatus(null);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);

    SyncStatusUpdateRequestDTO requestDTO = buildSyncStatusUpdateRequest("iud", "iud2", newStatus);

    DebtPositionDTO result = service.finalizeSyncStatus(debtPositionId, requestDTO);

    assertEquals(DebtPositionStatus.UNPAID, result.getStatus());
    assertEquals(PaymentOptionStatus.UNPAID, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.UNPAID, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertNull(result.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    reflectionEqualsByName(debtPositionDTO, result);
  }

  private DebtPosition buildTestDebtPosition() {
    DebtPosition debtPosition = DebtPositionFaker.buildDebtPosition();
    InstallmentNoPII installmentSyncError = InstallmentFaker.buildInstallmentNoPII();
    installmentSyncError.setInstallmentId(101L);
    installmentSyncError.setIud("iud2");
    debtPosition.getPaymentOptions().getFirst().getInstallments().add(installmentSyncError);
    return debtPosition;
  }

  private SyncStatusUpdateRequestDTO buildSyncStatusUpdateRequest(String iud2finalize, String iud2error, InstallmentStatus newStatus) {
    Map<String, SyncCompleteDTO> iupd2finalize = Map.of(iud2finalize, SyncCompleteDTO.builder()
      .newStatus(newStatus)
      .build());
    Map<String, SyncErrorDTO> iupdSyncError = Map.of(iud2error, new SyncErrorDTO("SYNCERROR"));

    return new SyncStatusUpdateRequestDTO(iupd2finalize, iupdSyncError);
  }

  @Test
  void givenFinalizeSyncStatusWhenIsNotSyncThenDoNotUpdateStatus() {
    Long debtPositionId = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    DebtPosition debtPosition = buildTestDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getLast().setStatus(InstallmentStatus.PAID);

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(debtPosition);

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    Mockito.doReturn(expectedResult)
      .when(service).alignHierarchyStatusAndRemap(Mockito.same(debtPosition));

    SyncStatusUpdateRequestDTO requestDTO = buildSyncStatusUpdateRequest("iud", "iud2", newStatus);

    DebtPositionDTO result = service.finalizeSyncStatus(debtPositionId, requestDTO);

    assertSame(expectedResult, result);
  }

  @Test
  void givenFinalizeSyncStatusWhenDoesNotHaveIudThenDoNotUpdateStatus() {
    Long debtPositionId = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    DebtPosition debtPosition = buildTestDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(debtPosition);

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    Mockito.doReturn(expectedResult)
      .when(service).alignHierarchyStatusAndRemap(Mockito.same(debtPosition));

    SyncStatusUpdateRequestDTO requestDTO = buildSyncStatusUpdateRequest("DUMMY", "DUMMY2", newStatus);

    DebtPositionDTO result = service.finalizeSyncStatus(debtPositionId, requestDTO);

    assertSame(expectedResult, result);
  }

  @Test
  void givenFinalizeSyncStatusWhenDebtPositionNotFoundThenThrowsException() {
    Long debtPositionId = 1L;
    SyncStatusUpdateRequestDTO request = new SyncStatusUpdateRequestDTO();

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> service.finalizeSyncStatus(debtPositionId, request),
      "Debt position related to the id 1 does not found");
  }

  @Test
  void givenNotifyReportedTransferIdWhenDebtPositionNotFoundThenThrowsException() {
    Long transferId = 1000L;
    String accessToken = "ACCESSTOKEN";
    TransferReportedRequest request = TransferReportedRequest.builder()
      .iuf("IUF")
      .build();

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByTransferId(transferId)).thenReturn(null);

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

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByTransferId(transferId)).thenReturn(debtPosition);

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

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByTransferId(transferId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);

    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = service.notifyReportedTransferId(transferId, request, accessToken);

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
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("WFID", "RUNID");

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByTransferId(transferId)).thenReturn(debtPosition);
    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatusAndIuf(100L, InstallmentStatus.REPORTED, request.getIuf());
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.eq(PaymentEventType.DPI_REPORTED), Mockito.eq("IUD:"+reportedInstallment.getIud()), Mockito.same(accessToken)))
      .thenReturn(workflow);

    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = service.notifyReportedTransferId(transferId, request, accessToken);

    assertEquals(DebtPositionStatus.REPORTED, result.getLeft().getStatus());
    assertEquals(PaymentOptionStatus.REPORTED, result.getLeft().getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.REPORTED, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertEquals(request.getIuf(), result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getIuf());
    reflectionEqualsByName(debtPositionDTOexpected, result);
    assertSame(workflow, result.getRight());

    assertEquals(InstallmentStatus.REPORTED, reportedInstallment.getStatus());
    assertEquals(request.getIuf(), reportedInstallment.getIuf());
  }

  @Test
  void givenCheckAndUpdateInstallmentExpirationWhenDebtPositionNotFoundThenThrowsException() {
    Long debtPositionId = 1L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(null);

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
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("WFID", "RUNID");

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.isNull(), Mockito.isNull(), Mockito.same(accessToken)))
      .thenReturn(workflow);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(DebtPositionTypeOrg.builder().debtPositionTypeOrgId(debtPosition.getDebtPositionTypeOrgId()).build()));

    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = service.checkAndUpdateInstallmentExpiration(debtPositionId, accessToken);

    assertEquals(InstallmentStatus.INVALID, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertSame(workflow, result.getRight());
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
    expiredInstallment.setSwitchToExpired(true);

    DebtPositionDTO debtPositionDTOexpected = buildDebtPositionDTO();
    debtPositionDTOexpected.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.EXPIRED);
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("WFID", "RUNID");

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.eq(PaymentEventType.DPI_EXPIRED), Mockito.eq("IUD:"+expiredInstallment.getIud()), Mockito.same(accessToken)))
      .thenReturn(workflow);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(DebtPositionTypeOrg.builder().debtPositionTypeOrgId(debtPosition.getDebtPositionTypeOrgId()).build()));

    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = service.checkAndUpdateInstallmentExpiration(debtPositionId, accessToken);

    assertEquals(InstallmentStatus.EXPIRED, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertSame(workflow, result.getRight());

    verify(installmentNoPIIRepositoryMock).updateStatus(expiredInstallment.getInstallmentId(), InstallmentStatus.EXPIRED, null);
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
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("WFID", "RUNID");

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.isNull(), Mockito.isNull(), Mockito.same(accessToken)))
      .thenReturn(workflow);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(DebtPositionTypeOrg.builder().debtPositionTypeOrgId(debtPosition.getDebtPositionTypeOrgId()).build()));

    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = service.checkAndUpdateInstallmentExpiration(debtPositionId, accessToken);

    assertEquals(InstallmentStatus.UNPAID, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertSame(workflow, result.getRight());
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
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("WFID", "RUNID");

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTOexpected);
    Mockito.when(syncServiceMock.syncDebtPosition(Mockito.same(debtPositionDTOexpected), Mockito.eq(new WfExecutionParameters()),
        Mockito.isNull(), Mockito.isNull(), Mockito.same(accessToken)))
      .thenReturn(workflow);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(DebtPositionTypeOrg.builder().debtPositionTypeOrgId(debtPosition.getDebtPositionTypeOrgId()).build()));

    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = service.checkAndUpdateInstallmentExpiration(debtPositionId, accessToken);

    assertEquals(InstallmentStatus.UNPAID, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertSame(workflow, result.getRight());
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
