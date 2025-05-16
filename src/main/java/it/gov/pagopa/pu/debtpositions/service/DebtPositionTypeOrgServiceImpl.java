package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.util.ValidationUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
public class DebtPositionTypeOrgServiceImpl implements DebtPositionTypeOrgService {
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService;
  public static final List<String> debtPositionTypeOrgReadOnlyFields = List.of(
          "debtPositionTypeOrgId", "debtPositionTypeId", "organizationId",
          "balance", "code", "description", "orgSector", "flagAnonymousFiscalCode",
          "flagMandatoryDueDate", "flagNotifyIo", "flagActive", "flagAmountActualization",
          "flagExternal");

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
    List<String> updatedFields = ValidationUtils.checkReadOnlyFields(existingDebtPositionTypeOrg, updatedDebtPositionTypeOrg, debtPositionTypeOrgReadOnlyFields);
    if(!CollectionUtils.isEmpty(updatedFields)){
      throw new ValidationException("The following DebtPositionTypeOrg fields are readOnly. "+updatedFields);
    }
  }
}
