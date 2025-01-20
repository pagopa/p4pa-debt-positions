package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
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
  private Long installmentId;
  private Long ingestionFlowFileId;
  private String receiptOrigin;
  private String paymentReceiptId;
  private String noticeNumber;
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
  private Character debtorEntityType;
  private Long personalDataId;
  private byte[] debtorFiscalCodeHash;
}
