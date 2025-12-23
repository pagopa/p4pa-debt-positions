package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.IuvSequenceNumber;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface IuvSequenceNumberRepository extends JpaRepository<IuvSequenceNumber, Long> {

  IuvSequenceNumber findByOrganizationId(Long organizationId);

  @Transactional(Transactional.TxType.MANDATORY)
  @Modifying(flushAutomatically = true)
  @Query("INSERT INTO IuvSequenceNumber (organizationId, sequenceNumber, updateDate, updateOperatorExternalId, updateTraceId)" +
    " VALUES(:organizationId, 1, CURRENT_TIMESTAMP, :currentUserExternalId, :traceId)" +
    " ON CONFLICT (organizationId) DO UPDATE SET sequenceNumber = sequenceNumber + 1, updateDate = CURRENT_TIMESTAMP, updateOperatorExternalId = :currentUserExternalId, updateTraceId = :traceId")
  void genNextIuvSequenceNumber(Long organizationId, String currentUserExternalId, String traceId);

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  default IuvSequenceNumber getNextIuvSequenceNumber(Long organizationId) {
    genNextIuvSequenceNumber(organizationId, SecurityUtils.getCurrentUserExternalId(), Utilities.getTraceId());
    return findByOrganizationId(organizationId);
  }
}
