package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.AppIONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionTypeOrgServiceImpl implements DebtPositionTypeOrgService {
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  public DebtPositionTypeOrgServiceImpl(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
  }

  @Override
  public AppIONotificationDTO getAppIONotificationDetails(Long debtPositionTypeOrgId, PaymentEventType context) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)
      .orElseThrow(() -> new NotFoundException("DebtPositionTypeOrg having id %d not found".formatted(debtPositionTypeOrgId)));

    if (!debtPositionTypeOrg.isFlagNotifyIo()) {
      throw new IllegalArgumentException("DebtPositionTypeOrg with id " + debtPositionTypeOrgId + " is not enabled for AppIO notifications.");
    }
    return AppIONotificationDTO.builder()
      .serviceId(debtPositionTypeOrg.getServiceId())
      .ioTemplateSubject(debtPositionTypeOrg.getIoTemplateSubject())
      .ioTemplateMessage(debtPositionTypeOrg.getIoTemplateMessage())
      .build();
  }
}
