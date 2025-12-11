package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptTransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.BalanceResolverService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@ExtendWith(MockitoExtension.class)
class OrdinaryInstallmentPaymentHandlerServiceTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private BalanceResolverService balanceResolverServiceMock;

  private OrdinaryInstallmentPaymentHandlerService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    service = new OrdinaryInstallmentPaymentHandlerService(
      debtPositionTypeOrgRepositoryMock,
      balanceResolverServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionTypeOrgRepositoryMock,
      balanceResolverServiceMock);
  }

  @Test
  void givenUnknownDebtPositionTypeOrgWhenUpdateInstallmentThenThrowNotFoundException() {
    // Given
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.UNPAID);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    receiptDTO.setBalance("BALANCE_PRESENT");

    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId()))
      .thenReturn(null);

    // When, Then
    Assertions.assertThrows(NotFoundException.class, () -> service.updateInstallment(installment, receiptDTO, accessToken));
  }

  @Test
  void givenReceiptWithBalanceWhenUpdateInstallmentThenReplaceOnInstallmentBeforeToResolveIt() {
    // Given
    String receiptBalance = "RECEIPTBALANCE";
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.UNPAID);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    receiptDTO.setBalance(receiptBalance);
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setOrganizationId(-2L);

    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId()))
      .thenReturn(dpTypeOrg);

    Mockito.doNothing()
      .when(balanceResolverServiceMock)
      .updateBalanceResolvingAmount(
        Mockito.argThat(i -> {
          Assertions.assertSame(installment, i);
          Assertions.assertSame(receiptBalance, installment.getBalance());
          return true;
        }),
        Mockito.same(dpTypeOrg.getOrganizationId()), Mockito.same(dpTypeOrg), Mockito.same(accessToken));

    // When
    service.updateInstallment(installment, receiptDTO, accessToken);

    // Then
    assertPaymentDataUpdatesAndCommonMockInvocations(receiptDTO, installment, dpTypeOrg, accessToken);
    Assertions.assertSame(receiptBalance, installment.getBalance());
  }

  @Test
  void givenReceiptWithoutBalanceWhenUpdateInstallmentThenSkipResolveBalance() {
    // Given
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.UNPAID);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    receiptDTO.setBalance(null);

    // When
    service.updateInstallment(installment, receiptDTO, accessToken);

    // Then
    Assertions.assertSame(receiptDTO.getReceiptId(), installment.getReceiptId());
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());

    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.never()).getDebtPositionTypeOrgByInstallmentId(Mockito.anyLong());
    Mockito.verify(balanceResolverServiceMock, Mockito.never()).updateBalanceResolvingAmount(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  //region desc=testAmountsUpdates
  @Test
  void givenFeeWhenUpdateInstallmentThenModifyInstallmentAndTransfer1Amounts() {
    testAmountsUpdates(2L);
  }

  @Test
  void givenNoFeeWhenUpdateInstallmentThenNotModifyAmounts() {
    testAmountsUpdates(0L);
  }

  void testAmountsUpdates(long notificationFeeAmounts) {
    // Given
    long installmentAmounts = 13_00;
    long t1Amounts = 6_00;
    long t2Amounts = 7_00;
    long receiptAmountsCents = installmentAmounts + notificationFeeAmounts;

    String accessToken = "ACCESSTOKEN";
    Transfer t1 = podamFactory.manufacturePojo(Transfer.class);
    t1.setTransferIndex(1);
    t1.setAmountCents(t1Amounts);
    Transfer t2 = podamFactory.manufacturePojo(Transfer.class);
    t2.setTransferIndex(2);
    t2.setAmountCents(t2Amounts);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.UNPAID);
    installment.setNotificationFeeCents(0L);
    installment.setAmountCents(installmentAmounts);
    installment.setTransfers(new TreeSet<>(Set.of(t1, t2)));
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    receiptDTO.setBalance("BAL");
    receiptDTO.setPaymentAmountCents(receiptAmountsCents);
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setOrganizationId(-2L);

    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId()))
      .thenReturn(dpTypeOrg);

    // When
    service.updateInstallment(installment, receiptDTO, accessToken);

    // Then
    assertPaymentDataUpdatesAndCommonMockInvocations(receiptDTO, installment, dpTypeOrg, accessToken);

    Assertions.assertEquals(receiptAmountsCents, installment.getAmountCents());
    Assertions.assertEquals(notificationFeeAmounts, installment.getNotificationFeeCents());
    Assertions.assertEquals(t1Amounts + notificationFeeAmounts, t1.getAmountCents());
    Assertions.assertEquals(t2Amounts, t2.getAmountCents());
  }
  //endregion

  @Test
  void givenMdbAttachmentWhenUpdateInstallmentThenSetItOnTransfer1() {
    String accessToken = "ACCESSTOKEN";
    Transfer t1 = podamFactory.manufacturePojo(Transfer.class);
    t1.setTransferIndex(1);
    Transfer t2 = podamFactory.manufacturePojo(Transfer.class);
    t2.setTransferIndex(2);
    t2.setMbdAttachment(null);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.UNPAID);
    installment.setTransfers(new TreeSet<>(Set.of(t1, t2)));
    ReceiptTransferDTO rt1 = podamFactory.manufacturePojo(ReceiptTransferDTO.class);
    rt1.setIdTransfer(1);
    rt1.setMbdAttachment(null);
    ReceiptTransferDTO rt2 = podamFactory.manufacturePojo(ReceiptTransferDTO.class);
    rt2.setIdTransfer(2);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    receiptDTO.setBalance("BAL");
    receiptDTO.setTransfers(List.of(rt1, rt2));
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setOrganizationId(-2L);

    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId()))
      .thenReturn(dpTypeOrg);

    // When
    service.updateInstallment(installment, receiptDTO, accessToken);

    // Then
    assertPaymentDataUpdatesAndCommonMockInvocations(receiptDTO, installment, dpTypeOrg, accessToken);

    Assertions.assertNotNull(t1.getMbdAttachment());
    Assertions.assertSame(rt2.getMbdAttachment(), t2.getMbdAttachment());
  }

  private void assertPaymentDataUpdatesAndCommonMockInvocations(ReceiptWithAdditionalNodeDataDTO receiptDTO, InstallmentNoPII installment, DebtPositionTypeOrg dpTypeOrg, String accessToken) {
    Assertions.assertSame(receiptDTO.getReceiptId(), installment.getReceiptId());
    Assertions.assertSame(receiptDTO.getPaymentReceiptId(), installment.getIur());
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
    Assertions.assertNull(installment.getSyncStatus());

    Mockito.verify(balanceResolverServiceMock)
      .updateBalanceResolvingAmount(
        Mockito.same(installment), Mockito.same(dpTypeOrg.getOrganizationId()), Mockito.same(dpTypeOrg), Mockito.same(accessToken));
  }

  @Test
  void givenNonReceiptFileOriginAndUnpaidInstallmentWhenUpdateInstallmentThenResolveBalance() {
    // Given
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.UNPAID);

    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    receiptDTO.setBalance("BAL");

    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setOrganizationId(-2L);

    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId()))
      .thenReturn(dpTypeOrg);

    // When
    service.updateInstallment(installment, receiptDTO, accessToken);

    // Then
    Assertions.assertSame(receiptDTO.getReceiptId(), installment.getReceiptId());
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
    Assertions.assertEquals("BAL", installment.getBalance());
    Mockito.verify(debtPositionTypeOrgRepositoryMock).getDebtPositionTypeOrgByInstallmentId(Mockito.anyLong());
    Mockito.verify(balanceResolverServiceMock).updateBalanceResolvingAmount(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void givenNonReceiptFileOriginAndPaidInstallmentWhenUpdateInstallmentThenSkipResolveBalance() {
    // Given
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.PAID);

    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    receiptDTO.setBalance("BAL");

    // When
    service.updateInstallment(installment, receiptDTO, accessToken);

    // Then
    Assertions.assertSame(receiptDTO.getReceiptId(), installment.getReceiptId());
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.never()).getDebtPositionTypeOrgByInstallmentId(Mockito.anyLong());
    Mockito.verify(balanceResolverServiceMock, Mockito.never()).updateBalanceResolvingAmount(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  void givenReceiptFileOriginAndPaidInstallmentWhenUpdateInstallmentThenResolveBalance() {
    // Given
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.PAID);

    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_FILE);
    receiptDTO.setBalance("BAL");

    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setOrganizationId(-2L);

    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId()))
      .thenReturn(dpTypeOrg);

    // When
    service.updateInstallment(installment, receiptDTO, accessToken);

    // Then
    Mockito.verify(debtPositionTypeOrgRepositoryMock).getDebtPositionTypeOrgByInstallmentId(Mockito.anyLong());
    Mockito.verify(balanceResolverServiceMock).updateBalanceResolvingAmount(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }
}
