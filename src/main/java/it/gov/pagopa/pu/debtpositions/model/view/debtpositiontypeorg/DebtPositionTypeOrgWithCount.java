package it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorg;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "debt_position_type_org")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DebtPositionTypeOrgWithCount implements Serializable {

  @Id
  private Long debtPositionTypeOrgId;
  @NotNull
  private Long organizationId;
  @NotNull
  private String code;
  @NotNull
  private String description;
  private LocalDateTime updateDate;
  @Formula("(SELECT COUNT(*) "
    + "FROM debt_position_type_org_operators o "
    + "WHERE debt_position_type_org_id = o.debt_position_type_org_id)")
  private Integer enabledOperators;
  private boolean flagActive;

}
