package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "debt-positions")
public interface DebtPositionRepository extends JpaRepository<DebtPosition, Long> {

  @EntityGraph(value = "completeDebtPosition")
  DebtPosition findOneWithAllDataByDebtPositionId(Long debtPositionId);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE DebtPosition d SET d.status = :status WHERE d.debtPositionId = :debtPositionId")
  void updateStatus(@Param("debtPositionId") Long debtPositionId, @Param("status") DebtPositionStatus status);

  @Query(value = "SELECT d FROM DebtPosition d " +
    "JOIN d.paymentOptions p " +
    "JOIN p.installments i " +
    "JOIN i.transfers t " +
    "WHERE t.transferId = :transferId")
  DebtPosition findByTransferId(@Param("transferId") Long transferId);

}
