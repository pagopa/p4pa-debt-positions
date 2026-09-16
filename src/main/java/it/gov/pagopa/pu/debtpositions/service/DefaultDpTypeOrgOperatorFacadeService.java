package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.RelateUserToDefaultDPTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;

import java.util.List;

public interface DefaultDpTypeOrgOperatorFacadeService {
  List<DebtPositionTypeOrgOperators> relateUserToDefaultDPTypeOrg(RelateUserToDefaultDPTypeOrgDTO relateUserToDefaultDPTypeOrgDTO, String accessToken);
}
