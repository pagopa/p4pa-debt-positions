package it.gov.pagopa.pu.debtpositions.model.view.debtposition;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class DebtPositionExtendedView extends DebtPositionView implements Serializable {

  @NotNull
  private String debtPositionTypeDescription;
  @NotNull
  private String taxonomyCode;

}
