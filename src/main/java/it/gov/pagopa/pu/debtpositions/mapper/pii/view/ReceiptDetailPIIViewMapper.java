package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.mapper.pii.BasePIIMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import org.springframework.stereotype.Service;

@Service
public class ReceiptDetailPIIViewMapper extends BasePIIMapper<ReceiptDetailDTO, ReceiptDetailNoPIIView, InstallmentPIIDTO> {

  public ReceiptDetailPIIViewMapper(PersonalDataService personalDataService) {
    super(InstallmentPIIDTO.class, personalDataService);
  }

  @Override
  public ReceiptDetailDTO map(ReceiptDetailNoPIIView noPii) {
    InstallmentPIIDTO pii = personalDataService.get(noPii.getDebtorPersonalDataId(), InstallmentPIIDTO.class);
    return map(noPii, pii);
  }

  @Override
  protected ReceiptDetailDTO map(ReceiptDetailNoPIIView noPii, InstallmentPIIDTO pii) {
    return ReceiptDetailDTO.builder()
      .receiptId(noPii.getReceiptId())
      .iuv(noPii.getIuv())
      .nav(noPii.getNav())
      .paymentAmountCents(noPii.getPaymentAmountCents())
      .remittanceInformation(noPii.getRemittanceInformation())
      .debtPositionTypeOrgDescription(noPii.getDebtPositionTypeOrgDescription())
      .paymentDateTime(noPii.getPaymentDateTime())
      .pspCompanyName(noPii.getPspCompanyName())
      .iud(noPii.getIud())
      .iur(noPii.getIur())
      .feeCents(noPii.getFeeCents())
      .notificationFeeCents(noPii.getNotificationFeeCents())
      .debtor(pii.getDebtor())
      .receiptOrigin(noPii.getReceiptOrigin())
      .debtPositionOrigin(noPii.getDebtPositionOrigin())
      .originalRemittanceInformation(pii.getOriginalRemittanceInformation())
      .build();
  }
}
