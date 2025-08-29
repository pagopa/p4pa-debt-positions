package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildSyncDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstallmentSynchronizeDebtPositionApplierServiceTest {

  private InstallmentSynchronizeDebtPositionApplierService applierDebtPositionService;

  @BeforeEach
  void setUp() {
    applierDebtPositionService = new InstallmentSynchronizeDebtPositionApplierService();
  }

  @Test
  void testMergeDebtPositionThenOk(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setDescription("New description");
    DebtPositionDTO debtPositionDTO = buildSyncDebtPositionDTO();

    applierDebtPositionService.merge(installmentSynchronizeDTO, debtPositionDTO, 1L);

    assertEquals("New description", debtPositionDTO.getDescription());
  }

  @Test
  void testMergeDebtPositionImmutableFieldThenException(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setDescription("New description");
    installmentSynchronizeDTO.setMultiDebtor(Boolean.FALSE);
    installmentSynchronizeDTO.setFlagPuPagoPaPayment(Boolean.FALSE);
    DebtPositionDTO debtPositionDTO = buildSyncDebtPositionDTO();

    ConflictErrorException exception = assertThrows(ConflictErrorException.class, () -> applierDebtPositionService.merge(installmentSynchronizeDTO, debtPositionDTO, 1L));
    assertEquals("[P4PA_UNMODIFIABLE_FIELD] These fields for debt position with iupd IUPD_ORG are not mutable: [multiDebtor, flagPuPagoPaPayment]", exception.getMessage());
  }
}
