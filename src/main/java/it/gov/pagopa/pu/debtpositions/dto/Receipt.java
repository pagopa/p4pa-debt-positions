package it.gov.pagopa.pu.debtpositions.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptOrigin;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

  private Long receiptId;
  private Long ingestionFlowFileId;
  private ReceiptOrigin receiptOrigin;
  private String paymentReceiptId;
  private String noticeNumber;
  private String paymentNote;
  private String orgFiscalCode;
  private String outcome;
  private String creditorReferenceId;
  private Long paymentAmountCents;
  private String description;
  private String companyName;
  private String officeName;
  private String idPsp;
  private String pspFiscalCode;
  private String pspPartitaIva;
  private String pspCompanyName;
  private String idChannel;
  private String channelDescription;
  private String paymentMethod;
  private Long feeCents;
  private OffsetDateTime paymentDateTime;
  private OffsetDateTime applicationDate;
  private OffsetDateTime transferDate;
  private boolean standin;
  private Person debtor;
  private Person payer;
  private LocalDateTime creationDate;
  private LocalDateTime updateDate;
  private String updateOperatorExternalId;

  @JsonIgnore
  private ReceiptNoPII noPII;
}
