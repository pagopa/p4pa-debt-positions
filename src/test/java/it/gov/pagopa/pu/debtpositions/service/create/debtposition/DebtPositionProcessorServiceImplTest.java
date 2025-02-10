package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class DebtPositionProcessorServiceImplTest {

  private DebtPositionProcessorServiceImpl debtPositionProcessorService;

  @BeforeEach
  public void setUp() {
    debtPositionProcessorService = new DebtPositionProcessorServiceImpl();
  }

  @Test
  void givenDebtPositionWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildPaymentOptionDTO())
      .collect(Collectors.toCollection(ArrayList::new)));
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildTransferDTO())
      .collect(Collectors.toCollection(ArrayList::new)));

    DebtPositionDTO result = debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(3000, result.getPaymentOptions().getFirst().getInstallments().getFirst().getAmountCents());
    assertEquals(5000, result.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void givenDebtPositionWithInstallmentCancelledWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));
    firstInstallment.setStatus(InstallmentStatus.CANCELLED);

    DebtPositionDTO result = debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(2000, result.getPaymentOptions().getFirst().getTotalAmountCents());
  }
}

