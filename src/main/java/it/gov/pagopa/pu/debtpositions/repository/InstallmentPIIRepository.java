package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;

import java.util.List;

public interface InstallmentPIIRepository {

  InstallmentDTO save(InstallmentDTO installment);

  List<InstallmentDTO> getByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin, String iud);

  void delete(InstallmentNoPII installmentNoPII);
}
