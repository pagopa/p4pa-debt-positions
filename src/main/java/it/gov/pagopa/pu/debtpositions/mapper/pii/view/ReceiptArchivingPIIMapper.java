package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.mapper.Base2PIIMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptArchivingView;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import org.springframework.stereotype.Service;

@Service
public class ReceiptArchivingPIIMapper extends Base2PIIMapper<ReceiptArchivingView, ReceiptArchivingNoPIIView, InstallmentPIIDTO, ReceiptPIIDTO> {

  public ReceiptArchivingPIIMapper(PersonalDataService personalDataService) {
    super(InstallmentPIIDTO.class, ReceiptPIIDTO.class, personalDataService);
  }

  public ReceiptArchivingView map(ReceiptArchivingNoPIIView noPii) {
    InstallmentPIIDTO installmentPIIDTO = personalDataService.get(noPii.getInstallmentPersonalDataId(), InstallmentPIIDTO.class);
    ReceiptPIIDTO receiptPIIDTO = personalDataService.get(noPii.getReceiptPersonalDataId(), ReceiptPIIDTO.class);

    return map(noPii, installmentPIIDTO, receiptPIIDTO);
  }

  @Override
  protected ReceiptArchivingView map(ReceiptArchivingNoPIIView noPii, InstallmentPIIDTO installmentPIIDTO, ReceiptPIIDTO receiptPIIDTO) {
    return ReceiptArchivingView.builder()
      .receiptId(noPii.getReceiptId())
      .paymentReceiptId(noPii.getPaymentReceiptId())
      .paymentDateTime(noPii.getPaymentDateTime())
      .creditorReferenceId(noPii.getCreditorReferenceId())
      .iuv(noPii.getIuv())
      .remittanceInformation(noPii.getRemittanceInformation())
      .organizationId(noPii.getOrganizationId())
      .orgFiscalCode(noPii.getOrgFiscalCode())
      .rtFilePath(noPii.getRtFilePath())
      .debtor(receiptPIIDTO.getDebtor())
      .payer(receiptPIIDTO.getPayer())
      .originalRemittanceInformation(installmentPIIDTO.getOriginalRemittanceInformation())
      .build();
  }
}
