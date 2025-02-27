package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseInstallmentSynchronizeServiceImplTest {

  private BaseInstallmentSynchronizeService baseInstallmentSynchronizeService;

  @BeforeEach
  void setUp() {
    baseInstallmentSynchronizeService = new BaseInstallmentSynchronizeServiceImpl();
  }

  @Test
  void testInstallmentFoundThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InstallmentDTO installmentDTO = baseInstallmentSynchronizeService.findInstallment(debtPositionDTO, "iud", 1);
    assertEquals(installmentDTO, buildInstallmentDTO());
  }

  @Test
  void testInstallmentNotFoundThenThrowException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      baseInstallmentSynchronizeService.findInstallment(debtPositionDTO, "iud2", 2));
    assertEquals(String.format("The installment with iud %s in payment option with index %s not found",
      "iud2", 2), notFoundException.getMessage());
  }
}
