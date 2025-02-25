package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;

import java.util.List;

public interface InstallmentPIIRepository {

  Installment save(Installment installment);

  List<Installment> getByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin);
}
