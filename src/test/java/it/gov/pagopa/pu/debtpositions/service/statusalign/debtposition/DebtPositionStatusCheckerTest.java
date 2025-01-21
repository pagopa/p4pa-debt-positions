package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DebtPositionStatusCheckerTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;

  private DebtPositionStatusChecker checker;

  @BeforeEach
  void setUp() {
    checker = new DebtPositionStatusChecker(debtPositionRepositoryMock);
  }

  /**
   * Test if the status is TO_SYNC when at least one PaymentOption has status TO_SYNC.
   */
  @Test
  void testCalculateNewStatus_ToSync() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.TO_SYNC, PaymentOptionStatus.PAID);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.TO_SYNC, result);
  }

  /**
   * Test if the status is PARTIALLY_PAID when there is at least one PAID and one UNPAID paymentOption.
   */
  @Test
  void testCalculateNewStatus_PartiallyPaid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.UNPAID);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PARTIALLY_PAID, result);
  }

  /**
   * Test if the status is PARTIALLY_PAID when there is at least one PAID and one EXPIRED paymentOption.
   */
  @Test
  void testDeterminePaymentOptionStatus_PartiallyPaid2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.EXPIRED);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PARTIALLY_PAID, result);
  }

  /**
   * Test if the status is PARTIALLY_PAID when there is at least one PARTIALLY_PAID paymentOption.
   */
  @Test
  void testDeterminePaymentOptionStatus_PartiallyPaid3() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.EXPIRED, PaymentOptionStatus.PARTIALLY_PAID);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PARTIALLY_PAID, result);
  }

  /**
   * Test if the status is UNPAID when all paymentOptions are UNPAID.
   */
  @Test
  void testCalculateNewStatus_Unpaid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.UNPAID);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.UNPAID, result);
  }

  /**
   * Test if the status is UNPAID when all paymentOptions are CANCELLED, with at least one UNPAID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Unpaid2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.CANCELLED, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.UNPAID, result);
  }

  /**
   * Test if the status is PAID when all paymentOptions are PAID.
   */
  @Test
  void testCalculateNewStatus_Paid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.PAID);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PAID, result);
  }

  /**
   * Test if the status is PAID when all paymentOptions are CANCELLED, with at least one PAID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Paid2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PAID, result);
  }

  /**
   * Test if the status is REPORTED when all paymentOptions are CANCELLED, with at least one REPORTED.
   */
  @Test
  void testCalculateNewStatus_Reported() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.REPORTED, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.REPORTED, result);
  }

  /**
   * Test if the status is REPORTED when all paymentOptions are REPORTED.
   */
  @Test
  void testDeterminePaymentOptionStatus_Reported2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.REPORTED, PaymentOptionStatus.REPORTED);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.REPORTED, result);
  }

  /**
   * Test if the status is INVALID when all paymentOptions are INVALID.
   */
  @Test
  void testCalculateNewStatus_Invalid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.INVALID, PaymentOptionStatus.INVALID);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.INVALID, result);
  }

  /**
   * Test if the status is INVALID when all paymentOptions are CANCELLED, with at least one INVALID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Invalid2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.INVALID, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.INVALID, result);
  }

  /**
   * Test if the status is CANCELLED when all paymentOptions are CANCELLED.
   */
  @Test
  void testCalculateNewStatus_Cancelled() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.CANCELLED, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.CANCELLED, result);
  }

  /**
   * Test if the status is EXPIRED when all paymentOptions are EXPIRED.
   */
  @Test
  void testCalculateNewStatus_Expired() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.EXPIRED, PaymentOptionStatus.EXPIRED);
    DebtPositionStatus result = checker.calculateNewStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.EXPIRED, result);
  }

  /**
   * Test if an exception is thrown when the list of paymentOptions is empty.
   */
  @Test
  void testCalculateNewStatus_InvalidStatus() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of();
    Exception exception = assertThrows(InvalidStatusException.class, () -> checker.calculateNewStatus(paymentOptionStatusList));
    assertEquals("Unable to determine status for DebtPosition", exception.getMessage());
  }

  @Test
  void testGetChildStatuses() {
    DebtPosition debtPosition = new DebtPosition();
    PaymentOption option1 = new PaymentOption();
    option1.setPaymentOptionId(1L);
    option1.setStatus(PaymentOptionStatus.PAID);

    PaymentOption option2 = new PaymentOption();
    option2.setPaymentOptionId(2L);
    option2.setStatus(PaymentOptionStatus.UNPAID);

    debtPosition.setPaymentOptions(new TreeSet<>(List.of(option1, option2)));

    List<PaymentOptionStatus> statuses = checker.getChildStatuses(debtPosition);

    assertEquals(2, statuses.size());
    assertEquals(PaymentOptionStatus.PAID, statuses.get(0));
    assertEquals(PaymentOptionStatus.UNPAID, statuses.get(1));
  }

  @Test
  void testSetStatus() {
    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionStatus newStatus = DebtPositionStatus.PAID;

    checker.setStatus(debtPosition, newStatus);

    assertEquals(newStatus, debtPosition.getStatus());
  }

  @Test
  void testStoreStatus() {
    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionStatus newStatus = DebtPositionStatus.PAID;

    Mockito.doNothing().when(debtPositionRepositoryMock).updateStatus(1L, newStatus);

    checker.storeStatus(debtPosition, newStatus);

    Mockito.verify(debtPositionRepositoryMock).updateStatus(1L, newStatus);
  }
}

