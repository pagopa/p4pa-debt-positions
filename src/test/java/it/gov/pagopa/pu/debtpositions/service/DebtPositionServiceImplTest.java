package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.*;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.Collections;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallment;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransfer;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceImplTest {

  @Mock
  private DebtPositionRepository debtPositionRepository;

  @Mock
  private PaymentOptionRepository paymentOptionRepository;

  @Mock
  private InstallmentPIIRepository installmentRepository;

  @Mock
  private TransferRepository transferRepository;

  @Mock
  private DebtPositionMapper debtPositionMapper;

  private DebtPositionServiceImpl debtPositionService;

  @BeforeEach
  void setUp() {
    debtPositionService = new DebtPositionServiceImpl(
      debtPositionRepository, paymentOptionRepository, installmentRepository, transferRepository,
      debtPositionMapper
    );
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
    savedDebtPosition.setPaymentOptions(Collections.singletonList(paymentOption));

    PaymentOption savedPaymentOption = new PaymentOption();
    savedPaymentOption.setPaymentOptionId(1L);
    savedPaymentOption.setInstallments(Collections.singletonList(installmentNoPII));

    Installment savedInstallment = new Installment();
    savedInstallment.setInstallmentId(1L);

    Transfer savedTransfer = new Transfer();
    savedTransfer.setTransferId(1L);

    Mockito.when(debtPositionMapper.mapToModel(debtPositionDTO)).thenReturn(mappedPair);
    Mockito.when(debtPositionRepository.save(Mockito.any(DebtPosition.class))).thenReturn(savedDebtPosition);
    Mockito.when(paymentOptionRepository.save(Mockito.any(PaymentOption.class))).thenReturn(savedPaymentOption);
    Mockito.when(installmentRepository.save(Mockito.any(Installment.class))).thenReturn(savedInstallment.getInstallmentId());
    Mockito.when(transferRepository.save(Mockito.any(Transfer.class))).thenReturn(savedTransfer);

    debtPositionService.saveDebtPosition(debtPositionDTO);

    Mockito.verify(debtPositionRepository, Mockito.times(1)).save(debtPosition);
    Mockito.verify(paymentOptionRepository, Mockito.times(1)).save(paymentOption);
    Mockito.verify(installmentRepository, Mockito.times(1)).save(installment);
    Mockito.verify(transferRepository, Mockito.times(1)).save(transfer);
  }
}

