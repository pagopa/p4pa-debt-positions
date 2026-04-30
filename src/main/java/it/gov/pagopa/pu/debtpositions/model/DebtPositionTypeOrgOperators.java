package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "debt_position_type_org_operators")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class DebtPositionTypeOrgOperators extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_type_org_operators_generator")
  @SequenceGenerator(name = "debt_position_type_org_operators_generator", sequenceName = "debt_position_type_org_operators_seq", allocationSize = 1)
  private Long debtPositionTypeOrgOperatorId;
  @NotNull
  private Long debtPositionTypeOrgId;
  @NotNull
  private String operatorExternalUserId;
}
