package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DebtPositionProcessorServiceImplTest {

  private DebtPositionProcessorServiceImpl debtPositionProcessorService;

  @BeforeEach
  void setUp() {
    debtPositionProcessorService = new DebtPositionProcessorServiceImpl();
  }

  // begin region updateAmounts with DebtPositionDTO
  @Test
  void givenDebtPositionDTOWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));

    debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(3300, debtPositionDTO.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void givenDebtPositionDTOWithInstallmentCancelledWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));
    firstInstallment.setStatus(InstallmentStatus.CANCELLED);
    firstInstallment.setSyncStatus(null);

    debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(2200, debtPositionDTO.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void givenDebtPositionDTOWithInstallmentWithStatusToCancelledWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));
    firstInstallment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(InstallmentStatus.UNPAID).syncStatusTo(InstallmentStatus.CANCELLED).build());

    debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(2200, debtPositionDTO.getPaymentOptions().getFirst().getTotalAmountCents());
  }
  // end region updateAmounts with DebtPositionDTO
  // begin region updateAmounts with DebtPosition
  @Test
  void givenDebtPositionWhenUpdateAmountsThenOk() {
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentNoPII())
      .collect(Collectors.toCollection(ArrayList::new)));

    debtPositionProcessorService.updateAmounts(debtPosition);

    assertEquals(1100, debtPosition.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void givenDebtPositionWithInstallmentCancelledWhenUpdateAmountsThenOk() {
    DebtPosition debtPosition = buildDebtPosition();
    InstallmentNoPII firstInstallment = debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst();
    debtPosition.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentNoPII())
      .collect(Collectors.toCollection(ArrayList::new)));
    firstInstallment.setStatus(InstallmentStatus.CANCELLED);
    firstInstallment.setSyncStatus(null);

    debtPositionProcessorService.updateAmounts(debtPosition);

    assertEquals(0, debtPosition.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void givenDebtPositionWithInstallmentWithStatusToCancelledWhenUpdateAmountsThenOk() {
    DebtPosition debtPosition = buildDebtPosition();
    InstallmentNoPII firstInstallment = debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst();
    debtPosition.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentNoPII())
      .collect(Collectors.toCollection(ArrayList::new)));
    firstInstallment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(InstallmentStatus.UNPAID).syncStatusTo(InstallmentStatus.CANCELLED).build());

    debtPositionProcessorService.updateAmounts(debtPosition);

    assertEquals(0, debtPosition.getPaymentOptions().getFirst().getTotalAmountCents());
  }
  // end region updateAmounts with DebtPosition

}
