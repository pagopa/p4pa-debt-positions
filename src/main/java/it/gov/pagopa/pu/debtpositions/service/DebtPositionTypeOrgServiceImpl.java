package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    DebtPositionTypeOrg savedDebtPositionTypeOrg = debtPositionTypeOrgRepository.save(
      saveDebtPositionTypeOrgDTO.getDebtPositionTypeOrg());
    handleOperators(savedDebtPositionTypeOrg,saveDebtPositionTypeOrgDTO);
    return savedDebtPositionTypeOrg;
  }

  private void handleOperators(DebtPositionTypeOrg debtPositionTypeOrg, SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO) {
    if(Boolean.TRUE.equals(saveDebtPositionTypeOrgDTO.getRemoveEnabledOperators())){
      debtPositionTypeOrgOperatorsService.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrg.getDebtPositionTypeOrgId());
    }
    if(!CollectionUtils.isEmpty(saveDebtPositionTypeOrgDTO.getEnabledOperators())){
      debtPositionTypeOrgOperatorsService.saveOperators(debtPositionTypeOrg.getDebtPositionTypeOrgId(),
        saveDebtPositionTypeOrgDTO.getEnabledOperators());
    }
    if(!CollectionUtils.isEmpty(saveDebtPositionTypeOrgDTO.getDisabledOperators())){
      debtPositionTypeOrgOperatorsService.deleteOperators(debtPositionTypeOrg.getDebtPositionTypeOrgId(),
        saveDebtPositionTypeOrgDTO.getDisabledOperators());
    }
  }
}
