package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.Installment;

import java.util.List;

public interface InstallmentPIIRepository {

  long save(Installment installment);

  List<Installment> getByOrganizationIdAndNav(Long organizationId, String nav);
}
