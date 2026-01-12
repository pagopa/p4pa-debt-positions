package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.Action;
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
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

  @Transactional
  @Override
  public WorkflowCreatedDTO installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, WfExecutionParameters wfExecutionParameters, DebtPositionOrigin debtPositionOrigin, String accessToken, String operatorExternalUserId) {
    DebtPositionDTO debtPositionDTO = retrieveDebtPosition(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getOrganizationId(), debtPositionOrigin);

    Action action = installmentSynchronizeDTO.getAction();
    return switch (action) {
      case I ->
        installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
      case M ->
        installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
      case A ->
        installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
    };
  }

  private DebtPositionDTO retrieveDebtPosition(String iupdOrg, String iud, Long orgId, DebtPositionOrigin debtPositionOrigin) {
    if (iupdOrg != null) {
      return retrieveDebtPositionByIupd(iupdOrg, orgId, debtPositionOrigin);
    }

    return retrieveDebtPositionByIud(iud, orgId, debtPositionOrigin);
  }

  private DebtPositionDTO retrieveDebtPositionByIupd(String iupdOrg, Long orgId, DebtPositionOrigin debtPositionOrigin) {
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(iupdOrg, orgId);

    if (debtPosition == null) {
      return null;
    }

    if (!debtPositionOrigin.equals(debtPosition.getDebtPositionOrigin())) {
      throw new ConflictErrorException(String.format("[INVALID_DEBT_POSITION] There is another debt position with iupd %s requested but different origin", iupdOrg));
    }

    return debtPositionMapper.mapToDto(debtPosition);
  }

  private DebtPositionDTO retrieveDebtPositionByIud(String iud, Long orgId, DebtPositionOrigin debtPositionOrigin) {
    List<DebtPosition> debtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndInstallmentIud(orgId, iud, List.of(debtPositionOrigin));

    if(debtPositions.isEmpty()) {
      return null;
    }

    if (debtPositions.size() > 1) {
      throw new ConflictErrorException(String.format("[TOO_MANY_DEBT_POSITIONS] Multiple debt positions found for iud %s", iud));
    }

    return debtPositionMapper.mapToDto(debtPositions.getFirst());
  }
}
