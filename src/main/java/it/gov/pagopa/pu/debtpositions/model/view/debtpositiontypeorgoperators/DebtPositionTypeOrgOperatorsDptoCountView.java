package it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorgoperators;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "debt_position_type_org_operators")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DebtPositionTypeOrgOperatorsDptoCountView implements Serializable {
    @Id
    private String operatorExternalUserId;
    @NotNull
    private Long debtPositionTypeOrgCount;
}
