package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.SortedSet;

@Entity
@Table(name = "payment_option")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = "paymentOptionId", callSuper = false)
public class PaymentOption extends BaseEntity implements Serializable, Comparable<PaymentOption> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_option_generator")
  @SequenceGenerator(name = "payment_option_generator", sequenceName = "payment_option_seq", allocationSize = 1)
  private Long paymentOptionId;
  @NotNull
  private Long debtPositionId;
  @NotNull
  private Long totalAmountCents;
  @Enumerated(EnumType.STRING)
  @NotNull
  private PaymentOptionStatus status;
  private OffsetDateTime dueDate;
  @NotNull
  private String description;
  @Enumerated(EnumType.STRING)
  @NotNull
  private PaymentOptionType paymentOptionType;
  @NotNull
  private Integer paymentOptionIndex;

  @OneToMany(mappedBy = "paymentOptionId")
  private SortedSet<InstallmentNoPII> installments;

  @Override
  public int compareTo(@Nonnull PaymentOption o) {
    return Comparator
      .comparing(PaymentOption::getPaymentOptionId, Comparator.nullsFirst(Comparator.naturalOrder()))
      .compare(this, o);
  }

}
