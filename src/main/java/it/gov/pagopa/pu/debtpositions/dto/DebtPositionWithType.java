package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebtPositionWithType {
  private DebtPosition debtPosition;
  private String debtPositionTypeOrgDescription;
  private String debtPositionTypeOrgCode;
}
