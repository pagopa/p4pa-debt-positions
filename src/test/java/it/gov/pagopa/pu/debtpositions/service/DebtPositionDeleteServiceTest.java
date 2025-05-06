package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;

@ExtendWith(MockitoExtension.class)
class DebtPositionDeleteServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private PaymentOptionRepository paymentOptionRepositoryMock;
  @Mock
  private InstallmentPIIRepository installmentPIIRepositoryMock;
  @Mock
  private TransferRepository transferRepositoryMock;

  private DebtPositionDeleteService debtPositionDeleteService;

  @BeforeEach
  void setUp() {
    debtPositionDeleteService = new DebtPositionDeleteService(
      debtPositionRepositoryMock,
      paymentOptionRepositoryMock,
      installmentPIIRepositoryMock,
      transferRepositoryMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      debtPositionRepositoryMock,
      paymentOptionRepositoryMock,
      installmentPIIRepositoryMock,
      transferRepositoryMock);
  }

  @Test
  void whenDeleteThenSuccess(){
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.doNothing().when(transferRepositoryMock).delete(debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst());
    Mockito.doNothing().when(installmentPIIRepositoryMock).delete(debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst());
    Mockito.doNothing().when(paymentOptionRepositoryMock).delete(debtPosition.getPaymentOptions().getFirst());
    Mockito.doNothing().when(debtPositionRepositoryMock).delete(debtPosition);

    Assertions.assertDoesNotThrow(() -> debtPositionDeleteService.delete(debtPosition));
  }

}
