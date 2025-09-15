package it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorg;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "debt_position_type_org")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DebtPositionTypeOrgWithActiveSpontaneousCount {

  @Id
  private Long organizationId;
  @Formula("""
    (SELECT COUNT(*)
    FROM debt_position_type_org dpto
    WHERE dpto.organization_id = organization_id
    AND dpto.flag_active = true
    AND dpto.flag_spontaneous = true)
  """)
  private Integer activeSpontaneousDebtPositionTypeOrg;

}
