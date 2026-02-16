package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.view.InstallmentViewDTO;
import it.gov.pagopa.pu.common.pii.mapper.BasePIIMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import org.springframework.stereotype.Component;

@Component
public class InstallmentViewDTOMapper extends BasePIIMapper<InstallmentViewDTO, InstallmentViewNoPII, InstallmentPIIDTO> {

  public InstallmentViewDTOMapper(PersonalDataService personalDataService) {
    super(InstallmentPIIDTO.class, personalDataService);
  }

  public InstallmentViewDTO map(InstallmentViewNoPII noPii) {
    InstallmentPIIDTO pii = personalDataService.get(noPii.getPersonalDataId(), InstallmentPIIDTO.class);
    return map(noPii, pii);
  }

  @Override
  protected InstallmentViewDTO map(InstallmentViewNoPII noPii, InstallmentPIIDTO pii) {
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
