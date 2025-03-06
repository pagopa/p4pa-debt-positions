package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.TreeSet;

@Slf4j
@ExtendWith(MockitoExtension.class)
class InstallmentUpdateServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;

  @InjectMocks
  private InstallmentUpdateService installmentUpdateService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenFoundDebtPositionWhenUpdateInstallmentStatusOfDebtPositionThenOk(){
    ReceiptDTO receiptDTO = podamFactory.manufacturePojo(ReceiptDTO.class);
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.UNPAID);
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);

    List<PaymentOption> paymentOptionList = debtPosition.getPaymentOptions().stream().toList();

    paymentOptionList.get(0).setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.PAID),
      targetInstallment
    )));
    paymentOptionList.get(1).setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.DRAFT),
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID),
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.REPORTED)
    )));
    paymentOptionList.get(2).setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.PAID),
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.INVALID)
    )));

    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(debtPosition.getDebtPositionId());
      paymentOption.getInstallments().forEach(anInstallment -> anInstallment.setPaymentOptionId(paymentOption.getPaymentOptionId()));
    });

    Mockito.when(debtPositionRepositoryMock.findByInstallmentId(targetInstallment.getInstallmentId())).thenReturn(debtPosition);

    DebtPosition response = installmentUpdateService.updateInstallmentStatusOfDebtPosition(targetInstallment, receiptDTO);

    Assertions.assertEquals(debtPosition, response);
    InstallmentNoPII responseTargetInstallment = response.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(anInstallment -> anInstallment.getInstallmentId().equals(targetInstallment.getInstallmentId()))
      .findFirst().orElse(null);
    Assertions.assertNotNull(responseTargetInstallment);
    verifyInstallmentStatus(responseTargetInstallment, InstallmentStatus.PAID, "target installment status");
    Assertions.assertEquals(receiptDTO.getReceiptId(), responseTargetInstallment.getReceiptId());
    int[] idxPo = {0};
    int[] idxInst = {0};
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      if (!paymentOption.getPaymentOptionId().equals(targetInstallment.getPaymentOptionId())) {
        paymentOption.getInstallments().forEach(anInstallment -> {
          if (InstallmentUpdateService.TRANSITION_TO_SYNC_REQUIRED.contains(anInstallment.getStatus() + "|" + InstallmentStatus.INVALID)) {
            verifyInstallmentStatus(anInstallment, InstallmentStatus.INVALID,
              "Installment[%s][%s] of payment option[%s][%s] is [%s]".formatted(
                idxInst[0], anInstallment.getInstallmentId(), idxPo[0], paymentOption.getPaymentOptionId(),anInstallment.getStatus()));
          }
          idxInst[0]++;
        });
      }
      idxPo[0]++;
    });
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1)).findByInstallmentId(targetInstallment.getInstallmentId());
  }

  @Test
  void givenNotFoundFoundDebtPositionWhenUpdateInstallmentStatusOfDebtPositionThenException(){
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.UNPAID);
    ReceiptDTO receiptDTO = podamFactory.manufacturePojo(ReceiptDTO.class);

    Mockito.when(debtPositionRepositoryMock.findByInstallmentId(targetInstallment.getInstallmentId())).thenReturn(null);
    NotFoundException response = Assertions.assertThrows(NotFoundException.class, () -> installmentUpdateService.updateInstallmentStatusOfDebtPosition(targetInstallment, receiptDTO));
    Assertions.assertTrue(response.getMessage().startsWith("debt position not found"));
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1)).findByInstallmentId(targetInstallment.getInstallmentId());
  }

  @Test
  void givenNotFoundFoundInstallmentWhenUpdateInstallmentStatusOfDebtPositionThenException(){
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.UNPAID);
    ReceiptDTO receiptDTO = podamFactory.manufacturePojo(ReceiptDTO.class);
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(debtPosition.getDebtPositionId());
      paymentOption.getInstallments().forEach(anInstallment -> {
        anInstallment.setPaymentOptionId(paymentOption.getPaymentOptionId());
        if(anInstallment.getInstallmentId().equals(targetInstallment.getInstallmentId())){
          anInstallment.setInstallmentId(anInstallment.getInstallmentId()+1);
        }
      });
    });
    Mockito.when(debtPositionRepositoryMock.findByInstallmentId(targetInstallment.getInstallmentId())).thenReturn(debtPosition);
    NotFoundException response = Assertions.assertThrows(NotFoundException.class, () -> installmentUpdateService.updateInstallmentStatusOfDebtPosition(targetInstallment, receiptDTO));
    Assertions.assertTrue(response.getMessage().startsWith("primary installment not found"));
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1)).findByInstallmentId(targetInstallment.getInstallmentId());
  }

  private void verifyInstallmentStatus(InstallmentNoPII installment, InstallmentStatus expectedStatus, String message) {
    Assertions.assertEquals(expectedStatus, installment.getStatus(), message);
  }
}
