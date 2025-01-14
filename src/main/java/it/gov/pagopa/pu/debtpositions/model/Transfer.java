package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transfer")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Transfer implements Serializable {

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
  private OffsetDateTime creationDate;
  private OffsetDateTime updateDate;
  private Long updateOperatorExternalId;

}
