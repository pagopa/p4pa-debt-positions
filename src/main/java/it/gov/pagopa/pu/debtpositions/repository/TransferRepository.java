package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RepositoryRestResource(path = "transfers")
public interface TransferRepository extends JpaRepository<Transfer, Long> {

  @Query(value = "SELECT t from Transfer t " +
    "JOIN InstallmentNoPII i ON t.installmentId = i.installmentId " +
    "JOIN PaymentOption p ON i.paymentOptionId = p.paymentOptionId " +
    "JOIN DebtPosition d ON p.debtPositionId = d.debtPositionId " +
    "JOIN DebtPositionTypeOrg dpto ON d.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId "+
    "JOIN DebtPositionType dpt ON dpto.debtPositionTypeId = dpt.debtPositionTypeId AND dpt.code <> 'MIXED' "+
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

  @RestResource(exported = false)
  @Query("""
    SELECT t
    FROM Transfer t
    JOIN InstallmentNoPII i ON t.installmentId = i.installmentId
    JOIN PaymentOption p ON i.paymentOptionId = p.paymentOptionId
    JOIN DebtPosition d ON p.debtPositionId = d.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON d.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    JOIN DebtPositionType dpt ON dpto.debtPositionTypeId = dpt.debtPositionTypeId AND dpt.code = 'MIXED'
    WHERE d.organizationId = :organizationId
    AND i.iuv = :iuv
    AND t.transferIndex = :transferIndex
    """)
  Optional<Transfer> findMixedByOrganizationIdAndIuvAndTransferIndex(Long organizationId, String iuv, int transferIndex);

  @RestResource(exported = false)
  @Query("""
    SELECT t
    FROM Transfer t
    JOIN InstallmentNoPII i ON t.installmentId = i.installmentId
    JOIN PaymentOption p ON i.paymentOptionId = p.paymentOptionId
    JOIN DebtPosition d ON p.debtPositionId = d.debtPositionId
    WHERE d.organizationId = :organizationId
    AND i.receiptId = :receiptId
    AND t.flagOwner = true
    """)
  Optional<Transfer> findOwnerTransferByOrganizationIdAndReceiptId(Long organizationId, Long receiptId);

  @RestResource(exported = false)
  @Query("""
    SELECT DISTINCT t.installmentId
    FROM Transfer t
    WHERE t.installmentId IN :installmentIds
      AND t.postalIban IS NULL
  """)
  Set<Long> findInstallmentIdsWithNullPostalIban(List<Long> installmentIds);
}
