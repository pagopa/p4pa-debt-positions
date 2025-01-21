package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
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

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
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
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  private DebtPositionHierarchyStatusAlignerService service;

  @BeforeEach
  void setUp() {
    service = new DebtPositionHierarchyStatusAlignerServiceImpl(debtPositionRepositoryMock, installmentNoPIIRepositoryMock, paymentOptionInnerStatusAlignerServiceMock, debtPositionInnerStatusAlignerServiceMock, debtPositionMapperMock);
  }

  @Test
  void givenFinalizeSyncStatusThenOk() {
    Long id = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    String iupdPagoPa = "iupdPagoPa";
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);
    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatusAndIupdPagopa(id, iupdPagoPa, newStatus);
    Mockito.doNothing().when(paymentOptionInnerStatusAlignerServiceMock).updatePaymentOptionStatus(buildPaymentOption());
    Mockito.doNothing().when(debtPositionInnerStatusAlignerServiceMock).updateDebtPositionStatus(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(buildDebtPositionDTO());

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa(iupdPagoPa)
      .build();

    syncStatusDTO.put("iud", iupdSyncStatusUpdateDTO);
    DebtPositionDTO result = service.finalizeSyncStatus(id, syncStatusDTO);

    assertEquals(DebtPositionStatus.UNPAID, result.getStatus());
    assertEquals(PaymentOptionStatus.UNPAID, result.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.UNPAID, result.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    reflectionEqualsByName(buildDebtPositionDTO(), result);
  }

  @Test
  void givenFinalizeSyncStatusWhenIsNotSyncThenDoNotUpdateStatus() {
    Long id = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    String iupdPagoPa = "iupdPagoPa";
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.PAID);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa(iupdPagoPa)
      .build();

    syncStatusDTO.put("iud", iupdSyncStatusUpdateDTO);

    DebtPositionDTO result = service.finalizeSyncStatus(id, syncStatusDTO);

    assertNull(result);
    verify(debtPositionRepositoryMock).findOneWithAllDataByDebtPositionId(id);
    verify(installmentNoPIIRepositoryMock, times(0)).updateStatusAndIupdPagopa(id, iupdPagoPa, newStatus);
  }

  @Test
  void givenFinalizeSyncStatusWhenDoesNotHaveIudThenDoNotUpdateStatus() {
    Long id = 1L;
    InstallmentStatus newStatus = InstallmentStatus.UNPAID;
    String iupdPagoPa = "iupdPagoPa";
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa(iupdPagoPa)
      .build();

    syncStatusDTO.put("fake-iud", iupdSyncStatusUpdateDTO);

    DebtPositionDTO result = service.finalizeSyncStatus(id, syncStatusDTO);

    assertNull(result);
    verify(debtPositionRepositoryMock).findOneWithAllDataByDebtPositionId(id);
    verify(installmentNoPIIRepositoryMock, times(0)).updateStatusAndIupdPagopa(id, iupdPagoPa, newStatus);
  }
}
