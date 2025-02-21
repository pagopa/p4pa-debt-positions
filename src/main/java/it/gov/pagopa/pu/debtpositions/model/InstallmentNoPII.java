package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.util.validator.ImmutableField;
import it.gov.pagopa.pu.debtpositions.util.validator.ImmutableFieldEntityListener;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.SortedSet;

@Entity
@Table(name = "installment")
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "installmentId", callSuper = false)
@EntityListeners(ImmutableFieldEntityListener.class)
public class InstallmentNoPII extends BaseEntity implements Serializable, Comparable<InstallmentNoPII>, NoPIIEntity<InstallmentPIIDTO> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "installment_generator")
  @SequenceGenerator(name = "installment_generator", sequenceName = "installment_seq", allocationSize = 1)
  @ImmutableField
  private Long installmentId;
  @NotNull
  @ImmutableField
  private Long paymentOptionId;
  @Enumerated(EnumType.STRING)
  @NotNull
  private InstallmentStatus status;
  @ImmutableField
  private String iupdPagopa;
  @NotNull
  @ImmutableField
  private String iud;
  @ImmutableField
  private String iuv;
  @ImmutableField
  private String iur;
  @ImmutableField
  private String iuf;
  @ImmutableField
  private String nav;
  private OffsetDateTime dueDate;
  @NotNull
  @ImmutableField
  private String paymentTypeCode;
  @NotNull
  private Long amountCents;
  @NotNull
  private String remittanceInformation;
  private String balance;
  private String legacyPaymentMetadata;
  @NotNull
  private Long personalDataId;
  @Enumerated(EnumType.STRING)
  @NotNull
  @ImmutableField
  private PersonEntityType debtorEntityType;
  @NotNull
  private byte[] debtorFiscalCodeHash;
  @Embedded
  private InstallmentSyncStatus syncStatus;
  private OffsetDateTime notificationDate;
  @ImmutableField
  private Long ingestionFlowFileId;
  @ImmutableField
  private Long ingestionFlowFileLineNumber;
  @ImmutableField
  private Long receiptId;

  @OneToMany(mappedBy = "installmentId")
  private SortedSet<Transfer> transfers;

  @Override
  public int compareTo(@Nonnull InstallmentNoPII o) {
    return Comparator
      .comparing(InstallmentNoPII::getInstallmentId, Comparator.nullsFirst(Comparator.naturalOrder()))
      .compare(this, o);
  }
}
