package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.mapper.Base2PIIMapper;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.view.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.stereotype.Service;

@Service
public class InstallmentPaidPIIMapper extends Base2PIIMapper<InstallmentPaidViewDTO, InstallmentPaidViewNoPII, InstallmentPIIDTO, ReceiptPIIDTO> {

  public InstallmentPaidPIIMapper(PersonalDataService personalDataService) {
    super(InstallmentPIIDTO.class, ReceiptPIIDTO.class, personalDataService);
  }

  @Override
  public InstallmentPaidViewDTO map(InstallmentPaidViewNoPII noPii) {
    InstallmentPIIDTO installmentPIIDTO = personalDataService.get(noPii.getPersonalDataId(), InstallmentPIIDTO.class);
    ReceiptPIIDTO receiptPIIDTO = personalDataService.get(noPii.getReceiptPersonalDataId(), ReceiptPIIDTO.class);

    return map(noPii, installmentPIIDTO, receiptPIIDTO);
  }

  @Override
  protected InstallmentPaidViewDTO map(InstallmentPaidViewNoPII noPii, InstallmentPIIDTO installmentPIIDTO, ReceiptPIIDTO receiptPIIDTO) {
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
