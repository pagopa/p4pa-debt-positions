package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstallmentPIIRepositoryImpl extends BasePIIRepository<Installment, InstallmentNoPII, InstallmentPIIDTO, Long> implements InstallmentPIIRepository {

  private final InstallmentPIIMapper installmentPIIMapper;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;

  public InstallmentPIIRepositoryImpl(InstallmentPIIMapper installmentPIIMapper, PersonalDataService personalDataService, InstallmentNoPIIRepository installmentNoPIIRepository) {
    super(installmentPIIMapper, personalDataService, installmentNoPIIRepository);
    this.installmentPIIMapper = installmentPIIMapper;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
  }

  @Override
  public List<Installment> getByOrganizationIdAndNav(Long organizationId, String nav) {
    return installmentNoPIIRepository.getByOrganizationIdAndNav(organizationId, nav, null)
      .stream().map(installmentPIIMapper::map).toList();
  }

  @Override
  void setId(Installment fullDTO, Long id) {
    fullDTO.setInstallmentId(id);
  }

  @Override
  void setId(InstallmentNoPII noPii, Long id) {
  noPii.setInstallmentId(id);
  }

  @Override
  Long getId(InstallmentNoPII noPii) {
    return noPii.getInstallmentId();
  }

  @Override
  Class<InstallmentPIIDTO> getPIITDTOClass() {
    return InstallmentPIIDTO.class;
  }

  @Override
  PersonalDataType getPIIPersonalDataType() {
    return PersonalDataType.INSTALLMENT;
  }
}
