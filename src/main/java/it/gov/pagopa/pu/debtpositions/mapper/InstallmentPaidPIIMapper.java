package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.stereotype.Service;

@Service
public class InstallmentPaidPIIMapper extends BasePIIMapper<InstallmentPaidViewDTO, InstallmentPaidViewNoPII, InstallmentPIIDTO> {

  private final PersonalDataService personalDataService;

  public InstallmentPaidPIIMapper(PersonalDataService personalDataService) {
    this.personalDataService = personalDataService;
  }

  @Override
  protected InstallmentPaidViewNoPII extractNoPiiEntity(InstallmentPaidViewDTO fullDTO) {
    InstallmentPaidViewNoPII installmentPaidViewNoPII = new InstallmentPaidViewNoPII();

    installmentPaidViewNoPII.setInstallmentId(fullDTO.getInstallmentId());
    installmentPaidViewNoPII.setIuf(fullDTO.getIuf());
    installmentPaidViewNoPII.setIud(fullDTO.getIud());
    installmentPaidViewNoPII.setNoticeNumber(fullDTO.getNoticeNumber());
    installmentPaidViewNoPII.setOrgFiscalCode(fullDTO.getOrgFiscalCode());
    installmentPaidViewNoPII.setPaymentReceiptId(fullDTO.getPaymentReceiptId());
    installmentPaidViewNoPII.setPaymentDateTime(fullDTO.getPaymentDateTime());
    installmentPaidViewNoPII.setIdPsp(fullDTO.getIdPsp());
    installmentPaidViewNoPII.setPspCompanyName(fullDTO.getPspCompanyName());
    installmentPaidViewNoPII.setPaymentAmountCents(fullDTO.getPaymentAmountCents());
    installmentPaidViewNoPII.setCreditorReferenceId(fullDTO.getCreditorReferenceId());
    installmentPaidViewNoPII.setAmountCents(fullDTO.getAmountCents());
    installmentPaidViewNoPII.setRemittanceInformation(fullDTO.getRemittanceInformation());
    installmentPaidViewNoPII.setCategory(fullDTO.getCategory());
    installmentPaidViewNoPII.setCode(fullDTO.getCode());
    installmentPaidViewNoPII.setTransferIndex(fullDTO.getTransferIndex());
    installmentPaidViewNoPII.setFeeCents(fullDTO.getFeeCents());
    installmentPaidViewNoPII.setBalance(fullDTO.getBalance());
    installmentPaidViewNoPII.setCompanyName(fullDTO.getCompanyName());

     return installmentPaidViewNoPII;
  }

  @Override
  protected InstallmentPIIDTO extractPiiDto(InstallmentPaidViewDTO fullDTO) {
    return InstallmentPIIDTO.builder()
            .debtor(fullDTO.getDebtor())
            .build();
  }

  @Override
  public InstallmentPaidViewDTO map(InstallmentPaidViewNoPII noPii) {
    InstallmentPIIDTO pii = personalDataService.get(noPii.getPersonalDataId(), InstallmentPIIDTO.class);

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
            .noPII(noPii)
            .build();
  }
}
