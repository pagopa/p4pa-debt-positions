package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IudSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallment;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransfer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceImplTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private PaymentOptionRepository paymentOptionRepositoryMock;
  @Mock
  private InstallmentPIIRepository installmentRepositoryMock;
  @Mock
  private TransferRepository transferRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  private DebtPositionService service;

  @BeforeEach
  void setUp(){
    service = new DebtPositionServiceImpl(debtPositionRepositoryMock, paymentOptionRepositoryMock, installmentRepositoryMock, transferRepositoryMock, debtPositionMapperMock, installmentNoPIIRepositoryMock);
  }

  @Test
  void givenValidDebtPositionDTO_WhenSaveDebtPosition_ThenSaveAllEntities() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    DebtPosition debtPosition = buildDebtPosition();
    PaymentOption paymentOption = buildPaymentOption();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    Installment installment = buildInstallment();
    Transfer transfer = buildTransfer();

    Map<InstallmentNoPII, Installment> installmentMap = Map.of(installmentNoPII, installment);
    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedPair = Pair.of(debtPosition, installmentMap);

    DebtPosition savedDebtPosition = new DebtPosition();
    savedDebtPosition.setDebtPositionId(1L);
    savedDebtPosition.setPaymentOptions(new TreeSet<>(List.of(paymentOption)));

    PaymentOption savedPaymentOption = new PaymentOption();
    savedPaymentOption.setPaymentOptionId(1L);
    savedPaymentOption.setInstallments(new TreeSet<>(List.of(installmentNoPII)));

    Installment savedInstallment = new Installment();
    savedInstallment.setInstallmentId(1L);

    Transfer savedTransfer = new Transfer();
    savedTransfer.setTransferId(1L);

    Mockito.when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(mappedPair);
    Mockito.when(debtPositionRepositoryMock.save(Mockito.any(DebtPosition.class))).thenReturn(savedDebtPosition);
    Mockito.when(paymentOptionRepositoryMock.save(Mockito.any(PaymentOption.class))).thenReturn(savedPaymentOption);
    Mockito.when(installmentRepositoryMock.save(Mockito.any(Installment.class))).thenReturn(savedInstallment.getInstallmentId());
    Mockito.when(transferRepositoryMock.save(Mockito.any(Transfer.class))).thenReturn(savedTransfer);

    service.saveDebtPosition(debtPositionDTO);

    verify(debtPositionRepositoryMock, Mockito.times(1)).save(debtPosition);
    verify(paymentOptionRepositoryMock, Mockito.times(1)).save(paymentOption);
    verify(installmentRepositoryMock, Mockito.times(1)).save(installment);
    verify(transferRepositoryMock, Mockito.times(1)).save(transfer);
  }

  @Test
  void givenFinalizeSyncStatusThenOk(){
    // Given
    Long id = 1L;
    String newStatus = "UNPAID";
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);

    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatus(id, newStatus);

    Mockito.doNothing().when(paymentOptionRepositoryMock).updateStatus(id, newStatus);

    Mockito.doNothing().when(debtPositionRepositoryMock).updateStatus(id, newStatus);

    Map<String, IudSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IudSyncStatusUpdateDTO iudSyncStatusUpdateDTO = IudSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa("iupdPagoPa")
      .build();

    syncStatusDTO.put("iud", iudSyncStatusUpdateDTO);

    // When
    service.finalizeSyncStatus(id, syncStatusDTO);

    // Then
    verify(debtPositionRepositoryMock).findOneWithAllDataByDebtPositionId(id);
    verify(installmentNoPIIRepositoryMock).updateStatus(id, newStatus);
    verify(paymentOptionRepositoryMock).updateStatus(id, newStatus);
    verify(debtPositionRepositoryMock).updateStatus(id, newStatus);

    assertEquals(newStatus, debtPosition.getStatus());
    assertEquals(newStatus, debtPosition.getPaymentOptions().getFirst().getStatus());
    assertEquals(newStatus, debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
  }

  @Test
  void givenFinalizeSyncStatusWhenDifferentStatusThenOk(){
    // Given
    Long id = 1L;
    String newStatus = "UNPAID";
    DebtPosition debtPosition = buildDebtPosition();
    InstallmentNoPII installment = InstallmentNoPII.builder()
      .installmentId(2L)
      .paymentOptionId(1L)
      .status("status")
      .iupdPagopa("iupdPagoPa1")
      .iud("iud1").build();

    debtPosition.getPaymentOptions().getFirst().setInstallments(new TreeSet<>(List.of(buildInstallmentNoPII(), installment)));

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);

    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatus(id, newStatus);

    Map<String, IudSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IudSyncStatusUpdateDTO iudSyncStatusUpdateDTO = IudSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa("iupdPagoPa")
      .build();

    syncStatusDTO.put("iud", iudSyncStatusUpdateDTO);

    // When
    service.finalizeSyncStatus(id, syncStatusDTO);

    // Then
    verify(debtPositionRepositoryMock).findOneWithAllDataByDebtPositionId(id);
    verify(installmentNoPIIRepositoryMock).updateStatus(id, newStatus);

    assertNotEquals(newStatus, debtPosition.getStatus());
    assertNotEquals(newStatus, debtPosition.getPaymentOptions().getFirst().getStatus());
    assertEquals(newStatus, debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertNotEquals(newStatus, debtPosition.getPaymentOptions().getFirst().getInstallments().getLast().getStatus());
  }
}

