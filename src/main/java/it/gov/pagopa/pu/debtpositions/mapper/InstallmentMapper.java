package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import org.springframework.stereotype.Service;

@Service
public class InstallmentMapper {

  private final PersonMapper personMapper;

  public InstallmentMapper(PersonMapper personMapper) {
    this.personMapper = personMapper;
  }

  public Installment mapToModel(InstallmentDTO dto) {
    Installment installment = new Installment();
    installment.setInstallmentId(dto.getInstallmentId());
    installment.setPaymentOptionId(dto.getPaymentOptionId());
    installment.setStatus(dto.getStatus());
    installment.setIupdPagopa(dto.getIupdPagopa());
    installment.setIud(dto.getIud());
    installment.setIuv(dto.getIuv());
    installment.setIur(dto.getIur());
    installment.setIuf(dto.getIuf());
    installment.setNav(dto.getNav());
    installment.setDueDate(dto.getDueDate());
    installment.setPaymentTypeCode(dto.getPaymentTypeCode());
    installment.setAmountCents(dto.getAmountCents());
    installment.setNotificationFeeCents(dto.getNotificationFeeCents());
    installment.setRemittanceInformation(dto.getRemittanceInformation());
    installment.setLegacyPaymentMetadata(dto.getLegacyPaymentMetadata());
    installment.setHumanFriendlyRemittanceInformation(dto.getHumanFriendlyRemittanceInformation());
    installment.setBalance(dto.getBalance());
    installment.setDebtor(personMapper.mapToModel(dto.getDebtor()));
    installment.setCreationDate(dto.getCreationDate());
    installment.setUpdateDate(dto.getUpdateDate());
    return installment;
  }

}
