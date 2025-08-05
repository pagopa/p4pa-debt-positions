package it.gov.pagopa.pu.debtpositions.service.dptypeorg;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DebtPositionTypeOrgTechHandlerService {
  private final UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService;

  public DebtPositionTypeOrgTechHandlerService(
    UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService) {
    this.unknownDebtPositionTypeOrgRetrieverService = unknownDebtPositionTypeOrgRetrieverService;
  }

  public DebtPositionTypeOrg createTechnicalDebtPositionTypeOrg(Long organizationId) {
    return unknownDebtPositionTypeOrgRetrieverService.getUnknownDebtPositionTypeOrg(organizationId);
  }
}
