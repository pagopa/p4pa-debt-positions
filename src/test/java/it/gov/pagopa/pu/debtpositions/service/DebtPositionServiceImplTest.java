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
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

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

    InstallmentNoPII installmentNoPIINoIud = buildInstallmentNoPII();
    Installment installmentNoIud = buildInstallment();
    installmentNoIud.setIud("");

    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    Installment installment = buildInstallment();
    installment.setInstallmentId(10L);
    installmentNoPII.setInstallmentId(10L);

    Transfer transfer = buildTransfer();

    Map<InstallmentNoPII, Installment> installmentMap = Map.of(
      installmentNoPIINoIud, installmentNoIud,
      installmentNoPII, installment
    );

    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedPair = Pair.of(debtPosition, installmentMap);

    DebtPosition savedDebtPosition = new DebtPosition();
    savedDebtPosition.setDebtPositionId(1L);
    savedDebtPosition.setPaymentOptions(new TreeSet<>(List.of(paymentOption)));

    PaymentOption savedPaymentOption = new PaymentOption();
    savedPaymentOption.setPaymentOptionId(1L);
    savedPaymentOption.setInstallments(new TreeSet<>(List.of(installmentNoPIINoIud, installmentNoPII)));

    Installment savedInstallment = new Installment();
    savedInstallment.setInstallmentId(1L);

    Transfer savedTransfer = new Transfer();
    savedTransfer.setTransferId(1L);

    String generatedIUD = "RANDOM_IUD_12345";

    Mockito.when(debtPositionMapper.mapToModel(debtPositionDTO)).thenReturn(mappedPair);
    Mockito.when(debtPositionRepository.save(Mockito.any(DebtPosition.class))).thenReturn(savedDebtPosition);
    Mockito.when(paymentOptionRepository.save(Mockito.any(PaymentOption.class))).thenReturn(savedPaymentOption);
    Mockito.when(installmentRepository.save(Mockito.any(Installment.class))).thenReturn(installmentNoPII);
    Mockito.when(transferRepository.save(Mockito.any(Transfer.class))).thenReturn(savedTransfer);

    try (MockedStatic<Utilities> mockedStatic = Mockito.mockStatic(Utilities.class)) {
      mockedStatic.when(Utilities::getRandomIUD).thenReturn(generatedIUD);

      debtPositionService.saveDebtPosition(debtPositionDTO);

      Mockito.verify(debtPositionRepository, Mockito.times(1)).save(debtPosition);
      Mockito.verify(paymentOptionRepository, Mockito.times(1)).save(paymentOption);
      Mockito.verify(installmentRepository, Mockito.times(1)).save(installment);
      Mockito.verify(transferRepository, Mockito.times(2)).save(transfer);
    }
  }
}

