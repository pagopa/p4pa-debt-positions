package it.gov.pagopa.pu.debtpositions.service.create.receipt.utils;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.OrdinaryInstallmentPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.OrdinaryPaidDPHierarchyUpdateService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.UnknownDebtPositionTypeOrgRetrieverService;
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

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PaymentFlowOrchestratorServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private OrdinaryInstallmentPaymentHandlerService ordinaryInstallmentPaymentHandlerServiceMock;
  @Mock
  private UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverServiceMock;
  @Mock
  private OrdinaryPaidDPHierarchyUpdateService hierarchyUpdateServiceMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;

  private PaymentFlowOrchestratorService service;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    service = new PaymentFlowOrchestratorService(
      debtPositionRepositoryMock,
      installmentNoPIIRepositoryMock,
      ordinaryInstallmentPaymentHandlerServiceMock,
      debtPositionTypeOrgRepositoryMock,
      unknownDebtPositionTypeOrgRetrieverServiceMock,
      ordinaryInstallmentPaymentHandlerServiceMock,
      hierarchyUpdateServiceMock,
      debtPositionServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionRepositoryMock,
      debtPositionTypeOrgRepositoryMock,
      installmentNoPIIRepositoryMock,
      ordinaryInstallmentPaymentHandlerServiceMock,
      unknownDebtPositionTypeOrgRetrieverServiceMock,
      hierarchyUpdateServiceMock,
      debtPositionServiceMock
    );
  }

  @Test
  void updateBalanceAndMeta_WhenDpIsUnknownAndCodeFound_ThenUpdatesToSpecificAndResolves() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setBalance("NEW_BALANCE");

    DebtPositionTypeOrg unknownTypeOrg = new DebtPositionTypeOrg();
    unknownTypeOrg.setDebtPositionTypeOrgId(100L);
    dp.setDebtPositionTypeOrgId(100L);
    DebtPositionTypeOrg specificTypeOrg = new DebtPositionTypeOrg();
    specificTypeOrg.setDebtPositionTypeOrgId(200L);

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(dp.getOrganizationId()))
      .thenReturn(unknownTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(dp.getOrganizationId(), receiptDTO.getDebtPositionTypeOrgCode()))
      .thenReturn(Optional.of(specificTypeOrg));

    // When
    service.updateBalanceAndMeta(dp, installment, receiptDTO, accessToken);

    // Then
    Assertions.assertEquals(200L, dp.getDebtPositionTypeOrgId());
    Assertions.assertEquals("NEW_BALANCE", installment.getBalance());

    Mockito.verify(debtPositionRepositoryMock).updateDebtPositionTypeOrgId(dp.getDebtPositionId(), 200L);
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, specificTypeOrg, receiptDTO.getPaymentDateTime(), accessToken);
    Mockito.verify(installmentNoPIIRepositoryMock).updateBalance(installment.getInstallmentId(), "NEW_BALANCE");
  }

  @Test
  void updateBalanceAndMeta_WhenDpIsUnknownAndCodeNotFound_ThenResolvesWithUnknown() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setBalance("NEW_BALANCE");

    DebtPositionTypeOrg unknownTypeOrg = new DebtPositionTypeOrg();
    unknownTypeOrg.setDebtPositionTypeOrgId(100L);
    dp.setDebtPositionTypeOrgId(100L);

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(dp.getOrganizationId()))
      .thenReturn(unknownTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(dp.getOrganizationId(), receiptDTO.getDebtPositionTypeOrgCode()))
      .thenReturn(Optional.empty());

    // When
    service.updateBalanceAndMeta(dp, installment, receiptDTO, accessToken);

    // Then
    Assertions.assertEquals(100L, dp.getDebtPositionTypeOrgId());
    Assertions.assertEquals("NEW_BALANCE", installment.getBalance());
    Mockito.verify(debtPositionRepositoryMock, Mockito.never()).updateDebtPositionTypeOrgId(Mockito.anyLong(), Mockito.anyLong());
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, unknownTypeOrg, receiptDTO.getPaymentDateTime(), accessToken);
    Mockito.verify(installmentNoPIIRepositoryMock).updateBalance(installment.getInstallmentId(), "NEW_BALANCE");
  }

  @Test
  void updateBalanceAndMeta_WhenDpIsAlreadySpecific_ThenResolvesWithoutTypeOrgArg() {
    // Given
    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    InstallmentNoPII installment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    receiptDTO.setBalance("NEW_BALANCE");

    DebtPositionTypeOrg unknownTypeOrg = new DebtPositionTypeOrg();
    unknownTypeOrg.setDebtPositionTypeOrgId(100L);
    dp.setDebtPositionTypeOrgId(500L);

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(dp.getOrganizationId()))
      .thenReturn(unknownTypeOrg);

    // When
    service.updateBalanceAndMeta(dp, installment, receiptDTO, accessToken);

    // Then
    Assertions.assertEquals("NEW_BALANCE", installment.getBalance());

    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.never()).findByOrganizationIdAndCode(Mockito.any(), Mockito.any());
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).resolveBalance(installment, receiptDTO.getPaymentDateTime(), accessToken);
    Mockito.verify(installmentNoPIIRepositoryMock).updateBalance(installment.getInstallmentId(), "NEW_BALANCE");
  }

  @Test
  void performStandardUpdate_InvokesHandlersAndSaves() {
    // Given
    DebtPosition dp = new DebtPosition();
    InstallmentNoPII installment = new InstallmentNoPII();
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();

    // When
    service.performStandardUpdate(dp, installment, receiptDTO, accessToken);

    // Then
    Mockito.verify(ordinaryInstallmentPaymentHandlerServiceMock).updateInstallment(installment, receiptDTO, accessToken);
    Mockito.verify(hierarchyUpdateServiceMock).updateHierarchy(dp, installment);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(dp);
  }

  @Test
  void resolveDebtPositionTypeOrgId_WhenStoredIsSpecific_ThenReturnsStoredId() {
    // Given
    Long orgId = 1L;
    String code = "CODE";
    Long currentId = 500L;

    DebtPositionTypeOrg unknownTypeOrg = new DebtPositionTypeOrg();
    unknownTypeOrg.setDebtPositionTypeOrgId(100L);

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(orgId))
      .thenReturn(unknownTypeOrg);

    // When
    Long result = service.resolveDebtPositionTypeOrgId(orgId, code, currentId);

    // Then
    Assertions.assertEquals(currentId, result);
    Mockito.verifyNoInteractions(debtPositionTypeOrgRepositoryMock);
  }

  @Test
  void resolveDebtPositionTypeOrgId_WhenStoredIsUnknownAndCodeFound_ThenReturnsFoundId() {
    // Given
    Long orgId = 1L;
    String code = "CODE";
    Long currentId = 100L;

    DebtPositionTypeOrg unknownTypeOrg = new DebtPositionTypeOrg();
    unknownTypeOrg.setDebtPositionTypeOrgId(100L);

    DebtPositionTypeOrg foundTypeOrg = new DebtPositionTypeOrg();
    foundTypeOrg.setDebtPositionTypeOrgId(200L);

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(orgId))
      .thenReturn(unknownTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(orgId, code))
      .thenReturn(Optional.of(foundTypeOrg));

    // When
    Long result = service.resolveDebtPositionTypeOrgId(orgId, code, currentId);

    // Then
    Assertions.assertEquals(200L, result);
  }

  @Test
  void resolveDebtPositionTypeOrgId_WhenStoredIsNullAndCodeFound_ThenReturnsFoundId() {
    // Given
    Long orgId = 1L;
    String code = "CODE";
    Long currentId = null;

    DebtPositionTypeOrg unknownTypeOrg = new DebtPositionTypeOrg();
    unknownTypeOrg.setDebtPositionTypeOrgId(100L);

    DebtPositionTypeOrg foundTypeOrg = new DebtPositionTypeOrg();
    foundTypeOrg.setDebtPositionTypeOrgId(200L);

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(orgId))
      .thenReturn(unknownTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(orgId, code))
      .thenReturn(Optional.of(foundTypeOrg));

    // When
    Long result = service.resolveDebtPositionTypeOrgId(orgId, code, currentId);

    // Then
    Assertions.assertEquals(200L, result);
  }

  @Test
  void resolveDebtPositionTypeOrgId_WhenCodeNotFound_ThenReturnsUnknownId() {
    // Given
    Long orgId = 1L;
    String code = "CODE";
    Long currentId = null;

    DebtPositionTypeOrg unknownTypeOrg = new DebtPositionTypeOrg();
    unknownTypeOrg.setDebtPositionTypeOrgId(100L);

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(orgId))
      .thenReturn(unknownTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(orgId, code))
      .thenReturn(Optional.empty());

    // When
    Long result = service.resolveDebtPositionTypeOrgId(orgId, code, currentId);

    // Then
    Assertions.assertEquals(100L, result);
  }
}
