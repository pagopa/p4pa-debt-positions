package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;

import java.util.List;

public interface InstallmentPIIRepository {

  InstallmentNoPII save(Installment installment);

  List<Installment> getByOrganizationIdAndNav(Long organizationId, String nav);
}
