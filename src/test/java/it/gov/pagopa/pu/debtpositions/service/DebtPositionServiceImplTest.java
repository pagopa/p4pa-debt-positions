package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IudSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.mapper.*;
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

    Mockito.verify(debtPositionRepositoryMock, Mockito.times(1)).save(debtPosition);
    Mockito.verify(paymentOptionRepositoryMock, Mockito.times(1)).save(paymentOption);
    Mockito.verify(installmentRepositoryMock, Mockito.times(1)).save(installment);
    Mockito.verify(transferRepositoryMock, Mockito.times(1)).save(transfer);
  }

  @Test
  void givenFinalizeSyncStatusThenOk(){
    Long id = 1L;
    String newStatus = "newStatus";
    DebtPosition debtPosition = buildDebtPosition();
    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);

    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatus(id, newStatus);

    Mockito.doNothing().when(paymentOptionRepositoryMock).updateStatus(id, newStatus);

    Mockito.doNothing().when(debtPositionRepositoryMock).updateStatus(id, newStatus);


    Map<String, IudSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IudSyncStatusUpdateDTO iudSyncStatusUpdateDTO = IudSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa("iupdPagopa")
      .build();

    syncStatusDTO.put("iud", iudSyncStatusUpdateDTO);

    service.finalizeSyncStatus(id, syncStatusDTO);
  }
}

