package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.utils.PaymentFlowOrchestratorService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReceiptBasedTechnicalDpHandlerServiceTest {

  @Mock
  private ReceiptWithAdditionalInfoMapper receiptMapperMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private UpdateAndSynchronizeTechDp updateAndSynchronizeTechDpMock;
  @Mock
  private PaymentFlowOrchestratorService paymentFlowOrchestratorServiceMock;
  @Mock
  private TechnicalDpUpdateService technicalDpUpdateServiceMock;

  private ReceiptBasedTechnicalDpHandlerService service;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init(){
    service = new ReceiptBasedTechnicalDpHandlerService(
      receiptMapperMock,
      debtPositionServiceMock,
      updateAndSynchronizeTechDpMock,
      paymentFlowOrchestratorServiceMock,
      technicalDpUpdateServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      receiptMapperMock,
      debtPositionServiceMock,
      updateAndSynchronizeTechDpMock,
      paymentFlowOrchestratorServiceMock,
      technicalDpUpdateServiceMock
    );
  }

  @Test
  void whenCreateAndPublishTechDpThenResolveIdMapSaveAndDelegatePublish() {
    // Given
    Organization organization = new Organization();
    organization.setOrganizationId(1L);
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setDebtPositionTypeOrgCode("CODE");

    Long resolvedId = 100L;
    DebtPositionDTO dpDto = new DebtPositionDTO();
    DebtPosition expectedResult = new DebtPosition();

    Mockito.when(paymentFlowOrchestratorServiceMock.resolveDebtPositionTypeOrgId(1L, "CODE", null))
      .thenReturn(resolvedId);

    Mockito.when(receiptMapperMock.mapToDebtPosition(Mockito.same(receiptDTO), Mockito.same(organization), Mockito.eq(resolvedId)))
      .thenReturn(dpDto);

    Mockito.when(updateAndSynchronizeTechDpMock.publishTechDp(Mockito.same(dpDto), Mockito.same(receiptDTO)))
      .thenReturn(expectedResult);

    // When
    DebtPosition result = service.createAndPublishTechDp(organization, receiptDTO);

    // Then
    Assertions.assertSame(expectedResult, result);

    Mockito.verify(debtPositionServiceMock).saveDebtPosition(Mockito.same(dpDto));
  }

  @Test
  void whenUpdateAndPublishTechDpWith5ArgsThenDelegateToUpdateAndSyncService() {
    // Given
    Organization organization = new Organization();
    DebtPosition dp = new DebtPosition();
    InstallmentNoPII installment = new InstallmentNoPII();
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    DebtPosition expectedResult = new DebtPosition();

    Mockito.when(updateAndSynchronizeTechDpMock.handleTechDpAlreadyPaid(
      Mockito.same(installment),
      Mockito.same(receiptDTO),
      Mockito.same(dp),
      Mockito.same(organization),
      Mockito.same(accessToken)
    )).thenReturn(expectedResult);

    // When
    DebtPosition result = service.updateAndPublishTechDp(organization, dp, installment, receiptDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenUpdateAndPublishTechDpWith3ArgsThenResolveIdUpdateAndPublish() {
    // Given
    Organization organization = new Organization();
    organization.setOrganizationId(1L);
    DebtPosition dp = new DebtPosition();
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setDebtPositionTypeOrgCode("CODE");

    Long resolvedId = 200L;
    DebtPositionDTO dpDto = new DebtPositionDTO();

    Mockito.when(paymentFlowOrchestratorServiceMock.resolveDebtPositionTypeOrgId(1L, "CODE", null))
      .thenReturn(resolvedId);

    Mockito.when(technicalDpUpdateServiceMock.updateDp(Mockito.same(dp), Mockito.same(receiptDTO), Mockito.same(organization), Mockito.eq(resolvedId)))
      .thenReturn(dpDto);

    // When
    service.updateAndPublishTechDp(organization, dp, receiptDTO);

    // Then
    Mockito.verify(updateAndSynchronizeTechDpMock).publishTechDp(Mockito.same(dpDto), Mockito.same(receiptDTO));
  }
}
