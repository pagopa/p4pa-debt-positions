package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.organization.dto.generated.Organization;

public interface DebtPositionProcessorService {
  DebtPositionDTO updateAmounts(DebtPositionDTO debtPositionDTO);

  void populateFirstTransfer(InstallmentDTO installmentDTO, Organization organization, DebtPositionTypeOrg debtPositionTypeOrg);
}
