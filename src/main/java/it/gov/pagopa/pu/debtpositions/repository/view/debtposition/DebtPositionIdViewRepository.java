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
  /**
   * Retrieves a paginated list of debt position identifiers (DebtPositionIdView).
   * The query excludes debt positions with dpto of type MIXED and origin SPONTANEOUS_MIXED, and applies
   * filtering logic based on the installment status (e.g., TO_SYNC, EXPIRED) and
   * the presence of synchronization errors.
   * The query filters by the provided transfer IBAN and postal IBAN. If the postalIban
   * parameter is null, it searches for transfers where the postal IBAN is absent (NULL in the database).
   * Regarding the DebtPositionTypeOrg (dpto): if a dptoId is provided, it filters by this identifier;
   * if dptoId is null, it requires the dpto IBAN to be null.
   *
   * @param organizationId the id of organization
   * @param iban The bank IBAN associated with the transfer
   * @param postalIban The postal IBAN associated with the transfer
   * @param syncError Indicates whether to filter by installments with synchronization errors (true) or without (false)
   * @param dptoId The DebtPositionTypeOrg identifier (optional)
   * @param installmentStatuses The list of target installment statuses
   * @param pageable The pagination and sorting information for the request
   * @return A list of {@link DebtPositionIdView} matching the search criteria
   */
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
        (:dptoId IS NULL AND dpto.iban IS NULL)
        OR (:dptoId IS NOT NULL AND d.debtPositionTypeOrgId = :dptoId)
      )
      AND ((:postalIban IS NULL AND t.postalIban IS NULL) OR t.postalIban = :postalIban)
      AND t.iban = :iban
  """)
  List<DebtPositionIdView> getDebtPositionIdsByIbansAndDptoId(
    @Param("organizationId") Long organizationId,
    @Param(("iban")) String iban,
    @RequestParam(required = false) String postalIban,
    @Param(value = "syncError") Boolean syncError,
    @RequestParam(required = false) Long dptoId,
    @Param(("installmentStatuses")) List<InstallmentStatus> installmentStatuses,
    Pageable pageable
  );
}
