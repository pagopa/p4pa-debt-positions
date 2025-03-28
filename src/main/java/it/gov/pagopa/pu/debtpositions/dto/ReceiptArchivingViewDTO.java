package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptArchivingViewDTO {

  private Long receiptId;
  @NotNull
  private String paymentReceiptId;
  private OffsetDateTime paymentDateTime;
  @NotNull
  private String creditorReferenceId;
  @NotNull
  private PersonEntityType debtorEntityType;
  private String iuv;
  @NotNull
  private String remittanceInformation;
  @NotNull
  private String orgFiscalCode;
  private Person payer;
}
