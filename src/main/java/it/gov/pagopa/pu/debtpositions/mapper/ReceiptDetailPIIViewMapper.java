package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import org.springframework.stereotype.Service;

@Service
public class ReceiptDetailPIIViewMapper {

  private final PersonalDataService personalDataService;
  private final PersonMapper personMapper;

  public ReceiptDetailPIIViewMapper(PersonalDataService personalDataService,
    PersonMapper personMapper) {
    this.personalDataService = personalDataService;
    this.personMapper = personMapper;
  }

  public ReceiptDetailDTO mapToReceiptDetailDTO(ReceiptDetailNoPIIView receiptDetailNoPIIView) {
    ReceiptPIIDTO pii = personalDataService.get(
      receiptDetailNoPIIView.getDebtorPersonalDataId(),ReceiptPIIDTO.class);
    return ReceiptDetailDTO.builder()
      .receiptId(receiptDetailNoPIIView.getReceiptId())
      .iuv(receiptDetailNoPIIView.getIuv())
      .paymentAmountCents(receiptDetailNoPIIView.getPaymentAmountCents())
      .remittanceInformation(receiptDetailNoPIIView.getRemittanceInformation())
      .debtPositionDescription(receiptDetailNoPIIView.getDebtPositionDescription())
      .paymentDateTime(receiptDetailNoPIIView.getPaymentDateTime())
      .pspCompanyName(receiptDetailNoPIIView.getPspCompanyName())
      .iud(receiptDetailNoPIIView.getIud())
      .iur(receiptDetailNoPIIView.getIur())
      .debtor(pii.getDebtor()!=null?personMapper.mapToDto(pii.getDebtor()):null)
      .payer(pii.getPayer()!=null?personMapper.mapToDto(pii.getPayer()):null)
      .build();
  }
}
