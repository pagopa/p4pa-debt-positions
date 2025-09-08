package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import static it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService.DEBT_POSITION_TYPE_MIXED;

import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowTypeOrgService;
import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedTransferDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.MixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
public class MixedDebtPositionCreationServiceImpl implements
  MixedDebtPositionCreationService {

  private final WorkflowTypeOrgService workflowTypeOrgService;
  private final DebtPositionCreationService debtPositionCreationService;
  private final MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverService;
  private final TechnicalMixedDebtPositionBuilderService technicalMixedDebtPositionBuilderService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final MixedDebtPositionMapper mixedDebtPositionMapper;
  private final DebtPositionMapper debtPositionMapper;

  public MixedDebtPositionCreationServiceImpl(
    WorkflowTypeOrgService workflowTypeOrgService,
    DebtPositionCreationService debtPositionCreationService,
    MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverService,
    TechnicalMixedDebtPositionBuilderService technicalMixedDebtPositionBuilderService,
    DebtPositionTypeOrgRepository debtPositionTypeOrgRepository,
    InstallmentNoPIIRepository installmentNoPIIRepository,
    MixedDebtPositionMapper mixedDebtPositionMapper,
    DebtPositionMapper debtPositionMapper) {
    this.workflowTypeOrgService = workflowTypeOrgService;
    this.debtPositionCreationService = debtPositionCreationService;
    this.mixedDebtPositionTypeOrgRetrieverService = mixedDebtPositionTypeOrgRetrieverService;
    this.technicalMixedDebtPositionBuilderService = technicalMixedDebtPositionBuilderService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.mixedDebtPositionMapper = mixedDebtPositionMapper;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Transactional
  @Override
  public Pair<WorkflowCreatedDTO, DebtPositionDTO> createMixedDebtPosition(
    MixedDebtPositionDTO mixedDebtPositionDTO, String accessToken,
    String operatorExternalUserId) {
    checkWorkflowTypeOrgExists(mixedDebtPositionDTO.getTransfers(),
      accessToken);
    validateIudUniqueness(mixedDebtPositionDTO);

    Long debtPositionTypeOrgId = getDebtPositionTypeOrgId(mixedDebtPositionDTO);

    DebtPositionDTO debtPositionDTO = mixedDebtPositionMapper.mapToDebtPositionDTO(
      mixedDebtPositionDTO, debtPositionTypeOrgId);

    WorkflowCreatedDTO workflowCreatedDTO = debtPositionCreationService.createDebtPosition(
      debtPositionDTO, new WfExecutionParameters(), accessToken,
      operatorExternalUserId);

    Map<Long, List<MixedDpAdditionalData>> debtPositionTypeOrgId2TransfersData = mixedDebtPositionMapper.buildDebtPositionTypeOrgId2TransfersData(
      mixedDebtPositionDTO.getTransfers());
    DebtPosition debtPosition = debtPositionMapper.mapToModel(debtPositionDTO);
    technicalMixedDebtPositionBuilderService.createTechnicalMixedDebtPositions(
      debtPositionTypeOrgId2TransfersData, debtPosition);

    return Pair.of(workflowCreatedDTO, debtPositionDTO);
  }

  private void checkWorkflowTypeOrgExists(List<MixedTransferDTO> transfers,
    String accessToken) {
    transfers
      .stream()
      .map(MixedTransferDTO::getDebtPositionTypeOrgId)
      .filter(Objects::nonNull)
      .map(String::valueOf)
      .forEach(id -> workflowTypeOrgService.getById(id, accessToken)
        .ifPresent(workflowTypeOrg -> {
          throw new InvalidValueException(
            "Workflow with id: [%s] already exists. workflowTypeId: [%d]".formatted(
              id, workflowTypeOrg.getWorkflowTypeId()));
        }));
  }

  private void validateIudUniqueness(
    MixedDebtPositionDTO mixedDebtPositionDTO) {
    List<String> requestIUDs = mixedDebtPositionDTO.getTransfers()
      .stream()
      .map(MixedTransferDTO::getIud)
      .distinct()
      .toList();
    for (String iud : requestIUDs) {
      if (installmentNoPIIRepository.isInstallmentExists(
        mixedDebtPositionDTO.getOrganizationId(),
        iud, null, null, InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS)) {
        throw new InvalidValueException(
          "IUD: [%s] is not unique".formatted(iud));
      }
    }
  }

  private Long getDebtPositionTypeOrgId(
    MixedDebtPositionDTO mixedDebtPositionDTO) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndDebtPositionTypeOrgId(
        mixedDebtPositionDTO.getOrganizationId(),
        DEBT_POSITION_TYPE_MIXED).map(
        DebtPositionTypeOrg::getDebtPositionTypeOrgId)
      .orElseGet(() ->
        mixedDebtPositionTypeOrgRetrieverService.getMixedDebtPositionTypeOrg(
          mixedDebtPositionDTO.getOrganizationId()).getDebtPositionTypeOrgId());
  }
}
