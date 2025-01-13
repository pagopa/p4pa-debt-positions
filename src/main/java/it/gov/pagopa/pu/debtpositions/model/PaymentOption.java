package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.SortedSet;

@Entity
@Table(name = "payment_option")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "paymentOptionId", callSuper = false)
public class PaymentOption extends BaseEntity implements Serializable, Comparable<PaymentOption> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_option_generator")
  @SequenceGenerator(name = "payment_option_generator", sequenceName = "payment_option_seq", allocationSize = 1)
  private Long paymentOptionId;
  private Long debtPositionId;
  private Long totalAmountCents;
  private String status;
  private boolean multiDebtor;
  private OffsetDateTime dueDate;
  private String description;
  @Enumerated(EnumType.STRING)
  private PaymentOptionType paymentOptionType;

  @OneToMany(mappedBy = "paymentOptionId")
  private SortedSet<InstallmentNoPII> installments;

  @Override
  public int compareTo(@Nonnull PaymentOption o) {
    return Comparator.comparing(PaymentOption::getPaymentOptionId).compare(this, o);
  }
}
