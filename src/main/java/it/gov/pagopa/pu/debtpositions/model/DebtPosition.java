package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.util.validator.ImmutableField;
import it.gov.pagopa.pu.debtpositions.util.validator.ImmutableFieldEntityListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.SortedSet;

@NamedEntityGraph(
  name = "completeDebtPosition",
  attributeNodes = {
    @NamedAttributeNode(value = "paymentOptions", subgraph = "subgraph.payment-option")
  },
  subgraphs = {
    @NamedSubgraph(name = "subgraph.payment-option",
      attributeNodes = @NamedAttributeNode(value = "installments", subgraph = "subgraph.installment")),
    @NamedSubgraph(name = "subgraph.installment",
      attributeNodes = @NamedAttributeNode(value = "transfers"))
  }
)
@Entity
@Table(name = "debt_position")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = "debtPositionId", callSuper = false)
@EntityListeners(ImmutableFieldEntityListener.class)
public class DebtPosition extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_generator")
  @SequenceGenerator(name = "debt_position_generator", sequenceName = "debt_position_seq", allocationSize = 1)
  @ImmutableField
  private Long debtPositionId;
  @NotNull
  @ImmutableField
  private String iupdOrg;
  private String description;
  @Enumerated(EnumType.STRING)
  @NotNull
  private DebtPositionStatus status;
  @Enumerated(EnumType.STRING)
  @NotNull
  @ImmutableField
  private DebtPositionOrigin debtPositionOrigin;
  @NotNull
  @ImmutableField
  private Long organizationId;
  @NotNull
  @ImmutableField
  private Long debtPositionTypeOrgId;
  private OffsetDateTime validityDate;
  private boolean flagIuvVolatile;
  @ImmutableField
  private boolean multiDebtor;
  @ImmutableField
  private boolean flagPagoPaPayment;

  @OneToMany(mappedBy = "debtPositionId")
  private SortedSet<PaymentOption> paymentOptions;
}
