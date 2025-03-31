package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentDetailPIIViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentDetailNoPIIView;
import org.springframework.stereotype.Service;

@Service
public class InstallmentDetailPIIViewRepositoryImpl implements InstallmentDetailPIIViewRepository {

  private final InstallmentDetailNoPIIViewRepository installmentDetailNoPIIViewRepository;
  private final InstallmentDetailPIIViewMapper installmentDetailPIIViewMapper;

  public InstallmentDetailPIIViewRepositoryImpl(
    InstallmentDetailNoPIIViewRepository installmentDetailNoPIIViewRepository,
    InstallmentDetailPIIViewMapper installmentDetailPIIViewMapper) {
    this.installmentDetailNoPIIViewRepository = installmentDetailNoPIIViewRepository;
    this.installmentDetailPIIViewMapper = installmentDetailPIIViewMapper;
  }

  @Override
  public InstallmentDetailDTO getInstallmentDetail(Long installmentId, String operatorExternalUserId) {
    InstallmentDetailNoPIIView installmentDetailNoPIIView = installmentDetailNoPIIViewRepository.findInstallmentDetailView(installmentId, operatorExternalUserId)
      .orElseThrow(() -> new NotFoundException(
        "InstallmentDetailNoPIIView having installmentId %d and operatorExternalUserId %s not found".formatted(
          installmentId, operatorExternalUserId)));
    return installmentDetailPIIViewMapper.mapToInstallmentDetailDTO(installmentDetailNoPIIView);
  }

}
