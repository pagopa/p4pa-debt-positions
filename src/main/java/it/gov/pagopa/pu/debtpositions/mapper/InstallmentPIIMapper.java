package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class InstallmentPIIMapper {

    private final DataCipherService dataCipherService;

    public InstallmentPIIMapper(DataCipherService dataCipherService) {
        this.dataCipherService = dataCipherService;
    }

    public Pair<InstallmentNoPII, InstallmentPIIDTO> map(InstallmentDTO installment) {
        InstallmentNoPII installmentNoPII = new InstallmentNoPII();

        installmentNoPII.setInstallmentId(installment.getInstallmentId());
        installmentNoPII.setPaymentOptionId(installment.getPaymentOptionId());
        installmentNoPII.setStatus(installment.getStatus());
        installmentNoPII.setIupdPagopa(installment.getIupdPagopa());
        installmentNoPII.setIud(installment.getIud());
        installmentNoPII.setIuv(installment.getIuv());
        installmentNoPII.setIur(installment.getIur());
        installmentNoPII.setIuf(installment.getIuf());
        installmentNoPII.setDueDate(installment.getDueDate());
        installmentNoPII.setPaymentTypeCode(installment.getPaymentTypeCode());
        installmentNoPII.setAmountCents(installment.getAmountCents());
        installmentNoPII.setNotificationFeeCents(installment.getNotificationFeeCents());
        installmentNoPII.setRemittanceInformation(installment.getRemittanceInformation());
        installmentNoPII.setHumanFriendlyRemittanceInformation(installment.getHumanFriendlyRemittanceInformation());
        installmentNoPII.setBalance(installment.getBalance());
        installmentNoPII.setLegacyPaymentMetadata(installment.getLegacyPaymentMetadata());
        installmentNoPII.setDebtorEntityType(installment.getDebtor().getUniqueIdentifierType().charAt(0));
        installmentNoPII.setDebtorFiscalCodeHash(dataCipherService.hash(installment.getDebtor().getUniqueIdentifierCode()));
        installmentNoPII.setCreationDate(installment.getCreationDate());
        installmentNoPII.setUpdateDate(installment.getUpdateDate());
        installmentNoPII.setUpdateOperatorExternalId(1L); // TODO to check

        PersonDTO debtor = installment.getDebtor();
        InstallmentPIIDTO installmentPIIDTO = getInstallmentPIIDTO(debtor);

        return Pair.of(installmentNoPII, installmentPIIDTO);
    }

    private static InstallmentPIIDTO getInstallmentPIIDTO(PersonDTO debtor) {
        InstallmentPIIDTO installmentPIIDTO = new InstallmentPIIDTO();
        installmentPIIDTO.setUniqueIdentifierType(debtor.getUniqueIdentifierType());
        installmentPIIDTO.setUniqueIdentifierCode(debtor.getUniqueIdentifierCode());
        installmentPIIDTO.setFullName(debtor.getFullName());
        installmentPIIDTO.setAddress(debtor.getAddress());
        installmentPIIDTO.setCivic(debtor.getCivic());
        installmentPIIDTO.setPostalCode(debtor.getPostalCode());
        installmentPIIDTO.setLocation(debtor.getLocation());
        installmentPIIDTO.setProvince(debtor.getProvince());
        installmentPIIDTO.setNation(debtor.getNation());
        installmentPIIDTO.setEmail(debtor.getEmail());
        return installmentPIIDTO;
    }
}
