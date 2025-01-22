package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "transfers")
public interface TransferRepository extends JpaRepository<Transfer,Long> {

  @Query(value = "SELECT t.* from debt_positions.transfer t " +
    "JOIN organizations.organization o ON t.org_fiscal_code = o.org_fiscal_code " +
    "JOIN debt_positions.installment i ON t.installment_id = i.installment_id " +
    "WHERE o.organization_id = :orgId AND " +
    "i.iuv = :iuv AND " +
    "i.iur = :iur AND " +
    "t.transfer_index = :transferIndex", nativeQuery = true)
  Optional<Transfer> findBySemanticKey(Long orgId, String iuv, String iur, Long transferIndex);
}
