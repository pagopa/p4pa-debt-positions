package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.stereotype.Service;

@Service
public class InstallmentPaidPIIMapper {

  private final PersonalDataService personalDataService;

  public InstallmentPaidPIIMapper(PersonalDataService personalDataService) {
    this.personalDataService = personalDataService;
  }

  public InstallmentPaidViewDTO map(InstallmentPaidViewNoPII noPii) {
    ReceiptPIIDTO pii = personalDataService.get(noPii.getReceiptPersonalDataId(), ReceiptPIIDTO.class);

    return InstallmentPaidViewDTO.builder()
            .installmentId(noPii.getInstallmentId())
            .iuf(noPii.getIuf())
            .iud(noPii.getIud())
            .noticeNumber(noPii.getNoticeNumber())
            .orgFiscalCode(noPii.getOrgFiscalCode())
            .paymentReceiptId(noPii.getPaymentReceiptId())
            .paymentDateTime(noPii.getPaymentDateTime())
            .idPsp(noPii.getIdPsp())
            .pspCompanyName(noPii.getPspCompanyName())
            .debtor(pii.getDebtor())
            .payer(pii.getPayer())
            .paymentAmountCents(noPii.getPaymentAmountCents())
            .creditorReferenceId(noPii.getCreditorReferenceId())
            .amountCents(noPii.getAmountCents())
            .remittanceInformation(noPii.getRemittanceInformation())
            .category(noPii.getCategory())
            .code(noPii.getCode())
            .transferIndex(noPii.getTransferIndex())
            .feeCents(noPii.getFeeCents())
            .balance(noPii.getBalance())
            .companyName(noPii.getCompanyName())
            .build();
  }
}
