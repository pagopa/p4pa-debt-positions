package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class InstallmentsSearchFiltersDTO {

  private Long organizationId;
  private String operatorExternalUserId;
  private LocalDate dueDateFrom;
  private LocalDate dueDateTo;
  private String iuv;
  private String iud;
  private String fiscalCode;
  private List<DebtPositionOrigin> debtPositionOrigins;
  private Long debtPositionTypeOrgId;
  private InstallmentStatus status;

}
