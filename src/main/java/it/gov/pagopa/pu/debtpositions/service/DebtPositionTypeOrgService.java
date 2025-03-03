package it.gov.pagopa.pu.debtpositions.service;
import it.gov.pagopa.pu.debtpositions.dto.generated.AppIONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentEventType;

public interface DebtPositionTypeOrgService {
  AppIONotificationDTO getAppIONotificationDetails(Long debtPositionTypeId, PaymentEventType context);
}
