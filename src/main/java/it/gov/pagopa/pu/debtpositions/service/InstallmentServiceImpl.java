package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentDetailPIIViewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstallmentServiceImpl implements InstallmentService {
  private final InstallmentPIIRepository installmentPIIRepository;
  private final InstallmentMapper installmentMapper;
  private final InstallmentDetailPIIViewRepository installmentDetailPIIViewRepository;

  public InstallmentServiceImpl(InstallmentPIIRepository installmentPIIRepository, InstallmentMapper installmentMapper, InstallmentDetailPIIViewRepository installmentDetailPIIViewRepository) {
    this.installmentPIIRepository = installmentPIIRepository;
    this.installmentMapper = installmentMapper;
    this.installmentDetailPIIViewRepository = installmentDetailPIIViewRepository;
  }

  @Override
  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin) {
    return installmentPIIRepository.getByOrganizationIdAndNav(organizationId, nav, debtPositionOrigin).stream()
            .map(installmentMapper::mapToDto)
            .toList();
  }

  @Override
  public InstallmentDetailDTO getInstallmentDetail(Long installmentId, String operatorExternalUserId) {
    return installmentDetailPIIViewRepository.getInstallmentDetail(installmentId, operatorExternalUserId);
  }
}
