package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "payment_option")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentOption implements Serializable {

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
  private OffsetDateTime creationDate;
  private OffsetDateTime updateDate;
  private Long updateOperatorExternalId;

  @OneToMany
  private List<InstallmentNoPII> installments;
}
