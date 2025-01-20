package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DebtPositionStatusCheckerTest {

  private DebtPositionStatusChecker checker;

  @BeforeEach
  void setUp() {
    checker = new DebtPositionStatusChecker();
  }

  /**
   * Test if the status is TO_SYNC when at least one PaymentOption has status TO_SYNC.
   */
  @Test
  void testDetermineDebtPositionStatus_ToSync() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.TO_SYNC, PaymentOptionStatus.PAID);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.TO_SYNC, result);
  }

  /**
   * Test if the status is PARTIALLY_PAID when there is at least one PAID and one UNPAID paymentOption.
   */
  @Test
  void testDetermineDebtPositionStatus_PartiallyPaid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.UNPAID);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PARTIALLY_PAID, result);
  }

  /**
   * Test if the status is PARTIALLY_PAID when there is at least one PAID and one EXPIRED paymentOption.
   */
  @Test
  void testDeterminePaymentOptionStatus_PartiallyPaid2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.EXPIRED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PARTIALLY_PAID, result);
  }

  /**
   * Test if the status is UNPAID when all paymentOptions are UNPAID.
   */
  @Test
  void testDetermineDebtPositionStatus_Unpaid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.UNPAID);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.UNPAID, result);
  }

  /**
   * Test if the status is UNPAID when all paymentOptions are CANCELLED, with at least one UNPAID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Unpaid2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.CANCELLED, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.UNPAID, result);
  }

  /**
   * Test if the status is PAID when all paymentOptions are PAID.
   */
  @Test
  void testDetermineDebtPositionStatus_Paid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.PAID);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PAID, result);
  }

  /**
   * Test if the status is PAID when all paymentOptions are CANCELLED, with at least one PAID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Paid2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PAID, result);
  }

  /**
   * Test if the status is REPORTED when all paymentOptions are CANCELLED, with at least one REPORTED.
   */
  @Test
  void testDetermineDebtPositionStatus_Reported() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.REPORTED, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.REPORTED, result);
  }

  /**
   * Test if the status is REPORTED when all paymentOptions are REPORTED.
   */
  @Test
  void testDeterminePaymentOptionStatus_Reported2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.REPORTED, PaymentOptionStatus.REPORTED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.REPORTED, result);
  }

  /**
   * Test if the status is INVALID when all paymentOptions are INVALID.
   */
  @Test
  void testDetermineDebtPositionStatus_Invalid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.INVALID, PaymentOptionStatus.INVALID);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.INVALID, result);
  }

  /**
   * Test if the status is INVALID when all paymentOptions are CANCELLED, with at least one INVALID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Invalid2() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.INVALID, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.INVALID, result);
  }

  /**
   * Test if the status is CANCELLED when all paymentOptions are CANCELLED.
   */
  @Test
  void testDetermineDebtPositionStatus_Cancelled() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.CANCELLED, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.CANCELLED, result);
  }

  /**
   * Test if the status is EXPIRED when all paymentOptions are EXPIRED.
   */
  @Test
  void testDetermineDebtPositionStatus_Expired() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.EXPIRED, PaymentOptionStatus.EXPIRED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.EXPIRED, result);
  }

  /**
   * Test if an exception is thrown when the list of paymentOptions is empty.
   */
  @Test
  void testDetermineDebtPositionStatus_InvalidStatus() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of();
    Exception exception = assertThrows(InvalidStatusException.class, () -> checker.determineDebtPositionStatus(paymentOptionStatusList));
    assertEquals("Unable to determine status for DebtPosition", exception.getMessage());
  }
}

