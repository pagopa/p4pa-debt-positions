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

  @Test
  void testDetermineDebtPositionStatus_ToSync() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.TO_SYNC, PaymentOptionStatus.PAID);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.TO_SYNC, result);
  }

  @Test
  void testDetermineDebtPositionStatus_PartiallyPaid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.UNPAID);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PARTIALLY_PAID, result);
  }

  @Test
  void testDetermineDebtPositionStatus_Unpaid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.UNPAID);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.UNPAID, result);
  }

  @Test
  void testDetermineDebtPositionStatus_Paid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.PAID, PaymentOptionStatus.PAID);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.PAID, result);
  }

  @Test
  void testDetermineDebtPositionStatus_Reported() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.REPORTED, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.REPORTED, result);
  }

  @Test
  void testDetermineDebtPositionStatus_Invalid() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.INVALID, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.INVALID, result);
  }

  @Test
  void testDetermineDebtPositionStatus_Cancelled() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.CANCELLED, PaymentOptionStatus.CANCELLED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.CANCELLED, result);
  }

  @Test
  void testDetermineDebtPositionStatus_Expired() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of(PaymentOptionStatus.EXPIRED, PaymentOptionStatus.EXPIRED);
    DebtPositionStatus result = checker.determineDebtPositionStatus(paymentOptionStatusList);
    assertEquals(DebtPositionStatus.EXPIRED, result);
  }

  @Test
  void testDetermineDebtPositionStatus_InvalidStatus() {
    List<PaymentOptionStatus> paymentOptionStatusList = List.of();
    Exception exception = assertThrows(InvalidStatusException.class, () -> checker.determineDebtPositionStatus(paymentOptionStatusList));
    assertEquals("Unable to determine status for DebtPosition", exception.getMessage());
  }
}

