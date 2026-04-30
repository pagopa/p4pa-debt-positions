package it.gov.pagopa.pu.debtpositions.service.create.receipt.mixed;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.UpdateAndSynchronizeTechDp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class MixedDpPaymentHandlerServiceTest {

  @Mock
  private TechnicalMixedDebtPositionUpdaterService technicalMixedDebtPositionUpdaterServiceMock;
  @Mock
  private DebtPositionMapper mapperMock;
  @Mock
  private UpdateAndSynchronizeTechDp updateAndSynchronizeTechDpMock;

  private MixedDpPaymentHandlerService service;

  @BeforeEach
  void init() {
    service = new MixedDpPaymentHandlerService(
      technicalMixedDebtPositionUpdaterServiceMock,
      mapperMock,
      updateAndSynchronizeTechDpMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      technicalMixedDebtPositionUpdaterServiceMock,
      mapperMock,
      updateAndSynchronizeTechDpMock);
  }

  @Test
  void givenNoMixedTypeWhenHandleThenDoNothing(){
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPosition primaryOrgDp = new DebtPosition();
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();

    Mockito.when(technicalMixedDebtPositionUpdaterServiceMock.update(Mockito.same(primaryOrgDp), Mockito.same(accessToken)))
      .thenReturn(List.of());

    // When
    service.handle(primaryOrgDp, receiptDTO, accessToken);

    // Then
    Mockito.verifyNoInteractions(mapperMock, updateAndSynchronizeTechDpMock);
  }

  @Test
  void givenMixedTypeWhenHandleThenDoNothing(){
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPosition primaryOrgDp = new DebtPosition();
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();

    DebtPositionDTO techDpMixed1DTO = new DebtPositionDTO();
    DebtPositionDTO techDpMixed2DTO = new DebtPositionDTO();

    List<DebtPosition> dps = List.of(new DebtPosition(), new DebtPosition());
    Mockito.when(technicalMixedDebtPositionUpdaterServiceMock.update(Mockito.same(primaryOrgDp), Mockito.same(accessToken)))
      .thenReturn(dps);
    Mockito.when(mapperMock.mapAllToDto(Mockito.same(dps)))
      .thenReturn(List.of(techDpMixed1DTO, techDpMixed2DTO));

    // When
    service.handle(primaryOrgDp, receiptDTO, accessToken);

    // Then
    Mockito.verify(updateAndSynchronizeTechDpMock)
      .publishTechDp(Mockito.same(techDpMixed1DTO), Mockito.same(receiptDTO));
    Mockito.verify(updateAndSynchronizeTechDpMock)
      .publishTechDp(Mockito.same(techDpMixed2DTO), Mockito.same(receiptDTO));
  }
}
