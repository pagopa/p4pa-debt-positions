package it.gov.pagopa.pu.debtpositions.service.update.massive;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class MassiveUpdateServiceImplTest {
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerServiceMock;
  @Mock
  private DebtPositionSyncService debtPositionSyncServiceMock;

  private MassiveUpdateService massiveUpdateService;

  @BeforeEach
  void setUp() {
    massiveUpdateService = new MassiveUpdateServiceImpl(
      debtPositionServiceMock,
      debtPositionHierarchyStatusAlignerServiceMock,
      debtPositionSyncServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      debtPositionServiceMock,
      debtPositionHierarchyStatusAlignerServiceMock,
      debtPositionSyncServiceMock
    );
  }

  @Test
  void givenNonExistentDebtPositionIdWhenUpdateTransferIbansThenThrowsException() {
    Long nonExistentDebtPositionId = 1L;

    Mockito.when(debtPositionServiceMock.getDebtPosition(nonExistentDebtPositionId)).thenReturn(null);

    Assertions.assertThrows(NotFoundException.class, () ->
      massiveUpdateService.updateTransferIbansAndSyncDebtPosition(
        nonExistentDebtPositionId, "oldIban", "newIban", "oldPostalIban", "newPostalIban", "token"
      )
    );
  }

  @Test
  void givenMatchingTransferIbansWhenUpdateTransferIbansThenUpdatesAndSyncs() {
    String oldIban = "oldIban";
    String newIban = "newIban";
    String oldPostalIban = "oldPostalIban";
    String newPostalIban = "newPostalIban";
    String accessToken = "accessToken";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    String expectedIud = installmentDTO.getIud();

    TransferDTO transferDTO = installmentDTO.getTransfers().getFirst();
    transferDTO.setIban(oldIban);
    transferDTO.setPostalIban(oldPostalIban);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionDTO.getDebtPositionId())).thenReturn(debtPositionDTO);

    massiveUpdateService.updateTransferIbansAndSyncDebtPosition(
      debtPositionDTO.getDebtPositionId(), oldIban, newIban, oldPostalIban, newPostalIban, accessToken
    );

    Assertions.assertEquals(newIban, transferDTO.getIban());
    Assertions.assertEquals(newPostalIban, transferDTO.getPostalIban());

    Mockito.verify(debtPositionServiceMock).getDebtPosition(debtPositionDTO.getDebtPositionId());
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionSyncServiceMock).syncDebtPosition(
      Mockito.eq(debtPositionDTO),
      Mockito.any(WfExecutionParameters.class),
      Mockito.eq(PaymentEventType.DPI_UPDATED),
      Mockito.eq("IUD: " + expectedIud),
      Mockito.eq(accessToken)
    );
  }

  @Test
  void givenNoMatchingTransferIbansWhenUpdateTransferIbansThenNotUpdateAndNotSync() {
    String oldIban = "oldIban";
    String newIban = "newIban";
    String oldPostalIban = "oldPostalIban";
    String newPostalIban = "newPostalIban";
    String accessToken = "accessToken";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    TransferDTO transferDTO = installmentDTO.getTransfers().getFirst();
    transferDTO.setIban("differentIban");
    transferDTO.setPostalIban("differentPostalIban");

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionDTO.getDebtPositionId())).thenReturn(debtPositionDTO);

    massiveUpdateService.updateTransferIbansAndSyncDebtPosition(
      debtPositionDTO.getDebtPositionId(), oldIban, newIban, oldPostalIban, newPostalIban, accessToken
    );

    Mockito.verify(debtPositionServiceMock).getDebtPosition(debtPositionDTO.getDebtPositionId());
  }
}
