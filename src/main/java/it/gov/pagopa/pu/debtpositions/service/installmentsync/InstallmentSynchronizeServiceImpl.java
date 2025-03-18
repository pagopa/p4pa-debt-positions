package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeCancelService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeInsertService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InstallmentSynchronizeServiceImpl implements InstallmentSynchronizeService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionMapper debtPositionMapper;
  private final InstallmentSynchronizeCancelService installmentSynchronizeCancelService;
  private final InstallmentSynchronizeUpdateService installmentSynchronizeUpdateService;
  private final InstallmentSynchronizeInsertService installmentSynchronizeInsertService;

  public InstallmentSynchronizeServiceImpl(DebtPositionRepository debtPositionRepository, DebtPositionMapper debtPositionMapper, InstallmentSynchronizeCancelService installmentSynchronizeCancelService, InstallmentSynchronizeUpdateService installmentSynchronizeUpdateService, InstallmentSynchronizeInsertService installmentSynchronizeInsertService) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionMapper = debtPositionMapper;
    this.installmentSynchronizeCancelService = installmentSynchronizeCancelService;
    this.installmentSynchronizeUpdateService = installmentSynchronizeUpdateService;
    this.installmentSynchronizeInsertService = installmentSynchronizeInsertService;
  }

  @Override
  public String installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, WfExecutionParameters wfExecutionParameters, DebtPositionOrigin debtPositionOrigin, String accessToken, String operatorExternalUserId) {
    DebtPositionDTO debtPositionDTO = retrieveAndVerifyOrigin(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getOrganizationId(), debtPositionOrigin);

    InstallmentSynchronizeDTO.ActionEnum action = installmentSynchronizeDTO.getAction();
    return switch (action) {
      case InstallmentSynchronizeDTO.ActionEnum.I ->
        installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
      case InstallmentSynchronizeDTO.ActionEnum.M ->
        installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
      case InstallmentSynchronizeDTO.ActionEnum.A ->
        installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
    };
  }

  private DebtPositionDTO retrieveAndVerifyOrigin(String iupdOrg, Long orgId, DebtPositionOrigin debtPositionOrigin) {
    DebtPosition debtPosition = debtPositionRepository.findByIupdOrgAndOrganizationId(iupdOrg, orgId);

    if (debtPosition == null){
      return null;
    }

    if (!debtPositionOrigin.equals(debtPosition.getDebtPositionOrigin())) {
      throw new ConflictErrorException(String.format("There is another debt position with iupd %s requested but different origin", iupdOrg));
    }
    return debtPositionMapper.mapToDto(debtPosition);
  }
}
