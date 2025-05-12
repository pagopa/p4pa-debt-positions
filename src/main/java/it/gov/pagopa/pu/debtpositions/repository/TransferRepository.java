package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RepositoryRestResource(path = "transfers")
public interface TransferRepository extends JpaRepository<Transfer, Long> {

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


  @Query
    ("""
      SELECT t
      FROM Transfer t
      JOIN InstallmentNoPII i ON t.installmentId = i.installmentId
      JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
      JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
      JOIN DebtPositionTypeOrgOperators dptoo ON dp.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
      WHERE t.installmentId = :installmentId
      AND dptoo.operatorExternalUserId = :operatorExternalUserId
      """)
  List<Transfer> findAuthorizedByInstallmentId(
    @Parameter(required = true) @Param("installmentId") Long installmentId,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId);

  @Query
    ("""
      SELECT t
      FROM Transfer t
      JOIN InstallmentNoPII i ON t.installmentId = i.installmentId
      JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
      JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
      WHERE t.installmentId = :installmentId
      """)
  List<Transfer> findByInstallmentId(
    @Parameter(required = true) @Param("installmentId") Long installmentId);

}
