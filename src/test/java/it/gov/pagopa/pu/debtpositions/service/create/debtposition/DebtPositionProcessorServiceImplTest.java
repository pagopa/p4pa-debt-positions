package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DebtPositionProcessorServiceImplTest {

  private DebtPositionProcessorServiceImpl debtPositionProcessorService;

  @BeforeEach
  void setUp() {
    debtPositionProcessorService = new DebtPositionProcessorServiceImpl();
  }

  @Test
  void givenDebtPositionWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));

    debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(300, debtPositionDTO.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void givenDebtPositionWithInstallmentCancelledWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));
    firstInstallment.setStatus(InstallmentStatus.CANCELLED);
    firstInstallment.setSyncStatus(null);

    debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(200, debtPositionDTO.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void givenDebtPositionWithInstallmentWithStatusToCancelledWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));
    firstInstallment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(InstallmentStatus.UNPAID).syncStatusTo(InstallmentStatus.CANCELLED).build());

    debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(200, debtPositionDTO.getPaymentOptions().getFirst().getTotalAmountCents());
  }
}
