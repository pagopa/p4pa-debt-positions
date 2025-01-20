package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusException;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PaymentOptionStatusCheckerTest {

  @Mock
  private PaymentOptionRepository paymentOptionRepositoryMock;

  private PaymentOptionStatusChecker checker;

  @BeforeEach
  void setUp() {
    checker = new PaymentOptionStatusChecker(paymentOptionRepositoryMock);
  }

  /**
   * Test if the status is TO_SYNC when at least one Installment has status TO_SYNC.
   */
  @Test
  void testCalculateNewStatus_ToSync() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.TO_SYNC, InstallmentStatus.PAID);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.TO_SYNC, result);
  }

  /**
   * Test if the status is PARTIALLY_PAID when there is at least one PAID and one UNPAID installment.
   */
  @Test
  void testCalculateNewStatus_PartiallyPaid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.UNPAID);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PARTIALLY_PAID, result);
  }

  /**
   * Test if the status is PARTIALLY_PAID when there is at least one PAID and one EXPIRED installment.
   */
  @Test
  void testCalculateNewStatus_PartiallyPaid2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.EXPIRED);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PARTIALLY_PAID, result);
  }

  /**
   * Test if the status is UNPAID when all installments are UNPAID.
   */
  @Test
  void testCalculateNewStatus_Unpaid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.UNPAID, InstallmentStatus.UNPAID);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.UNPAID, result);
  }

  /**
   * Test if the status is UNPAID when all installments are CANCELLED, with at least one UNPAID.
   */
  @Test
  void testCalculateNewStatus_Unpaid2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.UNPAID, InstallmentStatus.CANCELLED, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.UNPAID, result);
  }

  /**
   * Test if the status is PAID when all installments are PAID.
   */
  @Test
  void testCalculateNewStatus_Paid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.PAID);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PAID, result);
  }

  /**
   * Test if the status is PAID when all installments are CANCELLED, with at least one PAID.
   */
  @Test
  void testCalculateNewStatus_Paid2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.PAID, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.PAID, result);
  }

  /**
   * Test if the status is REPORTED when all installments are CANCELLED, with at least one REPORTED.
   */
  @Test
  void testCalculateNewStatus_Reported() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.REPORTED, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.REPORTED, result);
  }

  /**
   * Test if the status is REPORTED when all installments are REPORTED.
   */
  @Test
  void testCalculateNewStatus_Reported2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.REPORTED, InstallmentStatus.REPORTED);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.REPORTED, result);
  }

  /**
   * Test if the status is INVALID when all installments are INVALID.
   */
  @Test
  void testCalculateNewStatus_Invalid() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.INVALID, InstallmentStatus.INVALID);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.INVALID, result);
  }

  /**
   * Test if the status is INVALID when all installments are CANCELLED, with at least one INVALID.
   */
  @Test
  void testCalculateNewStatus_Invalid2() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.INVALID, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.INVALID, result);
  }

  /**
   * Test if the status is CANCELLED when all installments are CANCELLED.
   */
  @Test
  void testCalculateNewStatus_Cancelled() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.CANCELLED, InstallmentStatus.CANCELLED);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.CANCELLED, result);
  }

  /**
   * Test if the status is EXPIRED when all installments are EXPIRED.
   */
  @Test
  void testCalculateNewStatus_Expired() {
    List<InstallmentStatus> installmentStatusList = List.of(InstallmentStatus.EXPIRED, InstallmentStatus.EXPIRED);
    PaymentOptionStatus result = checker.calculateNewStatus(installmentStatusList);
    assertEquals(PaymentOptionStatus.EXPIRED, result);
  }

  /**
   * Test if an exception is thrown when the list of installments is empty.
   */
  @Test
  void testCalculateNewStatus_InvalidStatus() {
    List<InstallmentStatus> installmentStatusList = List.of();
    Exception exception = assertThrows(InvalidStatusException.class, () -> checker.calculateNewStatus(installmentStatusList));
    assertEquals("Unable to determine status for PaymentOption", exception.getMessage());
  }

  @Test
  void testGetChildStatuses() {
    PaymentOption paymentOption = new PaymentOption();
    InstallmentNoPII installmentNoPII1 = new InstallmentNoPII();
    installmentNoPII1.setInstallmentId(1L);
    installmentNoPII1.setStatus(InstallmentStatus.PAID);

    InstallmentNoPII installmentNoPII2 = new InstallmentNoPII();
    installmentNoPII2.setInstallmentId(2L);
    installmentNoPII2.setStatus(InstallmentStatus.UNPAID);

    paymentOption.setInstallments(new TreeSet<>(List.of(installmentNoPII1, installmentNoPII2)));

    List<InstallmentStatus> statuses = checker.getChildStatuses(paymentOption);

    assertEquals(2, statuses.size());
    assertEquals(InstallmentStatus.PAID, statuses.get(0));
    assertEquals(InstallmentStatus.UNPAID, statuses.get(1));
  }

  @Test
  void testSetStatus() {
    PaymentOption paymentOption = buildPaymentOption();
    PaymentOptionStatus newStatus = PaymentOptionStatus.PAID;

    checker.setStatus(paymentOption, newStatus);

    assertEquals(newStatus, paymentOption.getStatus());
  }

  @Test
  void testStoreStatus() {
    PaymentOption paymentOption = buildPaymentOption();
    PaymentOptionStatus newStatus = PaymentOptionStatus.PAID;

    Mockito.doNothing().when(paymentOptionRepositoryMock).updateStatus(1L, newStatus);

    checker.storeStatus(paymentOption, newStatus);

    Mockito.verify(paymentOptionRepositoryMock).updateStatus(1L, newStatus);
  }
}
