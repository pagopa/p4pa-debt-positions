package it.gov.pagopa.pu.debtpositions.model.view.debtpositiontype;

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
@Table(name = "debt_position_type")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class DebtPositionTypeWithCount implements Serializable {

  @Id
  private Long debtPositionTypeId;
  @NotNull
  private String code;
  @NotNull
  private String description;
  private LocalDateTime updateDate;
  @Formula("(SELECT COUNT(*) "
    + "FROM debt_position_type_org org "
    + "WHERE debt_position_type_id = org.debt_position_type_id)")
  private Integer activeOrganizations;
  @NotNull
  private Long brokerId;
}
