package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationOperationType;
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
  public IONotificationDTO getIONotificationDetails(Long debtPositionTypeOrgId, IONotificationOperationType context) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)
      .orElseThrow(() -> new NotFoundException("DebtPositionTypeOrg having id %d was not found".formatted(debtPositionTypeOrgId)));

    if (debtPositionTypeOrg.isFlagNotifyIo() && context.equals(IONotificationOperationType.CREATE_DP)) {
      return IONotificationDTO.builder()
        .serviceId(debtPositionTypeOrg.getServiceId())
        .ioTemplateSubject(debtPositionTypeOrg.getIoTemplateSubject())
        .ioTemplateMessage(debtPositionTypeOrg.getIoTemplateMessage())
        .build();
    }
    return null;
  }

}
