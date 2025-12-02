package it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorg;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "debt_position_type_org")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DebtPositionTypeOrgCountByOrganizationId implements Serializable {
  @Id
  private Long organizationId;

  @Formula("(SELECT COUNT(*) "
    + "FROM debt_position_type_org dpto "
    + "WHERE organization_id = dpto.organization_id)")
  private Integer organizations;
}
