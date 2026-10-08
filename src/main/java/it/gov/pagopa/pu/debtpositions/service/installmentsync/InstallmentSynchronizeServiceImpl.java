package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.Action;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.common.ConflictException;
import it.gov.pagopa.pu.debtpositions.exception.common.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeCancelService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeInsertService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeUpdateService;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.organization.dto.generated.PagoPaInteractionModel;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class InstallmentSynchronizeServiceImpl implements InstallmentSynchronizeService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionMapper debtPositionMapper;
  private final InstallmentSynchronizeCancelService installmentSynchronizeCancelService;
  private final InstallmentSynchronizeUpdateService installmentSynchronizeUpdateService;
  private final InstallmentSynchronizeInsertService installmentSynchronizeInsertService;
  private final OrganizationService organizationService;

  public InstallmentSynchronizeServiceImpl(DebtPositionRepository debtPositionRepository, DebtPositionMapper debtPositionMapper, InstallmentSynchronizeCancelService installmentSynchronizeCancelService, InstallmentSynchronizeUpdateService installmentSynchronizeUpdateService, InstallmentSynchronizeInsertService installmentSynchronizeInsertService, OrganizationService organizationService) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionMapper = debtPositionMapper;
    this.installmentSynchronizeCancelService = installmentSynchronizeCancelService;
    this.installmentSynchronizeUpdateService = installmentSynchronizeUpdateService;
    this.installmentSynchronizeInsertService = installmentSynchronizeInsertService;
    this.organizationService = organizationService;
  }

  @Transactional
  @Override
  public WorkflowCreatedDTO installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, WfExecutionParameters wfExecutionParameters, DebtPositionOrigin debtPositionOrigin, String accessToken, String operatorExternalUserId) {
    DebtPositionDTO debtPositionDTO = retrieveDebtPosition(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getOrganizationId(), debtPositionOrigin);

    Action action = installmentSynchronizeDTO.getAction();
    return switch (action) {
      case I -> {
        validate(installmentSynchronizeDTO, accessToken);
        yield installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
      }
      case M -> {
        validate(installmentSynchronizeDTO, accessToken);
        yield installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
      }
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
      throw new ConflictException(ErrorCodeConstants.ERROR_CODE_INVALID_DEBT_POSITION, String.format("There is another debt position with iupd %s requested but different origin", iupdOrg));
    }

    return debtPositionMapper.mapToDto(debtPosition);
  }

  private DebtPositionDTO retrieveDebtPositionByIud(String iud, Long orgId, DebtPositionOrigin debtPositionOrigin) {
    List<DebtPosition> debtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndInstallmentIud(orgId, iud, List.of(debtPositionOrigin));

    if(debtPositions.isEmpty()) {
      return null;
    }

    if (debtPositions.size() > 1) {
      throw new ConflictException(ErrorCodeConstants.ERROR_CODE_TOO_MANY_DEBT_POSITIONS, String.format("Multiple debt positions found for iud %s", iud));
    }

    return debtPositionMapper.mapToDto(debtPositions.getFirst());
  }

  private void validate(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken) {
    Long orgId = installmentSynchronizeDTO.getOrganizationId();

    OrganizationStationDTO organizationStationDTO = organizationService
      .getOrganizationStation(orgId, null, accessToken)
      .orElseThrow(() -> new InvalidValueException(
        ErrorCodeConstants.ERROR_CODE_ORGANIZATION_STATION_NOT_FOUND,
        String.format("Station for org with id %s not found", orgId)
      ));

    Boolean flagPuPagoPaPayment = installmentSynchronizeDTO.getFlagPuPagoPaPayment();
    String iupdPagopa = installmentSynchronizeDTO.getIupdPagopa();

    boolean isGpd = Objects.equals(organizationStationDTO.getPagoPaInteractionModel(), PagoPaInteractionModel.ASYNC_GPD);

    if (Boolean.FALSE.equals(flagPuPagoPaPayment) && isGpd && StringUtils.isBlank(iupdPagopa)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_IUPD_PAGOPA, "iupdPagopa is mandatory when flagPuPagoPaPayment is false and is a gpd station");
    }

    if (Boolean.TRUE.equals(flagPuPagoPaPayment) && StringUtils.isNotBlank(iupdPagopa)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IUPD_PAGOPA, "iupdPagopa must not be populated when flagPuPagoPaPayment is true");
    }
  }
}
