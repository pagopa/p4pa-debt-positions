package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowDebtPositionService;
import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.SpontaneousFormRepository;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.MassiveDebtPositionIbanUpdateRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.checkImmutableField;

@Slf4j
@Service
public class DebtPositionTypeOrgServiceImpl implements DebtPositionTypeOrgService {
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService;
  private final SpontaneousFormRepository spontaneousFormRepository;
  private final WorkflowDebtPositionService workflowDebtPositionService;
  private final OrganizationService organizationService;

  public DebtPositionTypeOrgServiceImpl(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository,
                                        DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService,
                                        SpontaneousFormRepository spontaneousFormRepository,
                                        WorkflowDebtPositionService workflowDebtPositionService,
                                        OrganizationService organizationService
  ) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeOrgOperatorsService = debtPositionTypeOrgOperatorsService;
    this.spontaneousFormRepository = spontaneousFormRepository;
    this.workflowDebtPositionService = workflowDebtPositionService;
    this.organizationService = organizationService;
  }

  @Override
  public IONotificationDTO getIONotificationDetails(Long debtPositionTypeOrgId, PaymentEventType paymentEventType) {
    DebtPositionTypeOrg debtPositionTypeOrg = findDptoByIdOrThrow(debtPositionTypeOrgId);

    if (debtPositionTypeOrg.isFlagNotifyIo() && PaymentEventType.DP_CREATED.equals(paymentEventType)) {
      return IONotificationDTO.builder()
        .serviceId(debtPositionTypeOrg.getServiceId())
        .ioTemplateSubject(debtPositionTypeOrg.getIoTemplateSubject())
        .ioTemplateMessage(debtPositionTypeOrg.getIoTemplateMessage())
        .build();
    }
    return null;
  }

  @Transactional
  @Override
  public void deleteDebtPositionTypeOrg(Long debtPositionTypeOrgId) {
    DebtPositionTypeOrg debtPositionTypeOrg = findDptoByIdOrThrow(debtPositionTypeOrgId);
    debtPositionTypeOrgOperatorsService.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrgId);
    debtPositionTypeOrgRepository.delete(debtPositionTypeOrg);
  }

  @Transactional
  @Override
  public DebtPositionTypeOrg saveDebtPositionTypeOrg(
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO,
    String accessToken
  ) {
    DebtPositionTypeOrg dpto = saveDebtPositionTypeOrgDTO.getDebtPositionTypeOrg();
    validateDptoAndTriggerMassiveIbanUpdateIfNeeded(dpto, accessToken);
    DebtPositionTypeOrg savedDebtPositionTypeOrg = debtPositionTypeOrgRepository.save(dpto);
    handleOperators(savedDebtPositionTypeOrg, saveDebtPositionTypeOrgDTO);
    return savedDebtPositionTypeOrg;
  }

  @Override
  public void updateFlagActiveDebtPositionTypeOrg(Long debtPositionTypeOrgId, boolean flagActive) {

    DebtPositionTypeOrg debtPositionTypeOrg = findDptoByIdOrThrow(debtPositionTypeOrgId);
    if (flagActive && debtPositionTypeOrg.getDebtPositionTypeId() < 0) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_FLAG_ACTIVE, "Technical debtPositionTypeOrg cannot be enabled");
    }

    if (debtPositionTypeOrgRepository.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, flagActive) == 0) {
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_ORG_NOT_FOUND,"DebtPositionTypeOrg with id %d not found".formatted(debtPositionTypeOrgId));
    }
  }

  private void handleOperators(DebtPositionTypeOrg debtPositionTypeOrg, SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO) {
    if (Boolean.TRUE.equals(saveDebtPositionTypeOrgDTO.getRemoveEnabledOperators())) {
      debtPositionTypeOrgOperatorsService.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrg.getDebtPositionTypeOrgId());
    }
    if (!CollectionUtils.isEmpty(saveDebtPositionTypeOrgDTO.getDisabledOperators())) {
      debtPositionTypeOrgOperatorsService.deleteOperators(debtPositionTypeOrg.getDebtPositionTypeOrgId(),
        saveDebtPositionTypeOrgDTO.getDisabledOperators());
    }
    if (!CollectionUtils.isEmpty(saveDebtPositionTypeOrgDTO.getEnabledOperators())) {
      debtPositionTypeOrgOperatorsService.saveOperators(debtPositionTypeOrg.getDebtPositionTypeOrgId(),
        saveDebtPositionTypeOrgDTO.getEnabledOperators());
    }
  }

  private void validateDptoAndTriggerMassiveIbanUpdateIfNeeded(DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    if (debtPositionTypeOrg == null) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_DEBT_POSITION_TYPE_ORG, "DebtPositionTypeOrg must not be null");
    }
    if (debtPositionTypeOrg.getDebtPositionTypeId() < 0 && debtPositionTypeOrg.isFlagActive()) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_FLAG_ACTIVE, "Technical debtPositionTypeOrg cannot be enabled");
    }
    if (StringUtils.isNotBlank(debtPositionTypeOrg.getIban()) && !Utilities.isValidIban(debtPositionTypeOrg.getIban())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IBAN, "Provided iban is not valid");
    }

    String postalIban = debtPositionTypeOrg.getPostalIban();
    // Postal IBAN is optional, but if provided, it must not be blank
    if (postalIban != null && !Utilities.isValidIban(postalIban)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_POSTAL_IBAN, "Provided postal iban is not valid");
    }

    Long dptoId = debtPositionTypeOrg.getDebtPositionTypeOrgId();

    DebtPositionTypeOrg existingDpto = null;
    if (dptoId != null) {
      existingDpto = findDptoByIdOrThrow(dptoId);
      checkReadOnlyFields(existingDpto, debtPositionTypeOrg);
    }

    if (debtPositionTypeOrg.getSpontaneousFormId() != null) {
      spontaneousFormRepository.findById(debtPositionTypeOrg.getSpontaneousFormId())
        .ifPresentOrElse(spontaneousForm -> {
          if (!Objects.equals(debtPositionTypeOrg.getOrganizationId(), spontaneousForm.getOrganizationId())) {
            throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_SPONTANEOUS_FORM, "SpontaneousFormId %d is not tied to the organizationId %d"
              .formatted(debtPositionTypeOrg.getSpontaneousFormId(), debtPositionTypeOrg.getOrganizationId()));
          }
        }, () -> {
          throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_SPONTANEOUS_FORM, "SpontaneousFormId %d not found"
            .formatted(debtPositionTypeOrg.getSpontaneousFormId()));
        });
    }

   triggerMassiveIbanUpdateIfNeeded(debtPositionTypeOrg, existingDpto, accessToken);
  }

  private void checkReadOnlyFields(DebtPositionTypeOrg existingDebtPositionTypeOrg, DebtPositionTypeOrg updatedDebtPositionTypeOrg) {
    List<String> modifiedFields = new ArrayList<>();
    checkImmutableField("debtPositionTypeOrgId", existingDebtPositionTypeOrg.getDebtPositionTypeOrgId(), updatedDebtPositionTypeOrg.getDebtPositionTypeOrgId(), modifiedFields);
    checkImmutableField("debtPositionTypeId", existingDebtPositionTypeOrg.getDebtPositionTypeId(), updatedDebtPositionTypeOrg.getDebtPositionTypeId(), modifiedFields);
    checkImmutableField("organizationId", existingDebtPositionTypeOrg.getOrganizationId(), updatedDebtPositionTypeOrg.getOrganizationId(), modifiedFields);
    checkImmutableField("code", existingDebtPositionTypeOrg.getCode(), updatedDebtPositionTypeOrg.getCode(), modifiedFields);
    checkImmutableField("description", existingDebtPositionTypeOrg.getDescription(), updatedDebtPositionTypeOrg.getDescription(), modifiedFields);
    checkImmutableField("orgSector", existingDebtPositionTypeOrg.getOrgSector(), updatedDebtPositionTypeOrg.getOrgSector(), modifiedFields);
    checkImmutableField("flagActive", existingDebtPositionTypeOrg.isFlagActive(), updatedDebtPositionTypeOrg.isFlagActive(), modifiedFields);
    checkImmutableField("flagAmountActualization", existingDebtPositionTypeOrg.isFlagAmountActualization(), updatedDebtPositionTypeOrg.isFlagAmountActualization(), modifiedFields);
    checkImmutableField("flagExternal", existingDebtPositionTypeOrg.isFlagExternal(), updatedDebtPositionTypeOrg.isFlagExternal(), modifiedFields);
    if (!CollectionUtils.isEmpty(modifiedFields)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD, "The following DebtPositionTypeOrg fields are readOnly. " + modifiedFields);
    }
  }

  private void triggerMassiveIbanUpdateIfNeeded(DebtPositionTypeOrg debtPositionTypeOrg, DebtPositionTypeOrg existingDpto, String accessToken) {
    Long dptoId = debtPositionTypeOrg.getDebtPositionTypeOrgId();
    if (dptoId == null) {
      return;
    }

    Long orgId = debtPositionTypeOrg.getOrganizationId();
    Organization org = organizationService.getOrganizationById(orgId, accessToken)
      .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_ORGANIZATION_NOT_FOUND, "Organization with id " + orgId + " not found"));

    String oldIban = existingDpto.getIban();
    String newIban = debtPositionTypeOrg.getIban();
    String oldPostalIban = existingDpto.getPostalIban();
    String newPostalIban = debtPositionTypeOrg.getPostalIban();

    String orgIban = org.getIban();
    if (orgIban == null) {
      return;
    }

    if (!Objects.equals(oldIban, newIban) || !Objects.equals(oldPostalIban, newPostalIban)) {
      MassiveDebtPositionIbanUpdateRequestDTO requestDTO = MassiveDebtPositionIbanUpdateRequestDTO.builder()
        .oldIban(oldIban == null ? orgIban : oldIban)
        .newIban(newIban == null ? orgIban: newIban)
        .oldPostalIban(oldPostalIban)
        .newPostalIban(newPostalIban)
        .debtPositionTypeOrgId(dptoId)
        .build();

      workflowDebtPositionService.massiveDpIbanUpdate(existingDpto.getOrganizationId(), requestDTO, accessToken);
    }
  }

  private DebtPositionTypeOrg findDptoByIdOrThrow(Long dptoId) {
    return debtPositionTypeOrgRepository.findById(dptoId)
      .orElseThrow(() -> new NotFoundException(
        ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_ORG_NOT_FOUND,
        "DebtPositionTypeOrg with id %d not found".formatted(dptoId)
      ));
  }
}
