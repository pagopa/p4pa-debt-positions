package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InstallmentSyncStatus implements Serializable {

  public InstallmentSyncStatus(InstallmentStatus from, InstallmentStatus to){
    this.syncStatusFrom = from;
    this.syncStatusTo = to;
  }

  @NotNull
  @Enumerated(EnumType.STRING)
  private InstallmentStatus syncStatusFrom;
  @NotNull
  @Enumerated(EnumType.STRING)
  private InstallmentStatus syncStatusTo;
  private String syncError;

}
