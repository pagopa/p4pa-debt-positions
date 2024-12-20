package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@NamedEntityGraph(
        name = "debt-position-entity-graph",
        attributeNodes = {
                @NamedAttributeNode("paymentOptions")
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
public class DebtPosition implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_generator")
    @SequenceGenerator(name = "debt_position_generator", sequenceName = "debt_position_seq", allocationSize = 1)
    private Long debtPositionId;
    private String iupdOrg;
    private String iupdPagopa;
    private String description;
    private String status;
    private Long ingestionFlowFileId;
    private Long ingestionFlowFileLineNumber;
    private Long organizationId;
    private Long debtPositionTypeOrgId;
    private OffsetDateTime notificationDate;
    private OffsetDateTime validityDate;
    private boolean flagIuvVolatile;
    private OffsetDateTime creationDate;
    private OffsetDateTime updateDate;
    private Long updateOperatorId;

    @OneToMany
    private List<PaymentOption> paymentOptions;
}
