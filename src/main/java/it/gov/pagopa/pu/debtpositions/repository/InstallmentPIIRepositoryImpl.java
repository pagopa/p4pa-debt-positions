package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class InstallmentPIIRepositoryImpl implements InstallmentPIIRepository {

    private final InstallmentPIIMapper installmentPIIMapper;
    private final PersonalDataService personalDataService;
    private final InstallmentNoPIIRepository installmentNoPIIRepository;

    public InstallmentPIIRepositoryImpl(InstallmentPIIMapper installmentPIIMapper, PersonalDataService personalDataService, InstallmentNoPIIRepository installmentNoPIIRepository) {
        this.installmentPIIMapper = installmentPIIMapper;
        this.personalDataService = personalDataService;
        this.installmentNoPIIRepository = installmentNoPIIRepository;
    }

    @Override
    public long save(Installment installment) {
        Pair<InstallmentNoPII, InstallmentPIIDTO> p = installmentPIIMapper.map(installment);
        long personalDataId = personalDataService.insert(p.getSecond(), PersonalDataType.INSTALLMENT);
        p.getFirst().setPersonalDataId(personalDataId);
        installment.setNoPII(p.getFirst());
        long newId = installmentNoPIIRepository.save(p.getFirst()).getInstallmentId();
        installment.setInstallmentId(newId);
        installment.getNoPII().setInstallmentId(newId);
        return newId;
    }
}
