package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class DebtorDebtPositionMapperTest {

  private final DebtorDebtPositionMapper mapper =  Mappers.getMapper(DebtorDebtPositionMapper.class);

  private final PodamFactory podam = TestUtils.getPodamFactory();

  @Test
  void givenValidDebtPositionAndTypeOrgWhenMapThenReturnDTO() {
    // given
    DebtPosition debtPosition = podam.manufacturePojo(DebtPosition.class);
    debtPosition.setDebtPositionId(100L);
    debtPosition.setDescription("DEBT_POS_DESC");

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setDescription("TYPE_ORG_DESCRIPTION");

    // when
    DebtorDebtPositionDTO result = mapper.map(debtPosition, typeOrg);

    // then
    assertNotNull(result);
    assertEquals(100L, result.getDebtPositionId());
    assertEquals("DEBT_POS_DESC", result.getDebtPositionDescription());
    assertEquals("TYPE_ORG_DESCRIPTION", result.getDebtPositionTypeOrgDescription());
    assertEquals(debtPosition.getStatus(), result.getStatus());
    assertEquals(debtPosition.getDebtPositionOrigin(), result.getDebtPositionOrigin());
    assertEquals(debtPosition.getOrganizationId(), result.getOrganizationId());

    TestUtils.checkNotNullFields(result);
  }

  @Test
  void givenDebtPositionWithPaymentOptionsWhenMapThenPaymentOptionsAreMapped() {
    // given
    DebtPosition debtPosition = podam.manufacturePojo(DebtPosition.class);
    PaymentOption po1 = podam.manufacturePojo(PaymentOption.class);
    PaymentOption po2 = podam.manufacturePojo(PaymentOption.class);

    SortedSet<PaymentOption> options = new TreeSet<>(Comparator.comparing(PaymentOption::getPaymentOptionId));
    options.add(po1);
    options.add(po2);

    debtPosition.setPaymentOptions(options);

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setDescription("TYPE_ORG_DESC");

    // when
    DebtorDebtPositionDTO result = mapper.map(debtPosition, typeOrg);

    // then
    assertNotNull(result.getPaymentOptions());
    assertEquals(2, result.getPaymentOptions().size());

    TestUtils.checkNotNullFields(result);
  }

  @Test
  void givenInstallmentsUnsortedWhenMapThenInstallmentsAreSortedByDueDate() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);

    PaymentOption po = new PaymentOption();
    po.setPaymentOptionId(1L);

    InstallmentNoPII i1 = new InstallmentNoPII();
    i1.setInstallmentId(3L);
    i1.setDueDate(LocalDate.of(2025, 5, 10));

    InstallmentNoPII i2 = new InstallmentNoPII();
    i2.setInstallmentId(4L);
    i2.setDueDate(LocalDate.of(2025, 1, 10));

    InstallmentNoPII i3 = new InstallmentNoPII();
    i3.setInstallmentId(5L);
    i3.setDueDate(LocalDate.of(2025, 3, 10));

    SortedSet<InstallmentNoPII> unsorted = new TreeSet<>(Comparator.comparing(InstallmentNoPII::getInstallmentId));
    unsorted.add(i1);
    unsorted.add(i2);
    unsorted.add(i3);

    po.setInstallments(unsorted);

    SortedSet<PaymentOption> options = new TreeSet<>(Comparator.comparing(PaymentOption::getPaymentOptionId));
    options.add(po);
    dp.setPaymentOptions(options);

    // when
    DebtorDebtPositionDTO result = mapper.map(dp, new DebtPositionTypeOrg());

    // then
    List<?> installments = result.getPaymentOptions().getFirst().getInstallments().stream().toList();

    assertEquals(LocalDate.of(2025, 1, 10), ((InstallmentNoPII) installments.getFirst()).getDueDate());
    assertEquals(LocalDate.of(2025, 3, 10), ((InstallmentNoPII) installments.get(1)).getDueDate());
    assertEquals(LocalDate.of(2025, 5, 10), ((InstallmentNoPII) installments.get(2)).getDueDate());
  }

  @Test
  void givenNullPaymentOptionsWhenMapThenPaymentOptionsListIsEmpty() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);
    dp.setPaymentOptions(null);

    // when
    DebtorDebtPositionDTO result = mapper.map(dp, new DebtPositionTypeOrg());

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
    DebtorDebtPositionDTO result = mapper.map(dp, new DebtPositionTypeOrg());

    // then
    assertTrue(result.getPaymentOptions().getFirst().getInstallments().isEmpty());
  }

}
