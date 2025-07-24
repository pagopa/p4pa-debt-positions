package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.checkImmutableField;

@Slf4j
@Service
public class DebtPositionTypeOrgServiceImpl implements DebtPositionTypeOrgService {
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService;

  public DebtPositionTypeOrgServiceImpl(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository,
    DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeOrgOperatorsService = debtPositionTypeOrgOperatorsService;
  }

  @Override
  public IONotificationDTO getIONotificationDetails(Long debtPositionTypeOrgId, PaymentEventType paymentEventType) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)
      .orElseThrow(() -> new NotFoundException("DebtPositionTypeOrg having id %d was not found".formatted(debtPositionTypeOrgId)));

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
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(
      debtPositionTypeOrgId).orElseThrow(
        () -> new NotFoundException("DebtPositionTypeOrg having id "+debtPositionTypeOrgId+" not found"));
    debtPositionTypeOrgOperatorsService.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrgId);
    debtPositionTypeOrgRepository.delete(debtPositionTypeOrg);
  }

  @Transactional
  @Override
  public DebtPositionTypeOrg saveDebtPositionTypeOrg(
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO) {
    validateDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO.getDebtPositionTypeOrg());
    DebtPositionTypeOrg savedDebtPositionTypeOrg = debtPositionTypeOrgRepository.save(
      saveDebtPositionTypeOrgDTO.getDebtPositionTypeOrg());
    handleOperators(savedDebtPositionTypeOrg,saveDebtPositionTypeOrgDTO);
    return savedDebtPositionTypeOrg;
  }

  @Override
  public void updateFlagActiveDebtPositionTypeOrg(Long debtPositionTypeOrgId, boolean flagActive) {
    debtPositionTypeOrgRepository.findById(
      debtPositionTypeOrgId).orElseThrow(
      () -> new NotFoundException("DebtPositionTypeOrg having id " + debtPositionTypeOrgId + " not found"));
    debtPositionTypeOrgRepository.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, flagActive);
  }

  private void handleOperators(DebtPositionTypeOrg debtPositionTypeOrg, SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO) {
    if(Boolean.TRUE.equals(saveDebtPositionTypeOrgDTO.getRemoveEnabledOperators())){
      debtPositionTypeOrgOperatorsService.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrg.getDebtPositionTypeOrgId());
    }
    if(!CollectionUtils.isEmpty(saveDebtPositionTypeOrgDTO.getDisabledOperators())){
      debtPositionTypeOrgOperatorsService.deleteOperators(debtPositionTypeOrg.getDebtPositionTypeOrgId(),
        saveDebtPositionTypeOrgDTO.getDisabledOperators());
    }
    if(!CollectionUtils.isEmpty(saveDebtPositionTypeOrgDTO.getEnabledOperators())){
      debtPositionTypeOrgOperatorsService.saveOperators(debtPositionTypeOrg.getDebtPositionTypeOrgId(),
        saveDebtPositionTypeOrgDTO.getEnabledOperators());
    }
  }

  private void validateDebtPositionTypeOrg(DebtPositionTypeOrg debtPositionTypeOrg) {
      if (debtPositionTypeOrg == null) {
          throw new ValidationException("DebtPositionTypeOrg must not be null");
      }
      if(debtPositionTypeOrg.getDebtPositionTypeOrgId()!=null){
        DebtPositionTypeOrg dpto = debtPositionTypeOrgRepository.findById(debtPositionTypeOrg.getDebtPositionTypeOrgId())
                .orElseThrow(()->new NotFoundException("DebtPositionTypeOrg having ID %d not found".formatted(debtPositionTypeOrg.getDebtPositionTypeOrgId())));
        checkReadOnlyFields(dpto, debtPositionTypeOrg);
      }
  }

  private void checkReadOnlyFields(DebtPositionTypeOrg existingDebtPositionTypeOrg, DebtPositionTypeOrg updatedDebtPositionTypeOrg) {
    List<String> modifiedFields = new ArrayList<>();
    checkImmutableField("debtPositionTypeOrgId", existingDebtPositionTypeOrg.getDebtPositionTypeOrgId(), updatedDebtPositionTypeOrg.getDebtPositionTypeOrgId(), modifiedFields);
    checkImmutableField("debtPositionTypeId", existingDebtPositionTypeOrg.getDebtPositionTypeId(), updatedDebtPositionTypeOrg.getDebtPositionTypeId(), modifiedFields);
    checkImmutableField("organizationId", existingDebtPositionTypeOrg.getOrganizationId(), updatedDebtPositionTypeOrg.getOrganizationId(), modifiedFields);
    checkImmutableField("balance", existingDebtPositionTypeOrg.getBalance(), updatedDebtPositionTypeOrg.getBalance(), modifiedFields);
    checkImmutableField("code", existingDebtPositionTypeOrg.getCode(), updatedDebtPositionTypeOrg.getCode(), modifiedFields);
    checkImmutableField("description", existingDebtPositionTypeOrg.getDescription(), updatedDebtPositionTypeOrg.getDescription(), modifiedFields);
    checkImmutableField("orgSector", existingDebtPositionTypeOrg.getOrgSector(), updatedDebtPositionTypeOrg.getOrgSector(), modifiedFields);
    checkImmutableField("flagAnonymousFiscalCode", existingDebtPositionTypeOrg.isFlagAnonymousFiscalCode(), updatedDebtPositionTypeOrg.isFlagAnonymousFiscalCode(), modifiedFields);
    checkImmutableField("flagMandatoryDueDate", existingDebtPositionTypeOrg.isFlagMandatoryDueDate(), updatedDebtPositionTypeOrg.isFlagMandatoryDueDate(), modifiedFields);
    checkImmutableField("flagActive", existingDebtPositionTypeOrg.isFlagActive(), updatedDebtPositionTypeOrg.isFlagActive(), modifiedFields);
    checkImmutableField("flagAmountActualization", existingDebtPositionTypeOrg.isFlagAmountActualization(), updatedDebtPositionTypeOrg.isFlagAmountActualization(), modifiedFields);
    checkImmutableField("flagExternal", existingDebtPositionTypeOrg.isFlagExternal(), updatedDebtPositionTypeOrg.isFlagExternal(), modifiedFields);
    if(!CollectionUtils.isEmpty(modifiedFields)){
      throw new ValidationException("The following DebtPositionTypeOrg fields are readOnly. "+modifiedFields);
    }
  }
}
