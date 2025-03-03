package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildSyncInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildTransferSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstallmentSynchronizeApplierInstallmentServiceTest {

  private InstallmentSynchronizeApplierInstallmentService applierInstallmentService;

  private final TransferSynchronizeDTO FIRST_SYNC_TRANSFER = TransferSynchronizeDTO.builder()
    .transferIndex(1)
    .orgFiscalCode("orgFiscalCode1")
    .orgName("orgName1")
    .amountCents(50L)
    .remittanceInformation("remittanceInformation1")
    .iban("iban1")
    .category("category1")
    .build();

  private final TransferDTO FIRST_TRANSFER = TransferDTO.builder()
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
    applierInstallmentService = new InstallmentSynchronizeApplierInstallmentService();
  }

  @Test
  void testMergeInstallmentThenOk(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAmountCents(500L);
    installmentSynchronizeDTO.setRemittanceInformation("New remittance information");
    installmentSynchronizeDTO.addAdditionalTransfersItem(FIRST_SYNC_TRANSFER);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();
    installmentDTO.addTransfersItem(FIRST_TRANSFER);

    applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO);

    assertEquals("New remittance information", installmentDTO.getRemittanceInformation());
    assertEquals(500L, installmentDTO.getAmountCents());
    assertEquals(50L, installmentDTO.getTransfers().getLast().getAmountCents());
  }

  @Test
  void testMergeInstallmentImmutableFieldThenException(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setDescription("New description");
    installmentSynchronizeDTO.setPaymentTypeCode("New payment type code");
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("These fields for installment with iud iud are not mutable: [paymentTypeCode]", exception.getMessage());
  }

  @Test
  void testMergeInstallmentTransfersSizeMismatchThenException(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setNumberBeneficiary(3);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("The number of beneficiary for installment with iud iud does not match with the size of the list", exception.getMessage());
  }

  @Test
  void testMergeInstallmentTransfersSizeNotMutableThenException(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.addAdditionalTransfersItem(FIRST_SYNC_TRANSFER);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("The number of beneficiary for installment with iud iud cannot be modified", exception.getMessage());
  }

  @Test
  void testMergeInstallmentWithTransferNotFoundThenException(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    TransferSynchronizeDTO transferSynchronizeDTO = buildTransferSynchronizeDTO();
    transferSynchronizeDTO.setTransferIndex(3);
    installmentSynchronizeDTO.addAdditionalTransfersItem(transferSynchronizeDTO);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();
    installmentDTO.addTransfersItem(FIRST_TRANSFER);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("The transfer with index 1 for installment with iud iud does not found", exception.getMessage());
  }

  @Test
  void testMergeInstallmentWithTransferImmutableFieldThenException(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    TransferSynchronizeDTO transferSynchronizeDTO = buildTransferSynchronizeDTO();
    transferSynchronizeDTO.setTransferIndex(1);
    transferSynchronizeDTO.setOrgFiscalCode("New fiscal code");
    installmentSynchronizeDTO.addAdditionalTransfersItem(transferSynchronizeDTO);
    InstallmentDTO installmentDTO = buildSyncInstallmentDTO();
    installmentDTO.addTransfersItem(FIRST_TRANSFER);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO));
    assertEquals("These fields for transfer with index 1 of installment with iud iud are not mutable: [orgFiscalCode, orgName, iban, category]", exception.getMessage());
  }

}
