package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface PrimaryOrgInstallmentService {
  Optional<InstallmentNoPII> findPrimaryOrgInstallment(Organization primaryOrg, String noticeNumber);
}
