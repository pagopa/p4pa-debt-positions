package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "debt_position_type_org_operators")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DebtPositionTypeOrgOperators implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_type_org_operators_generator")
  @SequenceGenerator(name = "debt_position_type_org_operators_generator", sequenceName = "debt_position_type_org_operators_seq", allocationSize = 1)
  private Long debtPositionTypeOrgOperatorId;
  private Long debtPositionTypeOrgId;
  private Long operatorExternalUserId;
  private OffsetDateTime creationDate;
  private OffsetDateTime updateDate;
  private Long updateOperatorId;
}
