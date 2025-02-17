package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
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
  void givenSyncInteractionAndFoundDebtPositionWhenUpdateInstallmentStatusOfDebtPositionThenOk(){
    handleUpdateInstallmentStatusOfDebtPosition(Broker.PagoPaInteractionModelEnum.SYNC);
  }

  @Test
  void givenAcaInteractionAndFoundDebtPositionWhenUpdateInstallmentStatusOfDebtPositionThenOk(){
    handleUpdateInstallmentStatusOfDebtPosition(Broker.PagoPaInteractionModelEnum.SYNC_ACA);
  }

  @Test
  void givenGpdInteractionAndFoundDebtPositionWhenUpdateInstallmentStatusOfDebtPositionThenOk(){
    handleUpdateInstallmentStatusOfDebtPosition(Broker.PagoPaInteractionModelEnum.ASYNC_GPD);
  }

  @Test
  void givenNotFoundFoundDebtPositionWhenUpdateInstallmentStatusOfDebtPositionThenException(){
    //given
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.UNPAID);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    ReceiptDTO receiptDTO = podamFactory.manufacturePojo(ReceiptDTO.class);

    Mockito.when(debtPositionRepositoryMock.findByInstallmentId(targetInstallment.getInstallmentId())).thenReturn(null);
    //when
    NotFoundException response = Assertions.assertThrows(NotFoundException.class, () -> installmentUpdateService.updateInstallmentStatusOfDebtPosition(targetInstallment, broker, receiptDTO));
    //verify
    Assertions.assertTrue(response.getMessage().startsWith("debt position not found"));
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1)).findByInstallmentId(targetInstallment.getInstallmentId());
  }

  @Test
  void givenNotFoundFoundInstallmentWhenUpdateInstallmentStatusOfDebtPositionThenException(){
    //given
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.UNPAID);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    ReceiptDTO receiptDTO = podamFactory.manufacturePojo(ReceiptDTO.class);
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    //align entities id
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
    //when
    NotFoundException response = Assertions.assertThrows(NotFoundException.class, () -> installmentUpdateService.updateInstallmentStatusOfDebtPosition(targetInstallment, broker, receiptDTO));
    //verify
    Assertions.assertTrue(response.getMessage().startsWith("primary installment not found"));
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1)).findByInstallmentId(targetInstallment.getInstallmentId());
  }


  private void handleUpdateInstallmentStatusOfDebtPosition(Broker.PagoPaInteractionModelEnum pagoPaInteractionModelEnum) {
    //given
    ReceiptDTO receiptDTO = podamFactory.manufacturePojo(ReceiptDTO.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setPagoPaInteractionModel(pagoPaInteractionModelEnum);
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.UNPAID);
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);

    List<PaymentOption> paymentOptionList = debtPosition.getPaymentOptions().stream().toList();

    paymentOptionList.getFirst().setInstallments(new TreeSet<>(List.of(
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
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallmentToSync(InstallmentStatus.DRAFT, InstallmentStatus.PAID)
    )));

    //align entities id
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(debtPosition.getDebtPositionId());
      paymentOption.getInstallments().forEach(anInstallment -> anInstallment.setPaymentOptionId(paymentOption.getPaymentOptionId()));
    });

    Mockito.when(debtPositionRepositoryMock.findByInstallmentId(targetInstallment.getInstallmentId())).thenReturn(debtPosition);

    //when
    DebtPosition response = installmentUpdateService.updateInstallmentStatusOfDebtPosition(targetInstallment, broker, receiptDTO);

    //verify
    Assertions.assertEquals(debtPosition, response);
    //find target installment in response
    InstallmentNoPII responseTargetInstallment = response.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(anInstallment -> anInstallment.getInstallmentId().equals(targetInstallment.getInstallmentId()))
      .findFirst().orElse(null);
    Assertions.assertNotNull(responseTargetInstallment);
    //verify that the target installment has been updated to (TO_SYNC->)PAID
    verifyInstallmentStatus(responseTargetInstallment, broker, InstallmentStatus.PAID, "target installment status");
    //verify that there no more NOT-PAID installments on payment options different of the one of the target installment
    int[] idxPo = {0};
    int[] idxInst = {0};
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      if (!paymentOption.getPaymentOptionId().equals(targetInstallment.getPaymentOptionId())) {
        paymentOption.getInstallments().forEach(anInstallment -> {
          if (InstallmentUpdateService.NOT_PAID.contains(anInstallment.getStatus())) {
            verifyInstallmentStatus(anInstallment, broker, InstallmentStatus.INVALID,
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

  private void verifyInstallmentStatus(InstallmentNoPII installmentNoPII, Broker broker, InstallmentStatus expectedStatus, String message) {
    if (broker.getPagoPaInteractionModel().equals(Broker.PagoPaInteractionModelEnum.SYNC)) {
      Assertions.assertEquals(expectedStatus, installmentNoPII.getStatus(), message);
    } else {
      Assertions.assertEquals(InstallmentStatus.TO_SYNC, installmentNoPII.getStatus(), message);
      Assertions.assertNotNull(installmentNoPII.getSyncStatus());
      Assertions.assertEquals(expectedStatus, installmentNoPII.getSyncStatus().getSyncStatusTo(), message);
    }
  }

}
