package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptOrigin;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "receipt")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class ReceiptNoPII extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "receipt_generator")
  @SequenceGenerator(name = "receipt_generator", sequenceName = "receipt_seq", allocationSize = 1)
  private Long receiptId;
  private Long ingestionFlowFileId;
  @Enumerated(EnumType.STRING)
  @NotNull
  private ReceiptOrigin receiptOrigin;
  @NotNull
  private String paymentReceiptId;
  @NotNull
  private String noticeNumber;
  private String paymentNote;
  @NotNull
  private String orgFiscalCode;
  @NotNull
  private String outcome;
  @NotNull
  private String creditorReferenceId;
  @NotNull
  private Long paymentAmountCents;
  @NotNull
  private String description;
  @NotNull
  private String companyName;
  private String officeName;
  @NotNull
  private String idPsp;
  private String pspFiscalCode;
  private String pspPartitaIva;
  @NotNull
  private String pspCompanyName;
  @NotNull
  private String idChannel;
  @NotNull
  private String channelDescription;
  private String paymentMethod;
  private Long feeCents;
  private OffsetDateTime paymentDateTime;
  private OffsetDateTime applicationDate;
  private OffsetDateTime transferDate;
  private boolean standin;
  @Enumerated(EnumType.STRING)
  @NotNull
  private PersonEntityType debtorEntityType;
  @NotNull
  private Long personalDataId;
  @NotNull
  private byte[] debtorFiscalCodeHash;
}
