package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
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
public class InstallmentNoPII extends BaseEntity implements Serializable, Comparable<InstallmentNoPII> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "installment_generator")
  @SequenceGenerator(name = "installment_generator", sequenceName = "installment_seq", allocationSize = 1)
  private Long installmentId;
  @NotNull
  private Long paymentOptionId;
  @Enumerated(EnumType.STRING)
  @NotNull
  private InstallmentStatus status;
  private String iupdPagopa;
  @NotNull
  private String iud;
  private String iuv;
  private String iur;
  private String iuf;
  private String nav;
  private OffsetDateTime dueDate;
  @NotNull
  private String paymentTypeCode;
  @NotNull
  private Long amountCents;
  private Long notificationFeeCents;
  @NotNull
  private String remittanceInformation;
  private String humanFriendlyRemittanceInformation;
  private String balance;
  @NotNull
  private String legacyPaymentMetadata;
  @NotNull
  private Long personalDataId;
  @Enumerated(EnumType.STRING)
  @NotNull
  private PersonEntityType debtorEntityType;
  @NotNull
  private byte[] debtorFiscalCodeHash;
  @Embedded
  private InstallmentSyncStatus syncStatus;

  @OneToMany(mappedBy = "installmentId")
  private SortedSet<Transfer> transfers;

  @Override
  public int compareTo(@Nonnull InstallmentNoPII o) {
    return Comparator.comparing(InstallmentNoPII::getInstallmentId).compare(this, o);
  }
}
