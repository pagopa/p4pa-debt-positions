package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;
import java.util.Set;

@RepositoryRestResource(path = "transfers")
public interface TransferRepository extends JpaRepository<Transfer,Long> {

  @Query(value = "SELECT t from Transfer t " +
    "JOIN InstallmentNoPII i ON t.installmentId = i.installmentId " +
    "JOIN PaymentOption p ON i.paymentOptionId = p.paymentOptionId " +
    "JOIN DebtPosition d ON p.debtPositionId = d.debtPositionId " +
    "WHERE d.organizationId = :orgId AND " +
    "i.iuv = :iuv AND " +
    "i.iur = :iur AND " +
    "t.transferIndex = :transferIndex AND" +
    "(:installmentStatusSet IS null OR i.status in :installmentStatusSet)")
  Optional<Transfer> findBySemanticKey(Long orgId, String iuv, String iur,
                                       int transferIndex, Set<InstallmentStatus> installmentStatusSet);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE Transfer t " +
    "SET t.amountCents = :amountCents, t.remittanceInformation = :remittanceInformation " +
    "WHERE t.transferId = :transferId")
  void update(Long transferId, Long amountCents, String remittanceInformation);
}
