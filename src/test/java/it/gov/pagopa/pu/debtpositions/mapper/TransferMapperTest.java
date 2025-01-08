package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Stamp;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransferMapperTest {

  private final TransferMapper transferMapper = new TransferMapper();

  @Test
  void givenValidTransfer_WhenMapToDto_ThenReturnTransferDTO() {
    Transfer transfer = new Transfer();
    transfer.setTransferId(123L);
    transfer.setOrgFiscalCode("ABC123");
    transfer.setOrgName("Organization Name");
    transfer.setAmountCents(1000L);
    transfer.setRemittanceInformation("Payment Info");
    transfer.setIban("IT60X0542811101000000123456");
    transfer.setPostalIban("123456");
    transfer.setCategory("Category");
    transfer.setTransferIndex(1L);
    transfer.setStamp(new Stamp("TYPE", "HASH", "PR"));

    TransferDTO result = transferMapper.mapToDto(transfer);

    assertNotNull(result);
    assertEquals(123L, result.getTransferId());
    assertEquals("ABC123", result.getOrgFiscalCode());
    assertEquals("Organization Name", result.getOrgName());
    assertEquals(1000L, result.getAmountCents());
    assertEquals("Payment Info", result.getRemittanceInformation());
    assertEquals("IT60X0542811101000000123456", result.getIban());
    assertEquals("123456", result.getPostalIban());
    assertEquals("Category", result.getCategory());
    assertEquals(1L, result.getTransferIndex());
    assertEquals("TYPE", result.getStampType());
    assertEquals("HASH", result.getStampHashDocument());
    assertEquals("PR", result.getStampProvincialResidence());
  }

  @Test
  void givenValidTransferDTO_WhenMapToModel_ThenReturnTransfer() {
    TransferDTO dto = TransferDTO.builder()
      .transferId(123L)
      .orgFiscalCode("ABC123")
      .orgName("Organization Name")
      .amountCents(1000L)
      .remittanceInformation("Payment Info")
      .iban("IT60X0542811101000000123456")
      .postalIban("123456")
      .category("Category")
      .transferIndex(1L)
      .stampType("TYPE")
      .stampHashDocument("HASH")
      .stampProvincialResidence("PR")
      .build();

    Transfer result = transferMapper.mapToModel(dto);

    assertNotNull(result);
    assertEquals(123L, result.getTransferId());
    assertEquals("ABC123", result.getOrgFiscalCode());
    assertEquals("Organization Name", result.getOrgName());
    assertEquals(1000L, result.getAmountCents());
    assertEquals("Payment Info", result.getRemittanceInformation());
    assertEquals("IT60X0542811101000000123456", result.getIban());
    assertEquals("123456", result.getPostalIban());
    assertEquals("Category", result.getCategory());
    assertEquals(1L, result.getTransferIndex());
    assertNotNull(result.getStamp());
    assertEquals("TYPE", result.getStamp().getStampType());
    assertEquals("HASH", result.getStamp().getStampHashDocument());
    assertEquals("PR", result.getStamp().getStampProvincialResidence());
  }
}
