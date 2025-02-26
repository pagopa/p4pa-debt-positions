package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentDetailNoPIIView;
import org.springframework.stereotype.Component;

@Component
public class InstallmentDetailPIIViewMapper {

  private final PersonalDataService personalDataService;
  private final PersonMapper personMapper;

  public InstallmentDetailPIIViewMapper(
    PersonalDataService personalDataService,
    PersonMapper personMapper) {
    this.personalDataService = personalDataService;
    this.personMapper = personMapper;
  }

  public InstallmentDetailDTO mapToInstallmentDetailDTO(InstallmentDetailNoPIIView installmentDetailNoPIIView) {
    InstallmentPIIDTO installmentPii = personalDataService.get(
      installmentDetailNoPIIView.getPersonalDataId(), InstallmentPIIDTO.class);
    ReceiptPIIDTO receiptPii = personalDataService.get(
      installmentDetailNoPIIView.getReceiptPersonalDataId(), ReceiptPIIDTO.class);
    return InstallmentDetailDTO.builder()
      .installmentId(installmentDetailNoPIIView.getInstallmentId())
      .receiptId(installmentDetailNoPIIView.getReceiptId())
      .paymentOptionId(installmentDetailNoPIIView.getPaymentOptionId())
      .status(installmentDetailNoPIIView.getStatus())
      .iuv(installmentDetailNoPIIView.getIuv())
      .amountCents(installmentDetailNoPIIView.getAmountCents())
      .dueDate(installmentDetailNoPIIView.getDueDate())
      .debtor(installmentPii.getDebtor() != null ? personMapper.mapToDto(installmentPii.getDebtor()) : null)
      .debtPositionTypeOrgDescription(installmentDetailNoPIIView.getDebtPositionTypeOrgDescription())
      .debtPositionDescription(installmentDetailNoPIIView.getDebtPositionDescription())
      .debtPositionId(installmentDetailNoPIIView.getDebtPositionId())
      .paymentDateTime(installmentDetailNoPIIView.getPaymentDateTime())
      .payer(receiptPii.getPayer() != null ? personMapper.mapToDto(receiptPii.getPayer()) : null)
      .pspCompanyName(installmentDetailNoPIIView.getPspCompanyName())
      .iud(installmentDetailNoPIIView.getIud())
      .iur(installmentDetailNoPIIView.getIur())
      .build();
  }

}
