package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidInstallmentStatusException;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
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

import java.time.OffsetDateTime;
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
  void givenEmptyInstallmentListWhenFindAndValidatePrimaryOrgInstallmentThenOk() {
    // given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    String noticeNumber = "noticeNumber";

    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber,
      PrimaryOrgInstallmentPaidVerifierService.ORDINARY_DEBT_POSITION_ORIGINS)).thenReturn(List.of());

    //when
    Pair<Optional<InstallmentNoPII>,Boolean> primaryOrgInstallment = primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(organization, noticeNumber);

    //verify
    Assertions.assertNotNull(primaryOrgInstallment);
    Assertions.assertEquals(Optional.empty(), primaryOrgInstallment.getLeft());
    Assertions.assertEquals(false, primaryOrgInstallment.getRight());
    Mockito.verify(installmentNoPIIRepositoryMock, Mockito.times(1)).getByOrganizationIdAndNav(
      organization.getOrganizationId(), noticeNumber, PrimaryOrgInstallmentPaidVerifierService.ORDINARY_DEBT_POSITION_ORIGINS);
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
      getInstallmentExpired(OffsetDateTime.now().minusDays(1))
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
      getInstallmentExpired(OffsetDateTime.now().minusDays(2))
    );
    handleTest(targetInstallment, additionalInstallments, ExptectedOutcome.EXCEPTION);
  }

  @Test
  void givenSingleExpiredWhenFindAndValidatePrimaryOrgInstallmentThenFound() {
    InstallmentNoPII targetInstallment = getInstallmentExpired(OffsetDateTime.now().minusDays(3));
    List<InstallmentNoPII> additionalInstallments = List.of(
      getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.PAID)
    );
    handleTest(targetInstallment, additionalInstallments, ExptectedOutcome.FOUND_VALID);
  }

  @Test
  void givenMultipleExpiredWhenFindAndValidatePrimaryOrgInstallmentThenFound() {
    InstallmentNoPII targetInstallment = getInstallmentExpired(OffsetDateTime.now().minusDays(3));
    List<InstallmentNoPII> additionalInstallments = List.of(
      getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.PAID),
      getInstallmentExpired(OffsetDateTime.now().minusDays(4))
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
  static InstallmentNoPII getInstallmentExpired(OffsetDateTime dueDate) {
    return getInstallment(InstallmentStatus.EXPIRED, null, null, dueDate);
  }
  private static InstallmentNoPII getInstallment(InstallmentStatus status, InstallmentStatus statusFrom, InstallmentStatus statusTo, OffsetDateTime dueDate) {
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(status);
    if(status == InstallmentStatus.TO_SYNC){
      installment.getSyncStatus().setSyncStatusFrom(statusFrom);
      installment.getSyncStatus().setSyncStatusTo(statusTo);
    }
    if(dueDate != null){
      installment.setDueDate(dueDate);
    }
    return installment;
  }

  private void handleTest(InstallmentNoPII targetInstallment, List<InstallmentNoPII> additionalInstallments, ExptectedOutcome expectedOutcome) {
    // given
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    String noticeNumber = "noticeNumber";

    List<InstallmentNoPII> installments = new ArrayList<>();
    installments.add(targetInstallment);
    //add a paid installment
    InstallmentNoPII otherInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    otherInstallment.setStatus(InstallmentStatus.PAID);
    installments.add(otherInstallment);
    //add a reported installment
    otherInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    otherInstallment.setStatus(InstallmentStatus.REPORTED);
    installments.add(otherInstallment);
    //add other test-case related installments
    if (additionalInstallments != null)
      installments.addAll(additionalInstallments);


    Mockito.when(installmentNoPIIRepositoryMock.getByOrganizationIdAndNav(organization.getOrganizationId(), noticeNumber,
      PrimaryOrgInstallmentPaidVerifierService.ORDINARY_DEBT_POSITION_ORIGINS)).thenReturn(installments);

    //when
    if(expectedOutcome == ExptectedOutcome.EXCEPTION){
      Assertions.assertThrows(InvalidInstallmentStatusException.class, () -> primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(organization, noticeNumber));
    } else {
      Pair<Optional<InstallmentNoPII>, Boolean> primaryOrgInstallment = primaryOrgInstallmentPaidVerifierService.findAndValidatePrimaryOrgInstallment(organization, noticeNumber);
      Assertions.assertNotNull(primaryOrgInstallment);
      Assertions.assertEquals(true, primaryOrgInstallment.getRight());
      //verify
      if(expectedOutcome == ExptectedOutcome.FOUND_VALID){
        Assertions.assertEquals(Optional.of(targetInstallment), primaryOrgInstallment.getLeft());
      } else {
        Assertions.assertEquals(Optional.empty(), primaryOrgInstallment.getLeft());
      }
    }

    //verify
    Mockito.verify(installmentNoPIIRepositoryMock, Mockito.times(1)).getByOrganizationIdAndNav(
      organization.getOrganizationId(), noticeNumber, PrimaryOrgInstallmentPaidVerifierService.ORDINARY_DEBT_POSITION_ORIGINS);
  }
}
