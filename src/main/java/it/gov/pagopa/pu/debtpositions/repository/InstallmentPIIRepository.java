package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;

import java.util.List;

public interface InstallmentPIIRepository {

  InstallmentDTO save(InstallmentDTO installment);

  List<InstallmentDTO> getByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin);

  void delete(InstallmentNoPII installmentNoPII);

  List<InstallmentDTO> getByOrganizationIdAndReceiptId(Long organizationId, Long receiptId, List<DebtPositionOrigin> debtPositionOrigin);

  List<InstallmentDTO> findByIuvOrNav(String iuvOrNav, String debtorFiscalCode, Long organizationId, List<InstallmentStatus> statuses);
}
