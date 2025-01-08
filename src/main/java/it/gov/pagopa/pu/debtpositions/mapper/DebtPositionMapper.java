package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionMapper {

  private final PaymentOptionMapper paymentOptionMapper;

  public DebtPositionMapper(PaymentOptionMapper paymentOptionMapper) {
    this.paymentOptionMapper = paymentOptionMapper;
  }

  public DebtPositionDTO mapToDto(DebtPosition debtPosition) {
    return DebtPositionDTO.builder()
      .debtPositionId(debtPosition.getDebtPositionId())
      .iupdOrg(debtPosition.getIupdOrg())
      .description(debtPosition.getDescription())
      .status(debtPosition.getStatus())
      .ingestionFlowFileId(debtPosition.getIngestionFlowFileId())
      .ingestionFlowFileLineNumber(debtPosition.getIngestionFlowFileLineNumber())
      .organizationId(debtPosition.getOrganizationId())
      .debtPositionTypeOrgId(debtPosition.getDebtPositionTypeOrgId())
      .notificationDate(debtPosition.getNotificationDate())
      .validityDate(debtPosition.getValidityDate())
      .flagIuvVolatile(debtPosition.isFlagIuvVolatile())
      .creationDate(debtPosition.getCreationDate())
      .updateDate(debtPosition.getUpdateDate())
      .paymentOptions(debtPosition.getPaymentOptions().stream()
        .map(paymentOptionMapper::mapToDto)
        .toList())
      .build();
  }

  public DebtPosition mapToModel(DebtPositionDTO dto) {
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(dto.getDebtPositionId());
    debtPosition.setIupdOrg(dto.getIupdOrg());
    debtPosition.setDescription(dto.getDescription());
    debtPosition.setStatus(dto.getStatus());
    debtPosition.setIngestionFlowFileId(dto.getIngestionFlowFileId());
    debtPosition.setIngestionFlowFileLineNumber(dto.getIngestionFlowFileLineNumber());
    debtPosition.setOrganizationId(dto.getOrganizationId());
    debtPosition.setDebtPositionTypeOrgId(dto.getDebtPositionTypeOrgId());
    debtPosition.setNotificationDate(dto.getNotificationDate());
    debtPosition.setValidityDate(dto.getValidityDate());
    debtPosition.setFlagIuvVolatile(dto.getFlagIuvVolatile());
    debtPosition.setCreationDate(dto.getCreationDate());
    debtPosition.setUpdateDate(dto.getUpdateDate());
    debtPosition.setPaymentOptions(dto.getPaymentOptions().stream()
      .map(paymentOptionMapper::mapToModel)
      .toList());
    return debtPosition;
  }

}
