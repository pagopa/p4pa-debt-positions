package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.stereotype.Service;

@Service
public class TechnicalMixedDebtPositionMapper {

  public DebtPosition toTechnicalMixedDebtPosition(DebtPosition debtPosition) {
    DebtPosition technicalMixedDp = new DebtPosition();
    technicalMixedDp.setIupdOrg(debtPosition.getIupdOrg());
    technicalMixedDp.setDescription(debtPosition.getDescription());
    technicalMixedDp.setStatus(debtPosition.getStatus());
    technicalMixedDp.setDebtPositionOrigin(debtPosition.getDebtPositionOrigin());
    technicalMixedDp.setOrganizationId(debtPosition.getOrganizationId());
    technicalMixedDp.setDebtPositionTypeOrgId(debtPosition.getDebtPositionTypeOrgId());
    technicalMixedDp.setValidityDate(debtPosition.getValidityDate());
    technicalMixedDp.setFlagIuvVolatile(debtPosition.isFlagIuvVolatile());
    technicalMixedDp.setMultiDebtor(debtPosition.isMultiDebtor());
    technicalMixedDp.setFlagPuPagoPaPayment(debtPosition.isFlagPuPagoPaPayment());
    technicalMixedDp.setPaymentOptions(debtPosition.getPaymentOptions());

    return technicalMixedDp;
  }
}
