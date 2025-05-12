package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;

public interface DebtPositionTypeOrgService {
  IONotificationDTO getIONotificationDetails(Long debtPositionTypeId, PaymentEventType context);

  void deleteDebtPositionTypeOrg(Long debtPositionTypeOrgId);
  DebtPositionTypeOrg saveDebtPositionTypeOrg(SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO);
}
