package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
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
public class DebtPosition extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_generator")
  @SequenceGenerator(name = "debt_position_generator", sequenceName = "debt_position_seq", allocationSize = 1)
  private Long debtPositionId;
  @NotNull
  private String iupdOrg;
  private String description;
  @Enumerated(EnumType.STRING)
  @NotNull
  private DebtPositionStatus status;
  @Enumerated(EnumType.STRING)
  @NotNull
  private DebtPositionOrigin debtPositionOrigin;
  @NotNull
  private Long organizationId;
  @NotNull
  private Long debtPositionTypeOrgId;
  private OffsetDateTime validityDate;
  private boolean flagIuvVolatile;
  private boolean multiDebtor;
  private boolean flagPagoPaPayment;

  @OneToMany(mappedBy = "debtPositionId")
  private SortedSet<PaymentOption> paymentOptions;
}
