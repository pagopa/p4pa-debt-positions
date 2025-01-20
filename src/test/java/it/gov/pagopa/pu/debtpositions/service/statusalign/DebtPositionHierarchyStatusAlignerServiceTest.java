package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.IudSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition.DebtPositionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption.PaymentOptionInnerStatusAlignerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DebtPositionHierarchyStatusAlignerServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private PaymentOptionInnerStatusAlignerService paymentOptionInnerStatusAlignerServiceMock;
  @Mock
  private DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerServiceMock;

  private DebtPositionHierarchyStatusAlignerService service;

  @BeforeEach
  void setUp() {
    service = new DebtPositionHierarchyStatusAlignerServiceImpl(debtPositionRepositoryMock, installmentNoPIIRepositoryMock, paymentOptionInnerStatusAlignerServiceMock, debtPositionInnerStatusAlignerServiceMock);
  }

  @Test
  void givenFinalizeSyncStatusThenOk() {
    Long id = 1L;
    String newStatus = "UNPAID";
    String iupdPagoPa = "iupdPagoPa";
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);
    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatusAndIupdPagopa(id, iupdPagoPa, InstallmentStatus.valueOf(newStatus));
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);

    Map<String, IudSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IudSyncStatusUpdateDTO iudSyncStatusUpdateDTO = IudSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa(iupdPagoPa)
      .build();

    syncStatusDTO.put("iud", iudSyncStatusUpdateDTO);
    service.finalizeSyncStatus(id, syncStatusDTO);

    verify(debtPositionRepositoryMock).findOneWithAllDataByDebtPositionId(id);
    verify(installmentNoPIIRepositoryMock).updateStatusAndIupdPagopa(id, iupdPagoPa, InstallmentStatus.valueOf(newStatus));
    verify(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    verify(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);

    assertEquals(DebtPositionStatus.UNPAID, debtPosition.getStatus());
    assertEquals(PaymentOptionStatus.UNPAID, debtPosition.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.UNPAID, debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
  }

  @Test
  void givenFinalizeSyncStatusWhenIllegalStatusThenThrowInvalidStatusException() {
    Long id = 1L;
    String newStatus = "newStatus";
    String iupdPagoPa = "iupdPagoPa";
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);

    Map<String, IudSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IudSyncStatusUpdateDTO iudSyncStatusUpdateDTO = IudSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa(iupdPagoPa)
      .build();

    syncStatusDTO.put("iud", iudSyncStatusUpdateDTO);

    Exception exception = assertThrows(InvalidStatusException.class, () -> service.finalizeSyncStatus(id, syncStatusDTO));
    assertEquals("Invalid status: newStatus", exception.getMessage());

  }
}
