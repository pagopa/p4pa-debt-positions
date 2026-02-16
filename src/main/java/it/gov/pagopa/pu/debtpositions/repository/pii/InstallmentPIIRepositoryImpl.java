package it.gov.pagopa.pu.debtpositions.repository.pii;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.mapper.pii.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstallmentPIIRepositoryImpl extends BasePIIRepository<InstallmentDTO, InstallmentNoPII, InstallmentPIIDTO, Long> implements InstallmentPIIRepository {

  private final InstallmentPIIMapper installmentPIIMapper;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;

  public InstallmentPIIRepositoryImpl(InstallmentPIIMapper installmentPIIMapper, PersonalDataService personalDataService, InstallmentNoPIIRepository installmentNoPIIRepository) {
    super(installmentPIIMapper, personalDataService, installmentNoPIIRepository);
    this.installmentPIIMapper = installmentPIIMapper;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
  }

  @Override
  public List<InstallmentDTO> getByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin) {
    return installmentPIIMapper.mapAll(installmentNoPIIRepository.getByOrganizationIdAndNav(organizationId, nav, debtPositionOrigin));
  }

  @Override
  public List<InstallmentDTO> getByOrganizationIdAndReceiptId(Long organizationId, Long receiptId, List<DebtPositionOrigin> debtPositionOrigin) {
    return installmentPIIMapper.mapAll(installmentNoPIIRepository.getByOrganizationIdAndReceiptId(organizationId, receiptId, debtPositionOrigin));
  }

  @Override
  public List<InstallmentDTO> findByIuvOrNav(String iuvOrNav, String debtorFiscalCode, Long organizationId, List<InstallmentStatus> statuses) {
    return installmentPIIMapper.mapAll(installmentNoPIIRepository.findByIuvOrNav(iuvOrNav,debtorFiscalCode,organizationId, statuses));
  }

  @Override
  void setId(InstallmentDTO fullDTO, Long id) {
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
