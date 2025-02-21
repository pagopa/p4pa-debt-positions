package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.util.validator.ImmutableField;
import it.gov.pagopa.pu.debtpositions.util.validator.ImmutableFieldEntityListener;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@EntityListeners(ImmutableFieldEntityListener.class)
public class Transfer extends BaseEntity implements Serializable, Comparable<Transfer> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transfer_generator")
  @SequenceGenerator(name = "transfer_generator", sequenceName = "transfer_seq", allocationSize = 1)
  @ImmutableField
  private Long transferId;
  @NotNull
  @ImmutableField
  private Long installmentId;
  @NotNull
  @ImmutableField
  private String orgFiscalCode;
  @ImmutableField
  private String orgName;
  @NotNull
  private Long amountCents;
  @NotNull
  private String remittanceInformation;
  @Embedded
  private Stamp stamp;
  @ImmutableField
  private String iban;
  @ImmutableField
  private String postalIban;
  @NotNull
  @ImmutableField
  private String category;
  @NotNull
  @ImmutableField
  private Integer transferIndex;

  @Override
  public int compareTo(@Nonnull Transfer o) {
    return Comparator
      .comparing(Transfer::getTransferId, Comparator.nullsFirst(Comparator.naturalOrder()))
      .compare(this, o);
  }

}
