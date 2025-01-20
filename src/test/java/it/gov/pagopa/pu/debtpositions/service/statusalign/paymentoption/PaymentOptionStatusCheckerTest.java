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

  @Test
  void testDeterminePaymentOptionStatus_ToSync() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.TO_SYNC, InstallmentStatus.PAID);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.TO_SYNC, result);
  }

  @Test
  void testDeterminePaymentOptionStatus_PartiallyPaid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.UNPAID);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PARTIALLY_PAID, result);
  }

  @Test
  void testDeterminePaymentOptionStatus_Unpaid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.UNPAID, InstallmentStatus.UNPAID);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.UNPAID, result);
  }

  @Test
  void testDeterminePaymentOptionStatus_Paid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.PAID);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PAID, result);
  }

  @Test
  void testDeterminePaymentOptionStatus_Reported() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.REPORTED, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.REPORTED, result);
  }

  @Test
  void testDeterminePaymentOptionStatus_Invalid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.INVALID, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.INVALID, result);
  }

  @Test
  void testDeterminePaymentOptionStatus_Cancelled() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.CANCELLED, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.CANCELLED, result);
  }

  @Test
  void testDeterminePaymentOptionStatus_Expired() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.EXPIRED, InstallmentStatus.EXPIRED);
    PaymentOptionStatus result = checker.determinePaymentOptionStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.EXPIRED, result);
  }

  @Test
  void testDeterminePaymentOptionStatus_InvalidStatus() {
    List<InstallmentStatus> installmentStatusList = List.of();
    Exception exception = assertThrows(InvalidStatusException.class, () -> checker.determinePaymentOptionStatus(installmentStatusList));
    assertEquals("Unable to determine status for PaymentOption", exception.getMessage());
  }
}
