package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.BalanceFetchService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;

@Slf4j
@ExtendWith(MockitoExtension.class)
class InstallmentUpdateServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private BalanceService balanceServiceMock;
  @Mock
  private BalanceFetchService balanceFetchServiceMock;

  @InjectMocks
  private InstallmentUpdateService installmentUpdateService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private static final String ACCESS_TOKEN = "accessToken";

  @Test
  void givenFoundDebtPositionWhenUpdateInstallmentStatusOfDebtPositionThenOk() {
    //given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
      InstallmentStatus.UNPAID);
    targetInstallment.setBalance(null);
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    debtPosition.setStatus(DebtPositionStatus.UNPAID);

    PaymentOption po0 = PaymentOptionFaker.buildPaymentOption();
    po0.setPaymentOptionIndex(1);
    po0.setStatus(PaymentOptionStatus.PAID);
    po0.setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
        InstallmentStatus.PAID),
      targetInstallment
    )));

    PaymentOption po1 = PaymentOptionFaker.buildPaymentOption();
    po1.setStatus(PaymentOptionStatus.TO_SYNC);
    po1.setPaymentOptionIndex(2);
    po1.setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
        InstallmentStatus.EXPIRED),
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallmentToSync(
        InstallmentStatus.UNPAID, InstallmentStatus.UNPAID),
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
        InstallmentStatus.CANCELLED)
    )));

    PaymentOption po2 = PaymentOptionFaker.buildPaymentOption();
    po2.setStatus(PaymentOptionStatus.UNPAID);
    po2.setPaymentOptionIndex(3);
    po2.setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
        InstallmentStatus.UNPAID)
    )));

    PaymentOption po3 = PaymentOptionFaker.buildPaymentOption();
    po3.setPaymentOptionIndex(4);
    po3.setStatus(PaymentOptionStatus.CANCELLED);
    po3.setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
        InstallmentStatus.CANCELLED)
    )));

    PaymentOption po4 = PaymentOptionFaker.buildPaymentOption();
    po4.setPaymentOptionIndex(5);
    po4.setStatus(PaymentOptionStatus.TO_SYNC);
    po4.setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
        InstallmentStatus.CANCELLED),
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallmentToSync(
        InstallmentStatus.UNPAID, InstallmentStatus.CANCELLED)
    )));

    debtPosition.setPaymentOptions(new TreeSet<>(Set.of(po0, po1, po2, po3, po4)));

    //align entities id
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(debtPosition.getDebtPositionId());
      paymentOption.getInstallments().forEach(
        anInstallment -> anInstallment.setPaymentOptionId(
          paymentOption.getPaymentOptionId()));
    });

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(
      targetInstallment.getInstallmentId())).thenReturn(debtPosition);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setBalance(null);
    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(targetInstallment.getInstallmentId()))
      .thenReturn(debtPositionTypeOrg);
    Mockito.when(balanceFetchServiceMock.getBalanceDefault(debtPosition.getOrganizationId(), debtPositionTypeOrg, ACCESS_TOKEN))
      .thenReturn(null);

    //when
    DebtPosition response = installmentUpdateService.updateInstallmentStatusOfDebtPosition(
      targetInstallment, receiptDTO, ACCESS_TOKEN);

    //verify
    Assertions.assertEquals(debtPosition, response);
    //find target installment in response
    InstallmentNoPII responseTargetInstallment = response.getPaymentOptions()
      .stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(anInstallment -> anInstallment.getInstallmentId()
        .equals(targetInstallment.getInstallmentId()))
      .findFirst().orElse(null);
    Assertions.assertNotNull(responseTargetInstallment);
    //verify that the target installment has been updated to (TO_SYNC->)PAID
    Assertions.assertEquals(InstallmentStatus.PAID, responseTargetInstallment.getStatus(), "target installment status");
    //verify that there no more NOT-PAID installments on payment options different of the one of the target installment
    int[] idxPo = {0};
    int[] idxInst = {0};
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      if (!paymentOption.getPaymentOptionId()
        .equals(targetInstallment.getPaymentOptionId())) {
        paymentOption.getInstallments().forEach(anInstallment -> {
          if (InstallmentUtils.isPayable(anInstallment)) {
            String message = "Installment[%s][%s] of payment option[%s][%s] is [%s]".formatted(
              idxInst[0], anInstallment.getInstallmentId(), idxPo[0],
              paymentOption.getPaymentOptionId(),
              anInstallment.getStatus());

            Assertions.assertEquals(InstallmentStatus.TO_SYNC, anInstallment.getStatus(), message);
            Assertions.assertEquals(InstallmentStatus.INVALID, anInstallment.getSyncStatus().getSyncStatusTo(), message + " syncStatusTo " + anInstallment.getSyncStatus().getSyncStatusTo());
          }
          idxInst[0]++;
        });
      }
      idxPo[0]++;
    });
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1))
      .findEntityGraphByInstallmentId(targetInstallment.getInstallmentId());
    Mockito.verify(organizationServiceMock, Mockito.times(0))
      .getOrganizationById(debtPosition.getOrganizationId(), ACCESS_TOKEN);
    Mockito.verify(balanceServiceMock, Mockito.times(0))
      .calculateAmountBalance(Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionWithBalanceNullWhenDebtPositionTypeOrgNotFoundThenException() {
    //given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
      InstallmentStatus.UNPAID);
    targetInstallment.setBalance(null);
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    debtPosition.setStatus(DebtPositionStatus.UNPAID);

    PaymentOption po0 = PaymentOptionFaker.buildPaymentOption();
    po0.setPaymentOptionIndex(1);
    po0.setStatus(PaymentOptionStatus.PAID);
    po0.setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
        InstallmentStatus.PAID),
      targetInstallment
    )));

    debtPosition.setPaymentOptions(new TreeSet<>(Set.of(po0)));

    //align entities id
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(debtPosition.getDebtPositionId());
      paymentOption.getInstallments().forEach(
        anInstallment -> anInstallment.setPaymentOptionId(
          paymentOption.getPaymentOptionId()));
    });

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(
      targetInstallment.getInstallmentId())).thenReturn(debtPosition);
    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(targetInstallment.getInstallmentId()))
      .thenReturn(null);

    NotFoundException response = Assertions.assertThrows(NotFoundException.class,
      () -> installmentUpdateService.updateInstallmentStatusOfDebtPosition(targetInstallment, receiptDTO, ACCESS_TOKEN));

    Assertions.assertEquals("The DebtPositionTypeOrg for installment with id " + targetInstallment.getInstallmentId() + " was not found", response.getMessage());

    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1))
      .findEntityGraphByInstallmentId(targetInstallment.getInstallmentId());
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.times(1))
      .getDebtPositionTypeOrgByInstallmentId(targetInstallment.getInstallmentId());
    Mockito.verify(organizationServiceMock, Mockito.times(0))
      .getOrganizationById(debtPosition.getOrganizationId(), ACCESS_TOKEN);
    Mockito.verify(balanceServiceMock, Mockito.times(0))
      .calculateAmountBalance(Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenReceiptWithGreaterPaymentAmountWhenUpdateInstallmentThenAmountsAreUpdatedForFee() {
    // Given
    long installmentAmount = 1000L;
    long paymentAmount = 1200L;
    long feeAmount = paymentAmount - installmentAmount; // = 200

    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setPaymentReceiptId("RECEIPTID");
    receiptDTO.setPaymentAmountCents(paymentAmount);

    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
      InstallmentStatus.UNPAID);
    targetInstallment.setAmountCents(installmentAmount);

    // Transfer with transferIndex=1 (it will be changed with fee)
    Transfer transferWithIndex1 = new Transfer();
    transferWithIndex1.setTransferIndex(1);
    transferWithIndex1.setOrgFiscalCode("12345678901");
    transferWithIndex1.setAmountCents(installmentAmount);

    // Other transfer (untouched)
    Transfer otherTransfer = new Transfer();
    otherTransfer.setTransferIndex(0);
    otherTransfer.setOrgFiscalCode("otherFiscalCode");
    otherTransfer.setAmountCents(500L);

    // Add transfers
    SortedSet<Transfer> transfers = new TreeSet<>(Comparator.comparingInt(Transfer::getTransferIndex));
    transfers.add(otherTransfer);
    transfers.add(transferWithIndex1);
    targetInstallment.setTransfers(transfers);

    // Payment Option with target installment
    PaymentOption po0 = PaymentOptionFaker.buildPaymentOption();
    po0.setPaymentOptionIndex(1);
    po0.setStatus(PaymentOptionStatus.PAID);
    po0.setInstallments(new TreeSet<>(List.of(
      PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(
        InstallmentStatus.PAID),
      targetInstallment
    )));

    // Debt position
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);
    debtPosition.setPaymentOptions(new TreeSet<>(List.of(po0)));

    //align entities id
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(debtPosition.getDebtPositionId());
      paymentOption.getInstallments().forEach(
        anInstallment -> anInstallment.setPaymentOptionId(
          paymentOption.getPaymentOptionId()));
    });

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(targetInstallment.getInstallmentId()))
      .thenReturn(debtPosition);
    Mockito.when(organizationServiceMock.getOrganizationById(debtPosition.getOrganizationId(), ACCESS_TOKEN))
      .thenReturn(Optional.ofNullable(buildOrganization()));
    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance(targetInstallment.getBalance()).amountCents(1200L).remittanceInformation(targetInstallment.getRemittanceInformation()).build();
    Mockito.when(balanceServiceMock.calculateAmountBalance(amountBalanceRequest, ACCESS_TOKEN)).thenReturn("balanceResolved");

    // When
    DebtPosition response = installmentUpdateService.updateInstallmentStatusOfDebtPosition(
      targetInstallment, receiptDTO, ACCESS_TOKEN);

    // Then
    InstallmentNoPII updatedInstallment = response.getPaymentOptions().getFirst()
      .getInstallments().getFirst();
    Assertions.assertEquals(InstallmentStatus.PAID, updatedInstallment.getStatus(), "status must be PAID");
    Assertions.assertEquals(receiptDTO.getReceiptId(), updatedInstallment.getReceiptId(), "set receiptId");
    Assertions.assertEquals(receiptDTO.getPaymentReceiptId(), updatedInstallment.getIur(), "set iur");
    Assertions.assertEquals(feeAmount, updatedInstallment.getNotificationFeeCents(), "set notificationFeeCents");
    Assertions.assertEquals(installmentAmount + feeAmount, updatedInstallment.getAmountCents(), "update amountCents");
    Assertions.assertEquals("balanceResolved", updatedInstallment.getBalance());

    // test Transfer with transferIndex=1 updated
    Transfer updatedTransfer = updatedInstallment.getTransfers().stream()
      .filter(t -> t.getTransferIndex() == 1)
      .findFirst().orElseThrow(() -> new AssertionError("Transfer with transferIndex=1 missing"));
    Assertions.assertEquals(installmentAmount + feeAmount, updatedTransfer.getAmountCents(), "update Transfer amountCents with fee");

    // test untouched Transfer with index 0
    Transfer untouched = updatedInstallment.getTransfers().stream()
      .filter(t -> t.getTransferIndex() == 0)
      .findFirst().orElseThrow(() -> new AssertionError("transfer 0 missing"));
    Assertions.assertEquals(500L, untouched.getAmountCents());
  }

  @Test
  void givenNotFoundFoundDebtPositionWhenUpdateInstallmentStatusOfDebtPositionThenException(){
    //given
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.UNPAID);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(targetInstallment.getInstallmentId())).thenReturn(null);
    //when
    NotFoundException response = Assertions.assertThrows(NotFoundException.class, () -> installmentUpdateService.updateInstallmentStatusOfDebtPosition(targetInstallment, receiptDTO, ACCESS_TOKEN));
    //verify
    Assertions.assertTrue(response.getMessage().startsWith("debt position not found"));
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1)).findEntityGraphByInstallmentId(targetInstallment.getInstallmentId());
  }

  @Test
  void givenNotFoundFoundInstallmentWhenUpdateInstallmentStatusOfDebtPositionThenException(){
    //given
    InstallmentNoPII targetInstallment = PrimaryOrgInstallmentPaidVerifierServiceTest.getInstallment(InstallmentStatus.UNPAID);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
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
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(targetInstallment.getInstallmentId())).thenReturn(debtPosition);
    //when
    NotFoundException response = Assertions.assertThrows(NotFoundException.class, () -> installmentUpdateService.updateInstallmentStatusOfDebtPosition(targetInstallment, receiptDTO, ACCESS_TOKEN));
    //verify
    Assertions.assertTrue(response.getMessage().startsWith("primary installment not found"));
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1)).findEntityGraphByInstallmentId(targetInstallment.getInstallmentId());
  }
}
