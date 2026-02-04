package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentViewDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import org.springframework.stereotype.Component;

@Component
public class InstallmentViewDTOMapper {

  private final PersonalDataService personalDataService;

  public InstallmentViewDTOMapper(PersonalDataService personalDataService) {
    this.personalDataService = personalDataService;
  }

  public InstallmentViewDTO map(InstallmentViewNoPII noPii) {
    InstallmentPIIDTO pii = personalDataService.get(noPii.getPersonalDataId(), InstallmentPIIDTO.class);
    return InstallmentViewDTO.builder()
      .installmentId(noPii.getInstallmentId())
      .debtPositionId(noPii.getDebtPositionId())
      .paymentOptionId(noPii.getPaymentOptionId())
      .receiptId(noPii.getReceiptId())
      .iuv(noPii.getIuv())
      .iud(noPii.getIud())
      .status(noPii.getStatus())
      .nav(noPii.getNav())
      .dueDate(noPii.getDueDate())
      .amountCents(noPii.getAmountCents())
      .remittanceInformation(noPii.getRemittanceInformation())
      .originalRemittanceInformation(pii.getOriginalRemittanceInformation())
      .debtorFiscalCodeHash(noPii.getDebtorFiscalCodeHash())
      .debtPositionTypeOrgDescription(noPii.getDebtPositionTypeOrgDescription())
      .build();
  }
}
