package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentOptionExtendedControllerTest {

  @Mock
  private PaymentOptionRepository repositoryMock;

  private PaymentOptionEntityExtendedController controller;

  @BeforeEach
  void init() {
    controller = new PaymentOptionEntityExtendedController(repositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(repositoryMock);
  }

  @Test
  void whenUpdateStatusThenInvokeRepository() {
    // Given
    Long installmentId = 1L;

    // When
    controller.updateStatus(installmentId, PaymentOptionStatus.UNPAID);

    // Then
    verify(repositoryMock).updateStatus(installmentId, PaymentOptionStatus.UNPAID);
  }
}
