package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
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

    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId()))
      .thenReturn(null);

    // When, Then
    Assertions.assertThrows(NotFoundException.class, () -> service.updateInstallment(installment, receiptDTO, accessToken));
  }

  @Test
  void givenReceiptWithBalanceWhenUpdateInstallmentThenReplaceOnInstallmentBeforeToResolveIt() {
    // Given
    String accessToken = "ACCESSTOKEN";
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    installment.setStatus(InstallmentStatus.UNPAID);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setOrganizationId(-2L);

    Mockito.when(debtPositionTypeOrgRepositoryMock.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId()))
      .thenReturn(dpTypeOrg);

    Mockito.doNothing()
      .when(balanceResolverServiceMock)
      .updateBalanceResolvingAmount(
        Mockito.argThat(i -> {
          Assertions.assertSame(receiptDTO.getBalance(), installment.getBalance());
          return true;
        }),
        Mockito.same(dpTypeOrg.getOrganizationId()), Mockito.same(dpTypeOrg), Mockito.same(accessToken));

    // When
    service.updateInstallment(installment, receiptDTO, accessToken);

    // Then
    Assertions.assertSame(receiptDTO.getReceiptId(), installment.getReceiptId());
    Assertions.assertSame(receiptDTO.getPaymentReceiptId(), installment.getIur());
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
    Assertions.assertNull(installment.getSyncStatus());
    Assertions.assertSame(receiptDTO.getBalance(), installment.getBalance());
  }
}
