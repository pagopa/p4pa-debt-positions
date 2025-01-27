package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstallmentServiceImpl implements InstallmentService {
  private final InstallmentPIIRepository installmentPIIRepository;
  private final InstallmentMapper installmentMapper;

  public InstallmentServiceImpl(InstallmentPIIRepository installmentPIIRepository, InstallmentMapper installmentMapper) {
    this.installmentPIIRepository = installmentPIIRepository;
    this.installmentMapper = installmentMapper;
  }

  @Override
  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav) {
    return installmentPIIRepository.getByOrganizationIdAndNav(organizationId, nav).stream()
            .map(installmentMapper::mapToDto)
            .toList();
  }
}
