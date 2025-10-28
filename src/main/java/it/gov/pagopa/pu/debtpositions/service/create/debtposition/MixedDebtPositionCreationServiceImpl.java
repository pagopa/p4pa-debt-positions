package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowTypeOrgService;
import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedTransferDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.MixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.CategoryResolverService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionSaveService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.IuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MixedDebtPositionCreationServiceImpl implements
  MixedDebtPositionCreationService {

  private final WorkflowTypeOrgService workflowTypeOrgService;
  private final AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;
  private final MixedOrdinaryDebtPositionCreationInnerService debtPositionCreationService;
  private final TechnicalMixedDebtPositionBuilderService technicalMixedDebtPositionBuilderService;
  private final OrganizationService organizationService;
  private final DebtPositionSaveService debtPositionSaveService;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final MixedDebtPositionMapper mixedDebtPositionMapper;
  private final DebtPositionMapper debtPositionMapper;

  public MixedDebtPositionCreationServiceImpl(
    WorkflowTypeOrgService workflowTypeOrgService,
    AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
    MixedOrdinaryDebtPositionCreationInnerService debtPositionCreationService,
    TechnicalMixedDebtPositionBuilderService technicalMixedDebtPositionBuilderService,
    OrganizationService organizationService,
    DebtPositionSaveService debtPositionSaveService,
    InstallmentNoPIIRepository installmentNoPIIRepository,
    MixedDebtPositionMapper mixedDebtPositionMapper,
    DebtPositionMapper debtPositionMapper) {
    this.workflowTypeOrgService = workflowTypeOrgService;
    this.authorizeOperatorOnDebtPositionTypeService = authorizeOperatorOnDebtPositionTypeService;
    this.debtPositionCreationService = debtPositionCreationService;
    this.technicalMixedDebtPositionBuilderService = technicalMixedDebtPositionBuilderService;
    this.organizationService = organizationService;
    this.debtPositionSaveService = debtPositionSaveService;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.mixedDebtPositionMapper = mixedDebtPositionMapper;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Service
  public static class MixedOrdinaryDebtPositionCreationInnerService extends DebtPositionCreationServiceImpl {

    public MixedOrdinaryDebtPositionCreationInnerService(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService, ValidateDebtPositionService validateDebtPositionService, DebtPositionService debtPositionService, IuvService iuvService, DebtPositionSyncService debtPositionSyncService, InstallmentNoPIIRepository installmentNoPIIRepository, DebtPositionProcessorService debtPositionProcessorService, OrganizationService organizationService, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, CategoryResolverService categoryResolverService) {
      super(authorizeOperatorOnDebtPositionTypeService, validateDebtPositionService, debtPositionService, iuvService, debtPositionSyncService, installmentNoPIIRepository, debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService, debtPositionTypeOrgRepository, categoryResolverService);
    }

    @Override
    protected boolean isDebtPositionTypeOrgDisabledAllowed() {
      return true;
    }
  }

  @Transactional
  @Override
  public Pair<WorkflowCreatedDTO, DebtPositionDTO> createMixedDebtPosition(
    MixedDebtPositionDTO mixedDebtPositionDTO, String accessToken,
    String operatorExternalUserId) {
    Organization organization = organizationService.getOrganizationById(
        mixedDebtPositionDTO.getOrganizationId(), accessToken)
      .orElseThrow(() -> new NotFoundException(
        "Organization with id: [%s] not found.".formatted(
          mixedDebtPositionDTO.getOrganizationId())));

    checkWorkflowTypeOrgExistsAndAuthorization(organization.getIpaCode(),
      mixedDebtPositionDTO.getTransfers(), accessToken, operatorExternalUserId);
    validateIudUniqueness(mixedDebtPositionDTO);

    DebtPositionDTO debtPositionDTO = mixedDebtPositionMapper.mapToDebtPositionDTO(organization,
      mixedDebtPositionDTO);

    WorkflowCreatedDTO workflowCreatedDTO = debtPositionCreationService.createDebtPosition(
      debtPositionDTO, new WfExecutionParameters(), accessToken,
      operatorExternalUserId);

    Map<Long, List<MixedDpAdditionalData>> debtPositionTypeOrgId2TransfersData = mixedDebtPositionMapper.buildDebtPositionTypeOrgId2TransfersData(
      mixedDebtPositionDTO.getTransfers());
    DebtPosition debtPosition = debtPositionMapper.mapToModel(debtPositionDTO);
    List<DebtPosition> technicalMixedDebtPositions = technicalMixedDebtPositionBuilderService.createTechnicalMixedDebtPositions(
      debtPositionTypeOrgId2TransfersData, debtPosition, accessToken);

    technicalMixedDebtPositions.forEach(
      debtPositionSaveService::saveDebtPosition);

    return Pair.of(workflowCreatedDTO, debtPositionDTO);
  }

  private void checkWorkflowTypeOrgExistsAndAuthorization(
    String organizationIpaCode, List<MixedTransferDTO> transfers,
    String accessToken, String operatorExternalUserId) {
    transfers
      .stream()
      .map(MixedTransferDTO::getDebtPositionTypeOrgId)
      .filter(Objects::nonNull)
      .map(String::valueOf)
      .forEach(dpTypeOrgId -> {
        authorizeOperatorOnDebtPositionTypeService.authorize(
          organizationIpaCode, Long.valueOf(dpTypeOrgId),
          operatorExternalUserId);

        workflowTypeOrgService.getById(dpTypeOrgId, accessToken)
          .ifPresent(workflowTypeOrg -> {
            throw new InvalidValueException(
              "DebtPositionTypeOrgId [%s] is related to custom workflow having id [%d]".formatted(
                dpTypeOrgId, workflowTypeOrg.getWorkflowTypeId()));
          });
      });
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
        iud, null, null, InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)) {
        throw new InvalidValueException(
          "IUD: [%s] is not unique".formatted(iud));
      }
    }
  }
}
