package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptArchivingView;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import org.springframework.stereotype.Service;

@Service
public class ReceiptArchivingPIIMapper {
  private final PersonalDataService personalDataService;

  public ReceiptArchivingPIIMapper(PersonalDataService personalDataService) {
    this.personalDataService = personalDataService;
  }

  public ReceiptArchivingView map(ReceiptArchivingNoPIIView noPII){
    ReceiptPIIDTO pii = personalDataService.get(noPII.getReceiptPersonalDataId(), ReceiptPIIDTO.class);

    return ReceiptArchivingView.builder()
      .receiptId(noPII.getReceiptId())
      .paymentReceiptId(noPII.getPaymentReceiptId())
      .paymentDateTime(noPII.getPaymentDateTime())
      .creditorReferenceId(noPII.getCreditorReferenceId())
      .iuv(noPII.getIuv())
      .remittanceInformation(noPII.getRemittanceInformation())
      .organizationId(noPII.getOrganizationId())
      .orgFiscalCode(noPII.getOrgFiscalCode())
      .rtFilePath(noPII.getRtFilePath())
      .debtor(pii.getDebtor())
      .payer(pii.getPayer())
      .build();
  }
}
