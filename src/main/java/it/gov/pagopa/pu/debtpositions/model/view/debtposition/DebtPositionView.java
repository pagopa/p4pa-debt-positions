package it.gov.pagopa.pu.debtpositions.model.view.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "debt_position")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class DebtPositionView {
  @Id
  private Long debtPositionId;
  private String description;
  @NotNull
  private String debtPositionTypeOrgDescription;
  private LocalDateTime creationDate;
  @Enumerated(EnumType.STRING)
  @NotNull
  private DebtPositionStatus status;
  @Enumerated(EnumType.STRING)
  @NotNull
  private DebtPositionOrigin debtPositionOrigin;
  @NotNull
  private Long debtPositionTypeOrgId;
  @NotNull
  private Long organizationId;
}
