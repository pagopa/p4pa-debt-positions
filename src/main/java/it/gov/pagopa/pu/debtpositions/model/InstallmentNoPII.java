package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.BaseInstallment;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.Action;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonEntityType;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.SortedSet;

@Entity
@Table(name = "installment")
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = {"installmentId", "iud"}, callSuper = false)
public class InstallmentNoPII extends BaseEntity implements Serializable, Comparable<InstallmentNoPII>, NoPIIEntity<InstallmentPIIDTO>, BaseInstallment {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "installment_generator")
  @SequenceGenerator(name = "installment_generator", sequenceName = "installment_seq", allocationSize = 1)
  private Long installmentId;
  @NotNull
  private Long paymentOptionId;
  @Enumerated(EnumType.STRING)
  @NotNull
  private InstallmentStatus status;
  @Embedded
  private InstallmentSyncStatus syncStatus;
  private String iupdPagopa;
  private boolean generateNotice;
  @NotNull
  private String iud;
  private String iuv;
  private String iur;
  private String iuf;
  private String nav;
  private String iun;
  private LocalDate dueDate;
  private boolean switchToExpired;
  private Long notificationFeeCents;
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
  private PersonEntityType debtorEntityType;
  @NotNull
  private byte[] debtorFiscalCodeHash;
  private OffsetDateTime notificationDate;
  private Long ingestionFlowFileId;
  private Long ingestionFlowFileLineNumber;
  @Enumerated(EnumType.STRING)
  private Action ingestionFlowFileAction;
  private String sourceFlowName;
  private Long receiptId;

  @OneToMany(mappedBy = "installmentId")
  private SortedSet<Transfer> transfers;

  @Override
  public int compareTo(@Nonnull InstallmentNoPII o) {
    return new CompareToBuilder()
      .append(this.getInstallmentId(), o.getInstallmentId())
      .append(this.getIud(), o.getIud())
      .build();
  }
}
