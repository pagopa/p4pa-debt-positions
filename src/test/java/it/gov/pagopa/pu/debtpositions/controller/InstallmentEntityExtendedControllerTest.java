package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstallmentEntityExtendedControllerTest {

  private static final LocalDate DATE = LocalDate.of(2099, 1, 1);

  @Mock
  private InstallmentNoPIIRepository repositoryMock;

  private InstallmentEntityExtendedController controller;

  @BeforeEach
  void init() {
    controller = new InstallmentEntityExtendedController(repositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(repositoryMock);
  }

  @Test
  void whenUpdateDueDateThenInvokeRepository() {
    // Given
    Long installmentId = 1L;

    // When
    controller.updateDueDate(installmentId, DATE);

    // Then
    verify(repositoryMock).updateDueDate(installmentId, DATE);
  }

  @Test
  void givenNoSyncStatusWhenUpdateStatusAndToSyncStatusThenInvokeRepository() {
    // Given
    Long installmentId = 1L;

    // When
    controller.updateStatusAndToSyncStatus(installmentId, InstallmentStatus.UNPAID, null, null);

    // Then
    verify(repositoryMock).updateStatus(installmentId, InstallmentStatus.UNPAID, null);
  }

  @Test
  void whenUpdateStatusAndToSyncStatusThenInvokeRepository() {
    // Given
    Long installmentId = 1L;
    InstallmentSyncStatus syncStatus = new InstallmentSyncStatus(InstallmentStatus.UNPAID, InstallmentStatus.UNPAYABLE);

    // When
    controller.updateStatusAndToSyncStatus(installmentId, InstallmentStatus.TO_SYNC, syncStatus.getSyncStatusFrom(), syncStatus.getSyncStatusTo());

    // Then
    verify(repositoryMock).updateStatus(installmentId, InstallmentStatus.TO_SYNC, syncStatus);
  }

  @Test
  void whenUpdateIunThenInvokeRepository() {
    // Given
    Long debtPositionId = 1L;
    String iun = "IUN";

    // When
    controller.updateIun(debtPositionId, iun);

    // Then
    verify(repositoryMock).updateIun(debtPositionId, iun);
  }
}
