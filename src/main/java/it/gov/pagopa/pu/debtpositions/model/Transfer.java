package it.gov.pagopa.pu.debtpositions.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Comparator;

@Entity
@Table(name = "transfer")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = "transferId", callSuper = false)
public class Transfer extends BaseEntity implements Serializable, Comparable<Transfer> {

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

  @Override
  public int compareTo(@Nonnull Transfer o) {
    return Comparator.comparing(Transfer::getTransferId).compare(this, o);
  }
}
