package it.gov.pagopa.pu.debtpositions.service;
import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationOperationType;

public interface DebtPositionTypeOrgService {
  IONotificationDTO getIONotificationDetails(Long debtPositionTypeId, IONotificationOperationType context);
}
