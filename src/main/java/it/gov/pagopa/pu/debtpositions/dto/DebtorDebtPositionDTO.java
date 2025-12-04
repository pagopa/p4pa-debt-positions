package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class DebtorDebtPositionDTO implements BaseDebtPosition{

  private Long debtPositionId;

  private String debtPositionDescription;

  private String debtPositionTypeOrgDescription;

  private DebtPositionStatus status;

  private DebtPositionOrigin debtPositionOrigin;

  private String iupdOrg;

  private Long organizationId;

  private List<BasePaymentOption> paymentOptions = new ArrayList<>();

}
