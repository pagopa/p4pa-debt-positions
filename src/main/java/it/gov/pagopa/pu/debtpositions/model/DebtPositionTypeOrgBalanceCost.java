package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.enums.DebtPositionTypeOrgBalanceCostType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "debt_position_type_org_balance_cost")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class DebtPositionTypeOrgBalanceCost extends BaseEntity implements Serializable {
  @Id
  private Long debtPositionTypeOrgId;
  @Enumerated(EnumType.STRING)
  @NotNull
  private DebtPositionTypeOrgBalanceCostType type;
  @NotNull
  private String operatingYear;
  private String officeCode;
  private String officeDescription;
  @NotNull
  private String sectionCode;
  private String sectionDescription;
  private String assessmentCode;
  private String assessmentDescription;
}
