package it.gov.pagopa.pu.debtpositions.service.update.massive;

public interface MassiveUpdateService {
  void updateTransferIbansAndSyncDebtPosition(Long debtPositionId, String oldIban, String newIban, String oldPostalIban, String newPostalIban, String accessToke);
}
