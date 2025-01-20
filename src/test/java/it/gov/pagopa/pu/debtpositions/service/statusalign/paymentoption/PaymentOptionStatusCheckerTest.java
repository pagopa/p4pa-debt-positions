package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentOptionStatusCheckerTest {

  private PaymentOptionStatusChecker checker;

  @BeforeEach
  void setUp() {
    checker = new PaymentOptionStatusChecker();
  }

  /**
   * Test if the status is TO_SYNC when at least one Installment has status TO_SYNC.
   */
  @Test
  void testDeterminePaymentOptionStatus_ToSync() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.TO_SYNC, InstallmentStatus.PAID);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.TO_SYNC, result);
  }

  /**
   * Test if the status is PARTIALLY_PAID when there is at least one PAID and one UNPAID installment.
   */
  @Test
  void testDeterminePaymentOptionStatus_PartiallyPaid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.UNPAID);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PARTIALLY_PAID, result);
  }

  /**
   * Test if the status is PARTIALLY_PAID when there is at least one PAID and one EXPIRED installment.
   */
  @Test
  void testDeterminePaymentOptionStatus_PartiallyPaid2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.EXPIRED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PARTIALLY_PAID, result);
  }

  /**
   * Test if the status is UNPAID when all installments are UNPAID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Unpaid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.UNPAID, InstallmentStatus.UNPAID);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.UNPAID, result);
  }

  /**
   * Test if the status is UNPAID when all installments are CANCELLED, with at least one UNPAID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Unpaid2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.UNPAID, InstallmentStatus.CANCELLED, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.UNPAID, result);
  }

  /**
   * Test if the status is PAID when all installments are PAID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Paid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.PAID);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PAID, result);
  }

  /**
   * Test if the status is PAID when all installments are CANCELLED, with at least one PAID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Paid2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PAID, result);
  }

  /**
   * Test if the status is REPORTED when all installments are CANCELLED, with at least one REPORTED.
   */
  @Test
  void testDeterminePaymentOptionStatus_Reported() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.REPORTED, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.REPORTED, result);
  }

  /**
   * Test if the status is REPORTED when all installments are REPORTED.
   */
  @Test
  void testDeterminePaymentOptionStatus_Reported2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.REPORTED, InstallmentStatus.REPORTED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.REPORTED, result);
  }

  /**
   * Test if the status is INVALID when all installments are INVALID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Invalid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.INVALID, InstallmentStatus.INVALID);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.INVALID, result);
  }

  /**
   * Test if the status is INVALID when all installments are CANCELLED, with at least one INVALID.
   */
  @Test
  void testDeterminePaymentOptionStatus_Invalid2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.INVALID, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.INVALID, result);
  }

  /**
   * Test if the status is CANCELLED when all installments are CANCELLED.
   */
  @Test
  void testDeterminePaymentOptionStatus_Cancelled() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.CANCELLED, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.CANCELLED, result);
  }

  /**
   * Test if the status is EXPIRED when all installments are EXPIRED.
   */
  @Test
  void testDeterminePaymentOptionStatus_Expired() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.EXPIRED, InstallmentStatus.EXPIRED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.EXPIRED, result);
  }

  /**
   * Test if an exception is thrown when the list of installments is empty.
   */
  @Test
  void testDeterminePaymentOptionStatus_InvalidStatus() {
    List<InstallmentStatus> installmentStatusList = List.of();
    Exception exception = assertThrows(InvalidStatusException.class, () -> checker.determinePaymentOptionStatus(installmentStatusList));
    assertEquals("Unable to determine status for PaymentOption", exception.getMessage());
  }
}
