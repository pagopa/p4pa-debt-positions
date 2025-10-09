package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExportPaidInstallmentsFiltersDTO {

  private Long organizationId;
  private String operatorExternalUserId;
  private OffsetDateTimeIntervalFilter paymentDateTime;
  private LocalDateTimeIntervalFilter installmentUpdateDateTime;
  private Long debtPositionTypeOrgId;
  private List<DebtPositionOrigin> debtPositionOrigins;

}
