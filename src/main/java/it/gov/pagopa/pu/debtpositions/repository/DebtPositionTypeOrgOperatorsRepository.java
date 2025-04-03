package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "debt-position-type-org-operators")
public interface DebtPositionTypeOrgOperatorsRepository extends JpaRepository<DebtPositionTypeOrgOperators,Long>{

  Optional<DebtPositionTypeOrgOperators> findByDebtPositionTypeOrgIdAndOperatorExternalUserId(Long debtPositionTypeOrgId, String operatorExternalUserId);
  List<DebtPositionTypeOrgOperators> findByDebtPositionTypeOrgId(Long debtPositionTypeOrgId);

}
