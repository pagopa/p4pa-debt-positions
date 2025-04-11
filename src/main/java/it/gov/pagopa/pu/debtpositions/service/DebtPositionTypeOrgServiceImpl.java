package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DebtPositionTypeOrgServiceImpl implements DebtPositionTypeOrgService {
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository;

  public DebtPositionTypeOrgServiceImpl(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository,
    DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeOrgOperatorsRepository = debtPositionTypeOrgOperatorsRepository;
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
    long deletedOperators = debtPositionTypeOrgOperatorsRepository.deleteByDebtPositionTypeOrgId(
      debtPositionTypeOrgId);
    log.info("Deleted {} operators having debtPositionTypeOrgId {}", deletedOperators,debtPositionTypeOrgId);
    debtPositionTypeOrgRepository.delete(debtPositionTypeOrg);
  }
}
