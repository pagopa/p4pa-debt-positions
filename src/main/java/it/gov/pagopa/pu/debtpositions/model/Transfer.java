package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfer")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transfer {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transfer_generator")
  @SequenceGenerator(name = "transfer_generator", sequenceName = "transfer_seq", allocationSize = 1)
  private Long transferId;
  private Long installmentId;
  private String orgFiscalCode;
  private String orgName;
  private Long amountCents;
  private String remittanceInformation;
  @Embedded
  private Stamp stamp;
  private String iban;
  private String postalIban;
  private String category;
  private Long transferIndex;
  private LocalDateTime creationDate;
  private LocalDateTime updateDate;
  private Long operatorExternalUserId;

}
