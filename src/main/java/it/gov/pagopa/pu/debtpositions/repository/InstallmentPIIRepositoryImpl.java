package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.Constants;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
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
    public long save(InstallmentDTO installment) {
        Pair<InstallmentNoPII, InstallmentPIIDTO> p = installmentPIIMapper.map(installment);
        long personalDataId = personalDataService.insert(p.getSecond(), Constants.PERSONAL_DATA_TYPE.INSTALLMENT);
        p.getFirst().setPersonalDataId(personalDataId);
        long newId = installmentNoPIIRepository.save(p.getFirst()).getInstallmentId();
        installment.setInstallmentId(newId);
        return newId;
    }
}
