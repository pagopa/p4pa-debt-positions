package it.gov.pagopa.pu.debtpositions.dto;

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
  private OffsetDateTimeIntervalFilter installmentUpdateDateTime;
  private Long debtPositionTypeOrgId;

}
