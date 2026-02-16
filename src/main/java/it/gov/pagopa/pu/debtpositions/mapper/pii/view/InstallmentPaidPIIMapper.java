package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
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
    ReceiptPIIDTO receiptPIIDTO = personalDataService.get(noPii.getReceiptPersonalDataId(), ReceiptPIIDTO.class);
    InstallmentPIIDTO installmentPIIDTO = personalDataService.get(noPii.getPersonalDataId(), InstallmentPIIDTO.class);

    return InstallmentPaidViewDTO.builder()
      .installmentId(noPii.getInstallmentId())
      .iuf(noPii.getIuf())
      .iud(noPii.getIud())
      .noticeNumber(noPii.getNoticeNumber())
      .organizationId(noPii.getOrganizationId())
      .orgFiscalCode(noPii.getOrgFiscalCode())
      .paymentReceiptId(noPii.getPaymentReceiptId())
      .paymentDateTime(noPii.getPaymentDateTime())
      .idPsp(noPii.getIdPsp())
      .pspCompanyName(noPii.getPspCompanyName())
      .debtor(receiptPIIDTO.getDebtor())
      .payer(receiptPIIDTO.getPayer())
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
      .rtFilePath(noPii.getRtFilePath())
      .iun(noPii.getIun())
      .notificationDate(noPii.getNotificationDate())
      .notificationFeeCents(noPii.getNotificationFeeCents())
      .originalRemittanceInformation(installmentPIIDTO.getOriginalRemittanceInformation())
      .build();
  }
}
