package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipt")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Receipt {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "receipt_generator")
  @SequenceGenerator(name = "receipt_generator", sequenceName = "receipt_seq", allocationSize = 1)
  private Long receiptId;
  private Long installmentId;
  private Long paymentReceiptId;
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
  private LocalDateTime paymentDateTime;
  private LocalDate applicationDate;
  private LocalDate transferDate;
  private byte[] receiptBytes;
  private boolean standin;
  private Character debtorEntityType;
  private Long personalDataId;
  private byte[] debtorFiscalCodeHash;
  private LocalDateTime creationDate;
  private LocalDateTime updateDate;
  private Long updateOperatorId;

}
