package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildSyncInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildTransferSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstallmentSynchronizeInstallmentApplierServiceTest {

  private InstallmentSynchronizeInstallmentApplierService applierInstallmentService;

  private final TransferSynchronizeDTO firstSyncTransfer = TransferSynchronizeDTO.builder()
    .transferIndex(1)
    .orgFiscalCode("orgFiscalCode1")
    .orgName("orgName1")
    .amountCents(50L)
    .remittanceInformation("remittanceInformation1")
    .iban("iban1")
    .category("category1")
    .build();

  private final TransferDTO firstTransfer = TransferDTO.builder()
    .transferIndex(1)
    .orgFiscalCode("orgFiscalCode1")
    .orgName("orgName1")
    .amountCents(100L)
    .remittanceInformation("remittanceInformation1")
    .iban("iban1")
    .category("category1")
    .build();

  @BeforeEach
  void setUp() {
    applierInstallmentService = new InstallmentSynchronizeInstallmentApplierService();
  }

  @Test
  void testMergeInstallmentThenOk() {
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAmountCents(500L);
    installmentSynchronizeDTO.setRemittanceInformation("New remittance information");
    installmentSynchronizeDTO.addAdditionalTransfersItem(firstSyncTransfer);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();
    installmentDTO.addTransfersItem(firstTransfer);

    applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO);

    assertEquals("New remittance information", installmentDTO.getRemittanceInformation());
    assertEquals(500L, installmentDTO.getAmountCents());
    assertEquals(50L, installmentDTO.getTransfers().getLast().getAmountCents());
  }

  @Test
  void testMergeInstallmentImmutableFieldThenException() {
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setDescription("New description");
    installmentSynchronizeDTO.setIuv("iuv_iuv");
    installmentSynchronizeDTO.setFiscalCode("new_fiscal_code");
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("IMMUTABLE_FIELD", exception.getCode());
    assertEquals("These fields for installment with iud iud are not mutable: [iuv, debtor.fiscalCode]", exception.getMessage());
    assertEquals(List.of(
        new ErrorFieldDTO("iuv", ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD, "Cannot be updated"),
        new ErrorFieldDTO("debtor.fiscalCode", ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD, "Cannot be updated")
      ),
      exception.getFields());
  }

  @Test
  void testMergeInstallmentTransfersSizeMismatchThenException() {
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setNumberBeneficiary(3);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("INVALID_INSTALLMENT", exception.getCode());
    assertEquals("The number of beneficiary for installment with iud iud does not match with the size of the list", exception.getMessage());
  }

  @Test
  void testMergeInstallmentTransfersSizeNotMutableThenException() {
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.addAdditionalTransfersItem(firstSyncTransfer);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("IMMUTABLE_FIELD", exception.getCode());
    assertEquals("The number of beneficiary for installment with iud iud cannot be modified", exception.getMessage());
  }

  @Test
  void testMergeInstallmentWithTransferNotFoundThenException() {
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    TransferSynchronizeDTO transferSynchronizeDTO = buildTransferSynchronizeDTO();
    transferSynchronizeDTO.setTransferIndex(3);
    installmentSynchronizeDTO.addAdditionalTransfersItem(transferSynchronizeDTO);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();
    installmentDTO.addTransfersItem(firstTransfer);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("TRANSFER_NOT_FOUND", exception.getCode());
    assertEquals("The transfer with index 1 for installment with iud iud does not found", exception.getMessage());
  }

  @Test
  void testMergeInstallmentWithTransferImmutableFieldThenException() {
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    TransferSynchronizeDTO transferSynchronizeDTO = buildTransferSynchronizeDTO();
    transferSynchronizeDTO.setTransferIndex(1);
    transferSynchronizeDTO.setOrgFiscalCode("New fiscal code");
    installmentSynchronizeDTO.addAdditionalTransfersItem(transferSynchronizeDTO);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();
    installmentDTO.addTransfersItem(firstTransfer);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("IMMUTABLE_FIELD", exception.getCode());
    assertEquals("These fields for transfer with index 1 of installment with iud iud are not mutable: [orgFiscalCode, orgName, iban, category]", exception.getMessage());
    assertEquals(List.of(
        new ErrorFieldDTO("orgFiscalCode", ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD, "Cannot be updated"),
        new ErrorFieldDTO("orgName", ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD, "Cannot be updated"),
        new ErrorFieldDTO("iban", ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD, "Cannot be updated"),
        new ErrorFieldDTO("category", ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD, "Cannot be updated")
      ),
      exception.getFields());
  }

}
