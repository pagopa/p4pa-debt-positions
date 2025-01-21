package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DebtPositionInnerStatusAlignerServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;

  private DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerService;

  @BeforeEach
  void setUp() {
    debtPositionInnerStatusAlignerService = new DebtPositionInnerStatusAlignerServiceImpl(debtPositionRepositoryMock);
  }

  @Test
  void testUpdateDebtPositionStatus() {
    // given
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(1L);

    PaymentOption paymentOption1 = new PaymentOption();
    paymentOption1.setPaymentOptionId(1L);
    paymentOption1.setStatus(PaymentOptionStatus.TO_SYNC);

    PaymentOption paymentOption2 = new PaymentOption();
    paymentOption2.setPaymentOptionId(2L);
    paymentOption2.setStatus(PaymentOptionStatus.PAID);

    debtPosition.setPaymentOptions(new TreeSet<>(List.of(paymentOption1, paymentOption2)));

    Mockito.doNothing().when(debtPositionRepositoryMock).updateStatus(1L, DebtPositionStatus.TO_SYNC);

    // when
    debtPositionInnerStatusAlignerService.updateDebtPositionStatus(debtPosition);

    // then
    assertEquals(DebtPositionStatus.TO_SYNC, debtPosition.getStatus());
    verify(debtPositionRepositoryMock, times(1)).updateStatus(1L,  DebtPositionStatus.TO_SYNC);
  }
}

