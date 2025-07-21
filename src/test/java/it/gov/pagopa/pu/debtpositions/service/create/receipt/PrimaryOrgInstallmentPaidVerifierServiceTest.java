package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidInstallmentStatusException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PrimaryOrgInstallmentPaidVerifierServiceTest {

  private enum ExptectedOutcome { FOUND_VALID, FOUND_INVALID, EXCEPTION }

  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;

  @InjectMocks
  private PrimaryOrgInstallmentPaidVerifierService primaryOrgInstallmentPaidVerifierService;

  private static final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenIudAndInstallmentMismatchNoticeNumberThenException() {
    // given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    String iud = "iud";
    String noticeNumber = "expected-nav";

    InstallmentNoPII installment = getInstallment(InstallmentStatus.UNPAID);
    installment.setNav("different-nav");

    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndIudAndStatus(organization.getOrganizationId(), iud, null))
      .thenReturn(List.of(installment));

    // when then
    Assertions.assertThrows(InvalidValueException.class, () ->
      primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(organization, noticeNumber, iud));
  }

  @Test
  void givenIudAndMultipleInstallmentsThenException() {
    // given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    String iud = "iud";
    String noticeNumber = "noticeNumber";

    InstallmentNoPII i1 = getInstallment(InstallmentStatus.UNPAID);
    i1.setNav(noticeNumber);
    InstallmentNoPII i2 = getInstallment(InstallmentStatus.UNPAID);
    i2.setNav(noticeNumber);

    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndIudAndStatus(organization.getOrganizationId(), iud, null))
      .thenReturn(List.of(i1, i2));

    // when then
    Assertions.assertThrows(InvalidValueException.class, () ->
      primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(organization, noticeNumber, iud));
  }

  @Test
  void givenIudAndValidSingleInstallmentThenFound() {
    // given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    String iud = "iud";
    String noticeNumber = "noticeNumber";

    InstallmentNoPII installment = getInstallment(InstallmentStatus.UNPAID);
    installment.setNav(noticeNumber);

    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndIudAndStatus(organization.getOrganizationId(), iud, null))
      .thenReturn(List.of(installment));

    // when
    Pair<Optional<InstallmentNoPII>, Boolean> primaryOrgInstallment =
      primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(organization, noticeNumber, iud);

    // then
    Assertions.assertEquals(true, primaryOrgInstallment.getRight());
    Assertions.assertEquals(Optional.of(installment), primaryOrgInstallment.getLeft());
  }

  @Test
  void givenIudAndNoInstallmentsThenNotFound() {
    // given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    String iud = "iud";
    String noticeNumber = "noticeNumber";

    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndIudAndStatus(organization.getOrganizationId(), iud, null))
      .thenReturn(List.of());

    // when
    Pair<Optional<InstallmentNoPII>, Boolean> result =
      primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(organization, noticeNumber, iud);

    // then
    Assertions.assertEquals(false, result.getRight());
    Assertions.assertEquals(Optional.empty(), result.getLeft());
  }

  @Test
  void givenSingleUnpaidWhenFindAndValidatePrimaryOrgInstallmentThenFound() {
    InstallmentNoPII targetInstallment = getInstallment(InstallmentStatus.UNPAID);
    handleTest(targetInstallment, null, ExptectedOutcome.FOUND_VALID);
  }

  @Test
  void givenMultipleUnpaidWhenFindAndValidatePrimaryOrgInstallmentThenException() {
    InstallmentNoPII targetInstallment = getInstallment(InstallmentStatus.UNPAID);
    List<InstallmentNoPII> additionalInstallments = List.of(
      getInstallment(InstallmentStatus.UNPAID),
      getInstallmentToSync(InstallmentStatus.EXPIRED, InstallmentStatus.UNPAID),
      getInstallmentExpired(LocalDate.now().minusDays(1))
    );
    handleTest(targetInstallment, additionalInstallments, ExptectedOutcome.EXCEPTION);
  }

  @Test
  void givenSingleSyncToUnpaidWhenFindAndValidatePrimaryOrgInstallmentThenFound() {
    InstallmentNoPII targetInstallment = getInstallmentToSync(InstallmentStatus.EXPIRED, InstallmentStatus.UNPAID);
    handleTest(targetInstallment, null, ExptectedOutcome.FOUND_VALID);
  }

  @Test
  void givenMultipleSyncToUnpaidWhenFindAndValidatePrimaryOrgInstallmentThenException() {
    InstallmentNoPII targetInstallment = getInstallmentToSync(InstallmentStatus.EXPIRED, InstallmentStatus.UNPAID);
    List<InstallmentNoPII> additionalInstallments = List.of(
      getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID),
      getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.PAID),
      getInstallmentExpired(LocalDate.now().minusDays(2))
    );
    handleTest(targetInstallment, additionalInstallments, ExptectedOutcome.EXCEPTION);
  }

  @Test
  void givenSingleExpiredWhenFindAndValidatePrimaryOrgInstallmentThenFound() {
    InstallmentNoPII targetInstallment = getInstallmentExpired(LocalDate.now().minusDays(3));
    List<InstallmentNoPII> additionalInstallments = List.of(
      getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.PAID)
    );
    handleTest(targetInstallment, additionalInstallments, ExptectedOutcome.FOUND_VALID);
  }

  @Test
  void givenMultipleExpiredWhenFindAndValidatePrimaryOrgInstallmentThenFound() {
    InstallmentNoPII targetInstallment = getInstallmentExpired(LocalDate.now().minusDays(3));
    List<InstallmentNoPII> additionalInstallments = List.of(
      getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.PAID),
      getInstallmentExpired(LocalDate.now().minusDays(4))
    );
    handleTest(targetInstallment, additionalInstallments, ExptectedOutcome.FOUND_VALID);
  }

  @Test
  void givenFallbackCaseWhenFindAndValidatePrimaryOrgInstallmentThenNotFound() {
    InstallmentNoPII targetInstallment = getInstallment(InstallmentStatus.PAID);
    List<InstallmentNoPII> additionalInstallments = List.of(
      getInstallment(InstallmentStatus.DRAFT),
      getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.PAID)
    );
    handleTest(targetInstallment, additionalInstallments, ExptectedOutcome.FOUND_INVALID);
  }

  static InstallmentNoPII getInstallment(InstallmentStatus status) {
    return getInstallment(status, null, null, null);
  }

  static InstallmentNoPII getInstallmentToSync(InstallmentStatus statusFrom, InstallmentStatus statusTo) {
    return getInstallment(InstallmentStatus.TO_SYNC, statusFrom, statusTo, null);
  }

  static InstallmentNoPII getInstallmentExpired(LocalDate dueDate) {
    return getInstallment(InstallmentStatus.EXPIRED, null, null, dueDate);
  }

  private static InstallmentNoPII getInstallment(InstallmentStatus status, InstallmentStatus statusFrom, InstallmentStatus statusTo, LocalDate dueDate) {
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(status);
    if (status == InstallmentStatus.TO_SYNC) {
      installment.getSyncStatus().setSyncStatusFrom(statusFrom);
      installment.getSyncStatus().setSyncStatusTo(statusTo);
    }
    if (dueDate != null) {
      installment.setDueDate(dueDate);
    }
    installment.setIud("iud");
    return installment;
  }

  private void handleTest(InstallmentNoPII targetInstallment, List<InstallmentNoPII> additionalInstallments, ExptectedOutcome expectedOutcome) {
    // given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    String noticeNumber = "noticeNumber";
    String iud = "";

    List<InstallmentNoPII> installments = new ArrayList<>();
    installments.add(targetInstallment);
    InstallmentNoPII otherInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    otherInstallment.setStatus(InstallmentStatus.PAID);
    installments.add(otherInstallment);
    otherInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    otherInstallment.setStatus(InstallmentStatus.REPORTED);
    installments.add(otherInstallment);
    if (additionalInstallments != null)
      installments.addAll(additionalInstallments);

    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber,
      InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS)).thenReturn(installments);

    // when
    if (expectedOutcome == ExptectedOutcome.EXCEPTION) {
      Assertions.assertThrows(InvalidInstallmentStatusException.class, () ->
        primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(organization, noticeNumber, iud));
    } else {
      Pair<Optional<InstallmentNoPII>, Boolean> primaryOrgInstallment =
        primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(organization, noticeNumber, iud);
      Assertions.assertNotNull(primaryOrgInstallment);
      Assertions.assertEquals(true, primaryOrgInstallment.getRight());
      if (expectedOutcome == ExptectedOutcome.FOUND_VALID) {
        Assertions.assertEquals(Optional.of(targetInstallment), primaryOrgInstallment.getLeft());
      } else {
        Assertions.assertEquals(Optional.empty(), primaryOrgInstallment.getLeft());
      }
    }

    // then
    Mockito.verify(installmentNoPIIRepositoryMock, Mockito.times(1)).getByOrganizationIdAndNav(
      organization.getOrganizationId(), noticeNumber, InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS);
  }
}
