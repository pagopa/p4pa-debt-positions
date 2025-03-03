package it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildSyncDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;

class InstallmentSynchronizeMapperTest {

  private InstallmentSynchronizeMapper installmentSynchronizeMapper;

  @BeforeEach
  void setUp() {
    installmentSynchronizeMapper = new InstallmentSynchronizeMapper();
  }

  @Test
  void testSyncMapper(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO expectedDebtPositionDTO = buildSyncDebtPositionDTO();

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, 1L);

    reflectionEqualsByName(expectedDebtPositionDTO, result);
  }

  @Test
  void testSyncMapperDraft(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setDraft(Boolean.TRUE);
    DebtPositionDTO expectedDebtPositionDTO = buildSyncDebtPositionDTO();
    expectedDebtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.DRAFT);

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, 1L);

    reflectionEqualsByName(expectedDebtPositionDTO, result);
  }
}
