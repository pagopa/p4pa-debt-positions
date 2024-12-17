package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "installment")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Installment {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "installment_generator")
  @SequenceGenerator(name = "installment_generator", sequenceName = "installment_seq", allocationSize = 1)
  private Long installmentId;
  private Long paymentOptionId;
  private String status;
  private String iud;
  private String iuv;
  private String iur;
  private LocalDate dueDate;
  private String paymentTypeCode;
  private Long amountCents;
  private Long notificationFeeCents;
  private String remittanceInformation;
  private String humanFriendlyRemittanceInformation;
  private String balance;
  private String legacyPaymentMetadata;
  private Long personalDataId;
  private Character debtorEntityType;
  private byte[] debtorFiscalCodeHash;
  private LocalDateTime creationDate;
  private LocalDateTime updateDate;
  private Long updateOperatorId;

}
