package it.gov.pagopa.pu.debtpositions.service.delete;

public interface DebtPositionDeletionService {

  String deleteDebtPosition(Long debtPositionId, String accessToken, String operatorExternalUserId);
}
