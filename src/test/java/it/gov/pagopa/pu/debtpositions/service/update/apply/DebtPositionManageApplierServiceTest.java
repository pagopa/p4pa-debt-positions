package it.gov.pagopa.pu.debtpositions.service.update.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.service.update.applier.DebtPositionManageApplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DebtPositionManageApplierServiceTest {

  private DebtPositionManageApplierService applier;

  private static final LocalDate DATE = LocalDate.of(2089, 1, 1);
  private static final TransferDTO TRANSFER_1 = buildTransferDTO().transferIndex(1).transferId(1L);

  @BeforeEach
  void setUp() {
    applier = new DebtPositionManageApplierService();
  }

  @Test
  void givenUpdatedInstallmentWithNewDataWhenMergeThenSuccess(){
    InstallmentDTO storedInstallment = buildInstallmentDTO();
    storedInstallment.getTransfers().add(TRANSFER_1);
    InstallmentDTO updatedInstallment = buildInstallmentDTO();
    updatedInstallment.getTransfers().add(TRANSFER_1);
    updatedInstallment.setRemittanceInformation("remittanceInformation_updated");
    updatedInstallment.setDueDate(DATE);

    applier.merge(updatedInstallment, storedInstallment);

    assertEquals("remittanceInformation_updated", storedInstallment.getRemittanceInformation());
    assertEquals(DATE, storedInstallment.getDueDate());
    assertEquals(1100L, storedInstallment.getAmountCents());
  }

  @Test
  void givenUpdatedInstallmentWithNewDataUnmodifiableWhenMergeThenException(){
    InstallmentDTO storedInstallment = buildInstallmentDTO();
    InstallmentDTO updatedInstallment = buildInstallmentDTO();
    updatedInstallment.setIupdPagopa("new_iupd_pagopa");
    updatedInstallment.setIuv("new_iuv");
    updatedInstallment.getDebtor().setFiscalCode("new_uniqueIdentifierCode");

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applier.merge(updatedInstallment, storedInstallment));

    assertEquals("IMMUTABLE_FIELD",exception.getCode());
    assertEquals("These fields for installment having id 100 are not mutable: [iupdPagopa, iuv, debtor: [fiscalCode]]", exception.getMessage());
  }

  @Test
  void givenUpdatedInstallmentWithTransfersSizeModifiedWhenMergeThenException(){
    InstallmentDTO storedInstallment = buildInstallmentDTO();
    InstallmentDTO updatedInstallment = buildInstallmentDTO();
    updatedInstallment.getTransfers().add(TRANSFER_1);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applier.merge(updatedInstallment, storedInstallment));

    assertEquals("IMMUTABLE_FIELD",exception.getCode());
    assertEquals("The number of beneficiary for installment having id 100 cannot be modified", exception.getMessage());
  }

  @Test
  void givenUpdatedInstallmentWithTransferNotFoundWhenMergeThenException(){
    InstallmentDTO storedInstallment = buildInstallmentDTO();
    InstallmentDTO updatedInstallment = buildInstallmentDTO();
    updatedInstallment.getTransfers().getFirst().setTransferId(1L);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applier.merge(updatedInstallment, storedInstallment));

    assertEquals("TRANSFER_NOT_FOUND",exception.getCode());
    assertEquals("The transfer having id 1000 of installment having id 100 does not found", exception.getMessage());
  }

  @Test
  void givenUpdatedInstallmentWithTransfersDataUnmodifiableWhenMergeThenSuccess(){
    InstallmentDTO storedInstallment = buildInstallmentDTO();
    InstallmentDTO updatedInstallment = buildInstallmentDTO();
    updatedInstallment.getTransfers().getFirst().setOrgName("new_org_name");
    updatedInstallment.getTransfers().getFirst().setOrgFiscalCode("new_org_fiscal_code");

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
            () -> applier.merge(updatedInstallment, storedInstallment));

    assertEquals("IMMUTABLE_FIELD",exception.getCode());
    assertEquals("These fields for transfer with index 1 of installment having id 100 are not mutable: [orgFiscalCode, orgName]", exception.getMessage());
  }

}
