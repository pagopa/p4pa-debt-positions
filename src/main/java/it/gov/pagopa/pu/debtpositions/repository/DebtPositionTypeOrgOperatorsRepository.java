package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

@RepositoryRestResource(path = "debt-position-type-org-operators")
public interface DebtPositionTypeOrgOperatorsRepository extends JpaRepository<DebtPositionTypeOrgOperators,Long>{

  Optional<DebtPositionTypeOrgOperators> findByDebtPositionTypeOrgIdAndOperatorExternalUserId(Long debtPositionTypeOrgId, String operatorExternalUserId);
  List<DebtPositionTypeOrgOperators> findByDebtPositionTypeOrgId(Long debtPositionTypeOrgId);
  @Transactional
  long deleteByDebtPositionTypeOrgId(Long debtPositionTypeOrgId);
  @Transactional
  @Modifying
  @Query("DELETE FROM DebtPositionTypeOrgOperators dptoo WHERE dptoo.debtPositionTypeOrgId = :debtPositionTypeOrgId "
    + " AND dptoo.operatorExternalUserId IN :operatorExternalUserIds ")
  int deleteByDebtPositionTypeOrgIdAndOperatorExternalUserId(Long debtPositionTypeOrgId, Set<String> operatorExternalUserIds);
}
