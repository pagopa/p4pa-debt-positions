package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("squid:S117") // suppressing naming convention warning for variable written with "_" name
@ExtendWith(MockitoExtension.class)
class OrdinaryPaidDPHierarchyUpdateServiceTest {


  @Mock
  private DebtPositionProcessorService debtPositionProcessorServiceMock;
  @Mock
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerServiceMock;

  private OrdinaryPaidDPHierarchyUpdateService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init(){
    service = new OrdinaryPaidDPHierarchyUpdateService(
      debtPositionProcessorServiceMock,
      debtPositionHierarchyStatusAlignerServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      debtPositionProcessorServiceMock,
      debtPositionHierarchyStatusAlignerServiceMock);
  }

  @Test
  void whenThenOk(){
    // Given
    PaymentOption po = podamFactory.manufacturePojo(PaymentOption.class);

    InstallmentNoPII paidInstallment = buildFakeInstallment(po.getPaymentOptionId());
    paidInstallment.setInstallmentId(-1L);
    paidInstallment.setStatus(InstallmentStatus.PAID);

    InstallmentNoPII fetchedInstallment = buildFakeInstallment(po.getPaymentOptionId());
    fetchedInstallment.setInstallmentId(paidInstallment.getInstallmentId());
    InstallmentNoPII po_i1 = buildFakeInstallment(po.getPaymentOptionId());
    po_i1.setStatus(InstallmentStatus.CANCELLED);
    InstallmentNoPII po_i2 = buildFakeInstallment(po.getPaymentOptionId());
    po.setInstallments(new TreeSet<>(Set.of(po_i1, po_i2, fetchedInstallment)));

    PaymentOption po1 = podamFactory.manufacturePojo(PaymentOption.class);
    InstallmentNoPII po1_i1 = buildFakeInstallment(po1.getPaymentOptionId());
    po1_i1.setStatus(InstallmentStatus.CANCELLED);
    InstallmentNoPII po1_i2 = buildFakeInstallment(po1.getPaymentOptionId());
    po1.setInstallments(new TreeSet<>(Set.of(po1_i1, po1_i2)));

    PaymentOption po2 = podamFactory.manufacturePojo(PaymentOption.class);
    po2.setInstallments(new TreeSet<>(Set.of(
      buildFakeInstallment(po2.getPaymentOptionId()),
      buildFakeInstallment(po2.getPaymentOptionId())
    )));

    DebtPosition dp = new DebtPosition();
    dp.setPaymentOptions(new TreeSet<>(Set.of(po1, po2, po)));

    // When
    service.updateHierarchy(dp, paidInstallment);

    // Then
    assertInvolvedPO(po, po_i1, po_i2, paidInstallment);
    assertInvalidatedPO1(po1, po1_i1, po1_i2);
    assertInvalidatedPO2(po2);

    Mockito.verify(debtPositionProcessorServiceMock)
      .updateAmounts(Mockito.same(dp));
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock)
      .alignHierarchyStatus(Mockito.same(dp));
  }

  private InstallmentNoPII buildFakeInstallment(Long poId) {
    InstallmentNoPII i = podamFactory.manufacturePojo(InstallmentNoPII.class);
    i.setPaymentOptionId(poId);
    i.setStatus(InstallmentStatus.UNPAID);
    i.setSyncStatus(null);
    return i;
  }

  private static void assertInvolvedPO(PaymentOption po, InstallmentNoPII po_i1, InstallmentNoPII po_i2, InstallmentNoPII paidInstallment) {
    Assertions.assertEquals(
      new TreeSet<>(Set.of(po_i1, po_i2, paidInstallment)),
      po.getInstallments());
    Assertions.assertEquals(InstallmentStatus.CANCELLED, po_i1.getStatus());
    Assertions.assertEquals(InstallmentStatus.UNPAID, po_i2.getStatus());
  }

  private void assertInvalidatedPO1(PaymentOption po1, InstallmentNoPII po1_i1, InstallmentNoPII po1_i2) {
    Assertions.assertEquals(
      new TreeSet<>(Set.of(po1_i1, po1_i2)),
      po1.getInstallments());
    Assertions.assertEquals(InstallmentStatus.CANCELLED, po1_i1.getStatus());
    assertInvalidatedStatus(po1_i2);
  }

  private void assertInvalidatedStatus(InstallmentNoPII i) {
    Assertions.assertEquals(InstallmentStatus.TO_SYNC, i.getStatus());
    Assertions.assertEquals(new InstallmentSyncStatus(InstallmentStatus.UNPAID, InstallmentStatus.INVALID), i.getSyncStatus());
  }

  private void assertInvalidatedPO2(PaymentOption po2) {
    po2.getInstallments().forEach(this::assertInvalidatedStatus);
  }
}
