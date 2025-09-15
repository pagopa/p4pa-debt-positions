package it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorg;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "debt_position_type_org")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DebtPositionTypeOrgWithActiveSpontaneousCount implements Serializable {

  @Id
  private Long organizationId;
  @NotNull
  private Long activeSpontaneousDebtPositionTypeOrg;

}
