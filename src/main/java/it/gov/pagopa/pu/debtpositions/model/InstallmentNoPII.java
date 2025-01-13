package it.gov.pagopa.pu.debtpositions.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.SortedSet;

@Entity
@Table(name = "installment")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "installmentId", callSuper = false)
public class InstallmentNoPII extends BaseEntity implements Serializable, Comparable<InstallmentNoPII> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "installment_generator")
  @SequenceGenerator(name = "installment_generator", sequenceName = "installment_seq", allocationSize = 1)
  private Long installmentId;
  private Long paymentOptionId;
  private String status;
  private String iupdPagopa;
  private String iud;
  private String iuv;
  private String iur;
  private String iuf;
  private String nav;
  private OffsetDateTime dueDate;
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

  @OneToMany(mappedBy = "installmentId")
  private SortedSet<Transfer> transfers;

  @Override
  public int compareTo(@Nonnull InstallmentNoPII o) {
    return Comparator.comparing(InstallmentNoPII::getInstallmentId).compare(this, o);
  }
}
