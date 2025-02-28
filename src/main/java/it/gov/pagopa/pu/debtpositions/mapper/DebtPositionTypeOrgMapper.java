package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionTypeOrgMapper {

  public DebtPositionTypeOrg mapFromDebtPositionType(DebtPositionType debtPositionType, Long organizationId) {
    return DebtPositionTypeOrg.builder()
      .debtPositionTypeId(debtPositionType.getDebtPositionTypeId())
      .organizationId(organizationId)
      .code(debtPositionType.getCode())
      .description(debtPositionType.getDescription())
      .iban(null)
      .postalIban(null)
      .postalAccountCode(null)
      .holderPostalCc(null)
      .orgSector(null)
      .xsdDefinitionRef(null)
      .amountCents(null)
      .externalPaymentUrl(null)
      .flagAnonymousFiscalCode(debtPositionType.isFlagAnonymousFiscalCode())
      .flagMandatoryDueDate(debtPositionType.isFlagMandatoryDueDate())
      .flagSpontaneous(false)
      .flagNotifyIo(debtPositionType.isFlagNotifyIo())
      .ioTemplateMessage(debtPositionType.getIoTemplateMessage())
      .flagActive(false)
      .flagNotifyOutcomePush(false)
      .notifyOutcomePushOrgSilServiceId(null)
      .flagAmountActualization(false)
      .amountActualizationOrgSilServiceId(null)
      .flagExternal(false)
      .serviceId(null)
      .subject(null)
      .markdown(null)
      .creationDate(null)
      .updateDate(null)
      .updateOperatorExternalId(null)
      .build();
  }
}
