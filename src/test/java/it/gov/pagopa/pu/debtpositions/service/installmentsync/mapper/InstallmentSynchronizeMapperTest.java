package it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildSyncDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeMapperTest {

  private InstallmentSynchronizeMapper installmentSynchronizeMapper;


  @BeforeEach
  void setUp() {
    installmentSynchronizeMapper = new InstallmentSynchronizeMapper();
  }

  @Test
  void testSyncMapper(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setBalance(null);
    DebtPositionDTO expectedDebtPositionDTO = buildSyncDebtPositionDTO();
    expectedDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setBalance(null);
    expectedDebtPositionDTO.setDebtPositionTypeOrgId(2L);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg);

    assertEquals(result, expectedDebtPositionDTO);
    reflectionEqualsByName(expectedDebtPositionDTO, result);
    checkNotNullFields(result, "debtPositionId", "stationId", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }

  @Test
  void testSyncMapperDraft(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setDraft(Boolean.TRUE);
    DebtPositionDTO expectedDebtPositionDTO = buildSyncDebtPositionDTO();
    expectedDebtPositionDTO.setDebtPositionTypeOrgId(2L);
    expectedDebtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.DRAFT);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg);

    assertEquals(expectedDebtPositionDTO, result);
    reflectionEqualsByName(expectedDebtPositionDTO, result);
    checkNotNullFields(result, "debtPositionId", "stationId", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }
}
