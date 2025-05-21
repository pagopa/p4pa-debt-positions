package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "operator")
public interface OperatorViewRepository extends JpaRepository<Operator, Long> {

  @Query("""
   SELECT new Operator(
     CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE end
   )
   FROM DebtPosition d
        JOIN DebtPositionTypeOrg dpto on d.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
        JOIN DebtPositionTypeOrgOperators dptoo on d.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
     WHERE d.debtPositionId= :debtPositionId AND dptoo.operatorExternalUserId = :operatorExternalUserId
   """)
  Operator validateOperator(@Parameter(required = true) @Param("debtPositionId")Long debtPositionId,
                            @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId);

}
