package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.mapper.Base2PIIMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentDetailNoPIIView;
import org.springframework.stereotype.Component;

@Component
public class InstallmentDetailPIIViewMapper extends Base2PIIMapper<InstallmentDetailDTO, InstallmentDetailNoPIIView, InstallmentPIIDTO, ReceiptPIIDTO> {

  public InstallmentDetailPIIViewMapper(PersonalDataService personalDataService) {
    super(InstallmentPIIDTO.class, ReceiptPIIDTO.class, personalDataService);
  }

  @Override
  public InstallmentDetailDTO map(InstallmentDetailNoPIIView noPii) {
    InstallmentPIIDTO installmentPii = personalDataService.get(noPii.getPersonalDataId(), InstallmentPIIDTO.class);

    ReceiptPIIDTO receiptPii = null;
    if (noPii.getReceiptPersonalDataId() != null) {
      receiptPii = personalDataService.get(noPii.getReceiptPersonalDataId(), ReceiptPIIDTO.class);
    }

    return map(noPii, installmentPii, receiptPii);
  }

  @Override
  protected InstallmentDetailDTO map(InstallmentDetailNoPIIView noPii, InstallmentPIIDTO installmentPii, ReceiptPIIDTO receiptPii) {
    return InstallmentDetailDTO.builder()
      .installmentId(noPii.getInstallmentId())
      .receiptId(noPii.getReceiptId())
      .paymentOptionId(noPii.getPaymentOptionId())
      .status(noPii.getStatus())
      .iuv(noPii.getIuv())
      .nav(noPii.getNav())
      .amountCents(noPii.getAmountCents())
      .dueDate(noPii.getDueDate())
      .debtor(installmentPii.getDebtor())
      .debtPositionTypeOrgDescription(noPii.getDebtPositionTypeOrgDescription())
      .debtPositionDescription(noPii.getDebtPositionDescription())
      .debtPositionId(noPii.getDebtPositionId())
      .debtPositionOrigin(noPii.getDebtPositionOrigin())
      .paymentDateTime(noPii.getPaymentDateTime())
      .payer(getPayer(receiptPii))
      .pspCompanyName(noPii.getPspCompanyName())
      .iud(noPii.getIud())
      .iur(noPii.getIur())
      .iun(noPii.getIun())
      .notificationDate(noPii.getNotificationDate())
      .notificationFeeCents(noPii.getNotificationFeeCents())
      .originalRemittanceInformation(installmentPii.getOriginalRemittanceInformation())
      .build();
  }

  private PersonDTO getPayer(ReceiptPIIDTO receiptPii) {
    if (receiptPii != null) {
      return receiptPii.getPayer();
    } else {
      return null;
    }
  }
}
