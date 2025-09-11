package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionDeleteService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.TechnicalMixedDebtPositionBuilderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicalMixedDebtPositionUpdaterServiceImplTest {

  @Mock
  private DebtPositionTypeOrgRepository dpTypeOrgRepositoryMock;
  @Mock
  private DebtPositionRepository dpRepositoryMock;
  @Mock
  private TechnicalMixedDebtPositionBuilderService mixedDPBuilderServiceMock;
  @Mock
  private DebtPositionDeleteService dpDeleteServiceMock;

  private TechnicalMixedDebtPositionUpdaterService service;

  @BeforeEach
  void setup() {
    service = new TechnicalMixedDebtPositionUpdaterServiceImpl(
      dpTypeOrgRepositoryMock,
      dpRepositoryMock,
      mixedDPBuilderServiceMock,
      dpDeleteServiceMock
    );
  }

  @AfterEach
  void afterEach() {
    verifyNoMoreInteractions(dpTypeOrgRepositoryMock, dpRepositoryMock, mixedDPBuilderServiceMock, dpDeleteServiceMock);
  }

  @Test
  void givenNotMixedDpTypeOrgWhenUpdateThenReturnEmptyList() {
    // Given
    DebtPosition debtPosition = generateDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setCode("NOTMIXED");

    when(dpTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));

    // When
    List<DebtPosition> result = service.update(debtPosition);

    // Then
    assertTrue(result.isEmpty());
    verify(dpTypeOrgRepositoryMock).findById(1L);
  }

  @Test
  void givenPaymentOptionsEmptyWhenUpdateThenThrowException() {
    // Given
    DebtPosition debtPosition = generateDebtPosition();
    debtPosition.setPaymentOptions(new TreeSet<>());
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setCode("MIXED");

    when(dpTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));

    // Then
    InvalidValueException exception = assertThrows(InvalidValueException.class, () -> service.update(debtPosition));
    verify(dpTypeOrgRepositoryMock).findById(1L);
    assertEquals("paymentOptions size must be 1 for debtPositionId=" +  debtPosition.getDebtPositionId(), exception.getMessage());
  }

  @Test
  void givenPaymentOptionsMoreThanOneWhenUpdateThenThrowException() {
    // Given
    DebtPosition debtPosition = generateDebtPosition();
    debtPosition.getPaymentOptions().add(new PaymentOption());
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setCode("MIXED");

    when(dpTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));

    // Then
    InvalidValueException exception = assertThrows(InvalidValueException.class, () -> service.update(debtPosition));
    verify(dpTypeOrgRepositoryMock).findById(1L);
    assertEquals("paymentOptions size must be 1 for debtPositionId=" +  debtPosition.getDebtPositionId(), exception.getMessage());
  }

  @Test
  void givenInstallmentsEmptyWhenUpdateThenThrowException() {
    // Given
    DebtPosition debtPosition = generateDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().clear();
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setCode("MIXED");

    when(dpTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));

    // Then
    InvalidValueException exception = assertThrows(InvalidValueException.class, () -> service.update(debtPosition));
    verify(dpTypeOrgRepositoryMock).findById(1L);
    assertEquals("installments size must be 1 for debtPositionId=" + debtPosition.getDebtPositionId(), exception.getMessage());
  }

  @Test
  void givenInstallmentsMoreThanOneWhenUpdateThenThrowException() {
    // Given
    DebtPosition debtPosition = generateDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().add(new InstallmentNoPII());
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setCode("MIXED");

    when(dpTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));

    // Then
    InvalidValueException exception = assertThrows(InvalidValueException.class, () -> service.update(debtPosition));
    verify(dpTypeOrgRepositoryMock).findById(1L);
    assertEquals("installments size must be 1 for debtPositionId=" + debtPosition.getDebtPositionId(), exception.getMessage());
  }

  @Test
  void givenValidMixedDebtPositionWhenUpdateThenUpdateSuccessfully() {
    // Given
    DebtPosition debtPosition = generateDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setCode("MIXED");
    InstallmentNoPII installment = debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst();
    DebtPosition oldMixedDebtPosition = generateDebtPosition();
    DebtPosition newMixedDebtPosition = generateDebtPosition();
    ArgumentCaptor<DebtPosition> dpCaptor =
ArgumentCaptor.forClass(DebtPosition.class);

    when(dpTypeOrgRepositoryMock.findById(debtPosition.getDebtPositionTypeOrgId())).thenReturn(Optional.of(debtPositionTypeOrg));
    when(dpRepositoryMock.findEntityGraphByOrganizationIdAndIuvAndDebtPositionOrigin(
      debtPosition.getOrganizationId(),
      installment.getIuv(),
      DebtPositionOrigin.SPONTANEOUS_MIXED
    )).thenReturn(List.of(oldMixedDebtPosition));
    when(mixedDPBuilderServiceMock.createTechnicalMixedDebtPositions(anyMap(), eq(debtPosition))).thenReturn(List.of(newMixedDebtPosition));

    // When
    List<DebtPosition> result = service.update(debtPosition);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(dpTypeOrgRepositoryMock).findById(1L);
    verify(dpRepositoryMock).findEntityGraphByOrganizationIdAndIuvAndDebtPositionOrigin(
      debtPosition.getOrganizationId(), installment.getIuv(), DebtPositionOrigin.SPONTANEOUS_MIXED);
    verify(mixedDPBuilderServiceMock).createTechnicalMixedDebtPositions(anyMap(), eq(debtPosition));
    verify(dpDeleteServiceMock).delete(dpCaptor.capture());
    assertEquals(newMixedDebtPosition.getDebtPositionId(), result.getFirst().getDebtPositionId());
    assertEquals(oldMixedDebtPosition.getDebtPositionId(), dpCaptor.getValue().getDebtPositionId());
    assertNull(dpCaptor.getValue().getPaymentOptions().getFirst().getInstallments().getFirst().getPersonalDataId());
  }

  @Test
  void whenDebtPositionTypeNotFound_thenThrowException() {
    DebtPosition debtPosition = generateDebtPosition();
    when(dpTypeOrgRepositoryMock.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.update(debtPosition));
    verify(dpTypeOrgRepositoryMock).findById(1L);
  }

  private DebtPosition generateDebtPosition() {
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(ThreadLocalRandom.current().nextLong());
    debtPosition.setDebtPositionTypeOrgId(1L);
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setInstallmentId(1L);
    installment.setPaymentOptionId(1L);
    installment.setStatus(InstallmentStatus.UNPAID);
    installment.setSyncStatus(new InstallmentSyncStatus());
    installment.setIupdPagopa(UUID.randomUUID().toString());
    installment.setGenerateNotice(false);
    installment.setIud(UUID.randomUUID().toString());
    installment.setIuv(UUID.randomUUID().toString());
    installment.setIur(UUID.randomUUID().toString());
    installment.setIuf(UUID.randomUUID().toString());
    installment.setNav(UUID.randomUUID().toString());
    installment.setIun(UUID.randomUUID().toString());
    installment.setDueDate(LocalDate.now());
    installment.setSwitchToExpired(false);
    installment.setNotificationFeeCents(0L);
    installment.setAmountCents(0L);
    installment.setRemittanceInformation("");
    installment.setBalance("100 EUR");
    installment.setLegacyPaymentMetadata("");
    installment.setPersonalDataId(ThreadLocalRandom.current().nextLong());
    installment.setDebtorEntityType(PersonEntityType.F);
    installment.setDebtorFiscalCodeHash(new byte[0]);
    installment.setNotificationDate(OffsetDateTime.now());
    installment.setIngestionFlowFileId(0L);
    installment.setIngestionFlowFileLineNumber(0L);
    installment.setIngestionFlowFileAction(Action.I);
    installment.setSourceFlowName("");
    installment.setReceiptId(0L);
    installment.setTransfers(new TreeSet<>(List.of(Transfer.builder().transferId(1L).transferIndex(0).build())));
    installment.setCreationDate(LocalDateTime.now());
    installment.setUpdateDate(LocalDateTime.now());
    installment.setUpdateOperatorExternalId("");
    installment.setUpdateTraceId("");
    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(1L);
    paymentOption.setDebtPositionId(0L);
    paymentOption.setTotalAmountCents(0L);
    paymentOption.setStatus(PaymentOptionStatus.UNPAID);
    paymentOption.setDescription("");
    paymentOption.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    paymentOption.setPaymentOptionIndex(0);
    paymentOption.setInstallments(new TreeSet<>(List.of(installment)));
    paymentOption.setCreationDate(LocalDateTime.now());
    paymentOption.setUpdateDate(LocalDateTime.now());
    paymentOption.setUpdateOperatorExternalId("");
    paymentOption.setUpdateTraceId("");
    debtPosition.setPaymentOptions(new TreeSet<>(List.of(paymentOption)));
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    return debtPosition;
  }


}
