package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.BaseInstallment;
import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DebtorDebtPositionMapperTest {

  private final DebtorDebtPositionMapper mapper =  Mappers.getMapper(DebtorDebtPositionMapper.class);

  private final PodamFactory podam = TestUtils.getPodamFactory();

  @Test
  void givenDebtPositionWithPaymentOptionsWhenMapThenPaymentOptionsAreMapped() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);
    byte[] hashedDebtorFiscalCode = {1, 2, 3};
    PaymentOption po1 = new PaymentOption();
    po1.setPaymentOptionId(1L);
    InstallmentNoPII i1 = new InstallmentNoPII();
    i1.setInstallmentId(1L);
    i1.setStatus(InstallmentUtils.UNPAID_OR_PAID_INSTALLMENT_STATUSES.iterator().next());
    i1.setDueDate(LocalDate.of(2025, 1, 1));
    i1.setDebtorFiscalCodeHash(hashedDebtorFiscalCode);

    InstallmentNoPII i2 = new InstallmentNoPII();
    i2.setInstallmentId(2L);
    i2.setStatus(InstallmentStatus.INVALID);
    i2.setDueDate(LocalDate.of(2025, 2, 1));

    po1.setInstallments(new TreeSet<>(Set.of(i1, i2)));

    PaymentOption po2 = new PaymentOption();
    po2.setPaymentOptionId(2L);
    InstallmentNoPII i3 = new InstallmentNoPII();
    i3.setInstallmentId(3L);
    i3.setStatus(InstallmentStatus.INVALID);
    i3.setDueDate(LocalDate.of(2025, 3, 1));
    po2.setInstallments(new TreeSet<>(Set.of(i3)));

    SortedSet<PaymentOption> options = new TreeSet<>(Comparator.comparing(PaymentOption::getPaymentOptionId));
    options.add(po1);
    options.add(po2);

    dp.setPaymentOptions(options);

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setDescription("TYPE_ORG_DESC");

    // when
    DebtorDebtPositionDTO result = mapper.map(dp, typeOrg, hashedDebtorFiscalCode);

    // then
    assertNotNull(result.getPaymentOptions());

    assertEquals(1, result.getPaymentOptions().size());

    List<? extends BaseInstallment> sortedInstallments = result.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .stream()
      .toList();

    assertEquals(1, sortedInstallments.size());
    assertEquals(i1.getInstallmentId(), sortedInstallments.getFirst().getInstallmentId());

    TestUtils.checkNotNullFields(result);
  }


  @Test
  void givenInstallmentsUnsortedWhenMapThenInstallmentsAreSortedByDueDate() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);
    byte[] hashedDebtorFiscalCode = {1, 2, 3};

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setDescription("TYPE_ORG_DESC");

    PaymentOption po = new PaymentOption();
    po.setPaymentOptionId(1L);

    InstallmentNoPII i1 = new InstallmentNoPII();
    i1.setInstallmentId(3L);
    i1.setDueDate(LocalDate.of(2025, 5, 10));
    i1.setStatus(InstallmentStatus.UNPAID);
    i1.setDebtorFiscalCodeHash(hashedDebtorFiscalCode);

    InstallmentNoPII i2 = new InstallmentNoPII();
    i2.setInstallmentId(4L);
    i2.setDueDate(LocalDate.of(2025, 1, 10));
    i2.setStatus(InstallmentStatus.UNPAID);
    i2.setDebtorFiscalCodeHash(hashedDebtorFiscalCode);

    InstallmentNoPII i3 = new InstallmentNoPII();
    i3.setInstallmentId(5L);
    i3.setDueDate(LocalDate.of(2025, 3, 10));
    i3.setStatus(InstallmentStatus.UNPAID);
    i3.setDebtorFiscalCodeHash(hashedDebtorFiscalCode);

    SortedSet<InstallmentNoPII> unsorted =
      new TreeSet<>(Comparator.comparing(InstallmentNoPII::getInstallmentId));
    unsorted.add(i1);
    unsorted.add(i2);
    unsorted.add(i3);

    po.setInstallments(unsorted);

    SortedSet<PaymentOption> options =
      new TreeSet<>(Comparator.comparing(PaymentOption::getPaymentOptionId));
    options.add(po);
    dp.setPaymentOptions(options);

    // when
    DebtorDebtPositionDTO result = mapper.map(dp, typeOrg, hashedDebtorFiscalCode);

    // then
    List<? extends BaseInstallment> sortedInstallments = result.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .stream()
      .toList();

    assertEquals(3, sortedInstallments.size());

    assertEquals(LocalDate.of(2025, 1, 10), sortedInstallments.get(0).getDueDate());
    assertEquals(LocalDate.of(2025, 3, 10), sortedInstallments.get(1).getDueDate());
    assertEquals(LocalDate.of(2025, 5, 10), sortedInstallments.get(2).getDueDate());

  }

  @Test
  void givenNullPaymentOptionsWhenMapThenPaymentOptionsListIsEmpty() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);
    dp.setPaymentOptions(null);

    // when
    DebtorDebtPositionDTO result = mapper.map(dp, new DebtPositionTypeOrg(), null);

    // then
    assertNotNull(result.getPaymentOptions());
    assertTrue(result.getPaymentOptions().isEmpty());
  }

  @Test
  void givenPaymentOptionWithoutInstallmentsWhenMapThenInstallmentsRemainEmpty() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);

    PaymentOption po = new PaymentOption();
    po.setPaymentOptionId(1L);
    po.setInstallments(null);

    SortedSet<PaymentOption> options = new TreeSet<>(Comparator.comparing(PaymentOption::getPaymentOptionId));
    options.add(po);
    dp.setPaymentOptions(options);

    // when
    DebtorDebtPositionDTO result = mapper.map(dp, new DebtPositionTypeOrg(), null);

    // then
    assertNull(result.getPaymentOptions().getFirst().getInstallments());
  }

}
