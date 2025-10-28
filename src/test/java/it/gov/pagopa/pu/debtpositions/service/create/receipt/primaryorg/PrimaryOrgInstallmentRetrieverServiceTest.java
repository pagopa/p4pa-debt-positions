package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PrimaryOrgInstallmentRetrieverServiceTest {

  @Mock
  private InstallmentNoPIIRepository repositoryMock;

  private PrimaryOrgInstallmentRetrieverService service;

  @BeforeEach
  void init() {
    service = new PrimaryOrgInstallmentRetrieverService(repositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      repositoryMock
    );
  }

//region desc=findByIud
  @Test
  void givenIudAndNoInstallmentWhenRetrieveThenReturnEmpty() {
    // Given
    Organization primaryOrg = new Organization();
    primaryOrg.setOrganizationId(-1L);
    String nav = "NAV";
    String iud = "IUD";

    Mockito.when(repositoryMock.getByOrganizationIdAndIudAndStatus(primaryOrg.getOrganizationId(), iud, null))
      .thenReturn(List.of());

    // When
    Optional<InstallmentNoPII> result = service.retrieve(primaryOrg, nav, iud);

    // Then
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void givenIudAndTooManyInstallmentWhenRetrieveThenThrowConflict() {
    // Given
    Organization primaryOrg = new Organization();
    primaryOrg.setOrganizationId(-1L);
    String nav = "NAV";
    String iud = "IUD";

    Mockito.when(repositoryMock.getByOrganizationIdAndIudAndStatus(primaryOrg.getOrganizationId(), iud, null))
      .thenReturn(List.of(new InstallmentNoPII(), new InstallmentNoPII()));

    // When, Then
    Assertions.assertThrows(ConflictErrorException.class, () -> service.retrieve(primaryOrg, nav, iud));
  }

  @Test
  void givenIudAndDifferentNavWhenRetrieveThenThrowConflict() {
    // Given
    Organization primaryOrg = new Organization();
    primaryOrg.setOrganizationId(-1L);
    String nav = "NAV";
    String iud = "IUD";
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setNav("UNEXPECTED");

    Mockito.when(repositoryMock.getByOrganizationIdAndIudAndStatus(primaryOrg.getOrganizationId(), iud, null))
      .thenReturn(List.of(installment));

    // When, Then
    Assertions.assertThrows(ConflictErrorException.class, () -> service.retrieve(primaryOrg, nav, iud));
  }

  @Test
  void givenIudAndSameNavWhenRetrieveThenOk() {
    // Given
    Organization primaryOrg = new Organization();
    primaryOrg.setOrganizationId(-1L);
    String nav = "NAV";
    String iud = "IUD";
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setNav(nav);

    Mockito.when(repositoryMock.getByOrganizationIdAndIudAndStatus(primaryOrg.getOrganizationId(), iud, null))
      .thenReturn(List.of(installment));

    // When
    Optional<InstallmentNoPII> result = service.retrieve(primaryOrg, nav, iud);

    // Then
    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(installment, result.get());
  }
//endregion

//region desc=findByNav
@Test
void givenNavAndNoInstallmentWhenRetrieveThenReturnEmpty() {
  // Given
  Organization primaryOrg = new Organization();
  primaryOrg.setOrganizationId(-1L);
  String nav = "NAV";

  Mockito.when(repositoryMock.getByOrganizationIdAndNav(primaryOrg.getOrganizationId(), nav, InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS))
    .thenReturn(List.of());

  // When
  Optional<InstallmentNoPII> result = service.retrieve(primaryOrg, nav, null);

  // Then
  Assertions.assertTrue(result.isEmpty());
}

  @Test
  void givenNavAndTooManyInstallmentWhenRetrieveThenThrowConflict() {
    // Given
    Organization primaryOrg = new Organization();
    primaryOrg.setOrganizationId(-1L);
    String nav = "NAV";
    InstallmentNoPII installment1 = new InstallmentNoPII();
    installment1.setStatus(InstallmentStatus.UNPAID);
    InstallmentNoPII installment2 = new InstallmentNoPII();
    installment2.setStatus(InstallmentStatus.UNPAYABLE);

    Mockito.when(repositoryMock.getByOrganizationIdAndNav(primaryOrg.getOrganizationId(), nav, InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS))
      .thenReturn(List.of(installment1, installment2));

    // When, Then
    Assertions.assertThrows(ConflictErrorException.class, () -> service.retrieve(primaryOrg, nav, null));
  }

  @Test
  void givenNavAndUniqueNotCancelledWhenRetrieveThenOk() {
    // Given
    Organization primaryOrg = new Organization();
    primaryOrg.setOrganizationId(-1L);
    String nav = "NAV";
    InstallmentNoPII installment1 = new InstallmentNoPII();
    installment1.setStatus(InstallmentStatus.UNPAID);
    InstallmentNoPII installment2 = new InstallmentNoPII();
    installment2.setStatus(InstallmentStatus.CANCELLED);

    Mockito.when(repositoryMock.getByOrganizationIdAndNav(primaryOrg.getOrganizationId(), nav, InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS))
      .thenReturn(List.of(installment1, installment2));

    // When
    Optional<InstallmentNoPII> result = service.retrieve(primaryOrg, nav, null);

    // Then
    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(installment1, result.get());
  }
//endregion
}
