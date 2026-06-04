package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.enums.DebtPositionTypeOrgBalanceCostType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DebtPositionTypeOrgBalanceCostId implements Serializable {
  @NotNull
  private Long debtPositionTypeOrgId;
  @Enumerated(EnumType.STRING)
  @NotNull
  private DebtPositionTypeOrgBalanceCostType type;
  @NotNull
  private String operatingYear;
}
