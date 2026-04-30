package it.gov.pagopa.pu.debtpositions.service.dptypeorg;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DebtPositionTypeOrgTechHandlerService {
  private final UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService;
  private final MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverService;

  public DebtPositionTypeOrgTechHandlerService(
    UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService,
    MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverService
  ) {
    this.unknownDebtPositionTypeOrgRetrieverService = unknownDebtPositionTypeOrgRetrieverService;
    this.mixedDebtPositionTypeOrgRetrieverService =  mixedDebtPositionTypeOrgRetrieverService;
  }

  @Transactional
  public DebtPositionTypeOrg createTechnicalDebtPositionTypeOrg(Long organizationId) {
    this.mixedDebtPositionTypeOrgRetrieverService.getMixedDebtPositionTypeOrg(organizationId);
    return unknownDebtPositionTypeOrgRetrieverService.getUnknownDebtPositionTypeOrg(organizationId);
  }
}
