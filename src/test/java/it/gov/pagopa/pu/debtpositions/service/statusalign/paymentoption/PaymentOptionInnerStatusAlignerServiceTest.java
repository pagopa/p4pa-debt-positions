package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
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
class PaymentOptionInnerStatusAlignerServiceTest {

  @Mock
  private PaymentOptionRepository paymentOptionRepositoryMock;

  private PaymentOptionInnerStatusAlignerService service;

  @BeforeEach
  void setUp() {
    service = new PaymentOptionInnerStatusAlignerServiceImpl(paymentOptionRepositoryMock);
  }

  @Test
  void testUpdatePaymentOptionStatus(){
    // given
    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(1L);

    InstallmentNoPII installmentNoPII1 = new InstallmentNoPII();
    installmentNoPII1.setInstallmentId(1L);
    installmentNoPII1.setStatus(InstallmentStatus.TO_SYNC);

    InstallmentNoPII installmentNoPII2 = new InstallmentNoPII();
    installmentNoPII2.setInstallmentId(1L);
    installmentNoPII2.setStatus(InstallmentStatus.PAID);

    paymentOption.setInstallments(new TreeSet<>(List.of(installmentNoPII1, installmentNoPII2)));

    Mockito.doNothing().when(paymentOptionRepositoryMock).updateStatus(1L, PaymentOptionStatus.TO_SYNC);

    // when
    service.updatePaymentOptionStatus(paymentOption);

    // then
    assertEquals(PaymentOptionStatus.TO_SYNC, paymentOption.getStatus());
    verify(paymentOptionRepositoryMock, times(1)).updateStatus(1L,  PaymentOptionStatus.TO_SYNC);
  }


}
