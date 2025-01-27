package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class InstallmentPIIMapper {

  private final DataCipherService dataCipherService;

  public InstallmentPIIMapper(DataCipherService dataCipherService) {
    this.dataCipherService = dataCipherService;
  }

  public Pair<InstallmentNoPII, InstallmentPIIDTO> map(Installment installment) {
    InstallmentNoPII installmentNoPII = new InstallmentNoPII();

    installmentNoPII.setInstallmentId(installment.getInstallmentId());
    installmentNoPII.setPaymentOptionId(installment.getPaymentOptionId());
    installmentNoPII.setStatus(installment.getStatus());
    installmentNoPII.setSyncStatusFrom(installment.getSyncStatus().getSyncStatusFrom());
    installmentNoPII.setSyncStatusTo(installment.getSyncStatus().getSyncStatusTo());
    installmentNoPII.setIupdPagopa(installment.getIupdPagopa());
    installmentNoPII.setIud(installment.getIud());
    installmentNoPII.setIuv(installment.getIuv());
    installmentNoPII.setIur(installment.getIur());
    installmentNoPII.setIuf(installment.getIuf());
    installmentNoPII.setNav(installment.getNav());
    installmentNoPII.setDueDate(installment.getDueDate());
    installmentNoPII.setPaymentTypeCode(installment.getPaymentTypeCode());
    installmentNoPII.setAmountCents(installment.getAmountCents());
    installmentNoPII.setNotificationFeeCents(installment.getNotificationFeeCents());
    installmentNoPII.setRemittanceInformation(installment.getRemittanceInformation());
    installmentNoPII.setHumanFriendlyRemittanceInformation(installment.getHumanFriendlyRemittanceInformation());
    installmentNoPII.setBalance(installment.getBalance());
    installmentNoPII.setLegacyPaymentMetadata(installment.getLegacyPaymentMetadata());
    installmentNoPII.setDebtorEntityType(installment.getDebtor().getEntityType().charAt(0));
    installmentNoPII.setDebtorFiscalCodeHash(dataCipherService.hash(installment.getDebtor().getFiscalCode()));
    installmentNoPII.setCreationDate(installment.getCreationDate());
    installmentNoPII.setUpdateDate(installment.getUpdateDate());
    installmentNoPII.setUpdateOperatorExternalId(installment.getUpdateOperatorExternalId());

    if (installment.getNoPII() != null) {
      installmentNoPII.setPersonalDataId(installment.getNoPII().getPersonalDataId());
    }

    InstallmentPIIDTO installmentPIIDTO = InstallmentPIIDTO.builder()
      .debtor(installment.getDebtor()).build();

    return Pair.of(installmentNoPII, installmentPIIDTO);
  }
}
