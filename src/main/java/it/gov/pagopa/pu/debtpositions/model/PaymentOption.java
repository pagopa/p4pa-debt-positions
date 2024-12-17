package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_option")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentOption {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_option_generator")
  @SequenceGenerator(name = "payment_option_generator", sequenceName = "payment_option_seq", allocationSize = 1)
  private Long paymentOptionId;
  private Long debtPositionId;
  private Long totalAmountCents;
  private String status;
  private boolean multiDebtor;
  private LocalDate dueDate;
  private String description;
  @Enumerated(EnumType.STRING)
  private PaymentOptionType paymentOptionType;
  private LocalDateTime creationDate;
  private LocalDateTime updateDate;
  private Long updateOperatorId;

}
