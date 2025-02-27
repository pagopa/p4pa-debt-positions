package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
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
    return InstallmentDetailDTO.builder()
      .installmentId(installmentDetailNoPIIView.getInstallmentId())
      .receiptId(installmentDetailNoPIIView.getReceiptId())
      .paymentOptionId(installmentDetailNoPIIView.getPaymentOptionId())
      .status(installmentDetailNoPIIView.getStatus())
      .iuv(installmentDetailNoPIIView.getIuv())
      .amountCents(installmentDetailNoPIIView.getAmountCents())
      .dueDate(installmentDetailNoPIIView.getDueDate())
      .debtor(personMapper.mapToDto(installmentPii.getDebtor()))
      .debtPositionTypeOrgDescription(installmentDetailNoPIIView.getDebtPositionTypeOrgDescription())
      .debtPositionDescription(installmentDetailNoPIIView.getDebtPositionDescription())
      .debtPositionId(installmentDetailNoPIIView.getDebtPositionId())
      .paymentDateTime(installmentDetailNoPIIView.getPaymentDateTime())
      .payer(getPayer(installmentDetailNoPIIView.getReceiptPersonalDataId()))
      .pspCompanyName(installmentDetailNoPIIView.getPspCompanyName())
      .iud(installmentDetailNoPIIView.getIud())
      .iur(installmentDetailNoPIIView.getIur())
      .build();
  }

  private PersonDTO getPayer(Long receiptPersonalDataId) {
    if (receiptPersonalDataId != null) {
      ReceiptPIIDTO receiptPii = personalDataService.get(receiptPersonalDataId, ReceiptPIIDTO.class);
      if (receiptPii != null && receiptPii.getPayer() != null) {
        return personMapper.mapToDto(receiptPii.getPayer());
      }
    }
    return null;
  }

}
