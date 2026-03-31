package it.gov.pagopa.pu.debtpositions.repository.view.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.view.debtposition.DebtPositionIdView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RepositoryRestResource(path = "debt-position-id-view")
public interface DebtPositionIdViewRepository extends JpaRepository<DebtPositionIdView, Long> {
  @Query("""
    SELECT DISTINCT new DebtPositionIdView(d.debtPositionId as debtPositionId)
    FROM DebtPosition d
      JOIN d.paymentOptions p
      JOIN p.installments i
      JOIN i.transfers t
      JOIN DebtPositionTypeOrg dpto on d.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    WHERE d.organizationId = :organizationId
      AND dpto.debtPositionTypeId <> :#{T(it.gov.pagopa.pu.debtpositions.util.Constants).DEBT_POSITION_TYPE_MIXED}
      AND d.debtPositionOrigin <> :#{T(it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin).SPONTANEOUS_MIXED}
      AND i.status IN :installmentStatuses
      AND (
        (i.status = :#{T(it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus).TO_SYNC}
          AND ((:syncError = true AND i.syncStatus.syncError IS NOT NULL) OR (:syncError = false AND i.syncStatus.syncError IS NULL))
        )
        OR (i.status = :#{T(it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus).EXPIRED} AND i.updateDate >= :#{T(java.time.LocalDateTime).now().minusYears(@environment.getProperty('massive-update.expired-threshold-years', T(java.lang.Integer)))})
        OR (i.status NOT IN (:#{T(it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus).TO_SYNC}, :#{T(it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus).EXPIRED}))
      )
      AND (
        (:dptoId IS NULL AND dpto.iban IS NULL AND t.postalIban = :postalIban AND t.iban= :iban)
        OR (:dptoId IS NOT NULL AND d.debtPositionTypeOrgId = :dptoId AND t.postalIban = :postalIban AND t.iban = :iban)
      )
  """)
  List<DebtPositionIdView> getDebtPositionIdsByIbansAndDptoId(
    @Param("organizationId") Long organizationId,
    @Param(("iban")) String iban,
    @Param("postalIban") String postalIban,
    @Param(value = "syncError") Boolean syncError,
    @RequestParam(value = "dptoId", required = false) Long dptoId,
    @Param(("installmentStatuses")) List<InstallmentStatus> installmentStatuses,
    Pageable pageable
  );
}
