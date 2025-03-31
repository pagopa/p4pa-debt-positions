package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptArchivingView;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import org.springframework.stereotype.Service;

@Service
public class ReceiptArchivingPIIMapping {
  private final PersonalDataService personalDataService;
  private final PersonMapper personMapper;

  public ReceiptArchivingPIIMapping(PersonalDataService personalDataService, PersonMapper personMapper) {
    this.personalDataService = personalDataService;
    this.personMapper = personMapper;
  }

  public ReceiptArchivingView map(ReceiptArchivingNoPIIView noPII){
    InstallmentPIIDTO installmentPIIDTO = personalDataService.get(noPII.getInstallmentPersonalDataId(), InstallmentPIIDTO.class);

    return ReceiptArchivingView.builder()
      .receiptId(noPII.getReceiptId())
      .paymentReceiptId(noPII.getPaymentReceiptId())
      .paymentDateTime(noPII.getPaymentDateTime())
      .creditorReferenceId(noPII.getCreditorReferenceId())
      .iuv(noPII.getIuv())
      .remittanceInformation(noPII.getRemittanceInformation())
      .orgFiscalCode(noPII.getOrgFiscalCode())
      .debtor(personMapper.mapToDto(installmentPIIDTO.getDebtor()))
      .build();
  }
}
