package it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstallmentSynchronizeMapper {

  public DebtPositionDTO map2DebtPositionDTO(InstallmentSynchronizeDTO installmentSynchronizeDTO, Long debtPositionTypeOrgId) {
    return DebtPositionDTO.builder()
      .iupdOrg(installmentSynchronizeDTO.getIupdOrg())
      .description(installmentSynchronizeDTO.getDescription())
      .debtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL)
      .debtPositionTypeOrgId(debtPositionTypeOrgId)
      .organizationId(installmentSynchronizeDTO.getOrganizationId())
      .validityDate(installmentSynchronizeDTO.getValidityDate())
      .multiDebtor(installmentSynchronizeDTO.getMultiDebtor())
      .flagPagoPaPayment(installmentSynchronizeDTO.getFlagPagoPaPayment())
      .flagIuvVolatile(Boolean.FALSE)
      .status(installmentSynchronizeDTO.getDraft().equals(Boolean.TRUE) ? DebtPositionStatus.DRAFT : DebtPositionStatus.UNPAID)
      .paymentOptions(List.of(map2PaymentOptionDTO(installmentSynchronizeDTO)))
      .build();
  }

  public PaymentOptionDTO map2PaymentOptionDTO(InstallmentSynchronizeDTO installmentSynchronizeDTO){
    return PaymentOptionDTO.builder()
      .paymentOptionIndex(installmentSynchronizeDTO.getPaymentOptionIndex())
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.valueOf(installmentSynchronizeDTO.getPaymentOptionType()))
      .description(installmentSynchronizeDTO.getPaymentOptionDescription())
      .status(installmentSynchronizeDTO.getDraft().equals(Boolean.TRUE) ? PaymentOptionStatus.DRAFT :PaymentOptionStatus.UNPAID)
      .installments(List.of(map2Installment(installmentSynchronizeDTO)))
      .build();
  }

  public InstallmentDTO map2Installment(InstallmentSynchronizeDTO installmentSynchronizeDTO){
    return InstallmentDTO.builder()
      .iud(installmentSynchronizeDTO.getIud())
      .iuv(installmentSynchronizeDTO.getIuv())
      .dueDate(installmentSynchronizeDTO.getDueDate())
      .notificationFeeCents(installmentSynchronizeDTO.getNotificationFeeCents())
      .amountCents(installmentSynchronizeDTO.getAmountCents())
      .remittanceInformation(installmentSynchronizeDTO.getRemittanceInformation())
      .balance(installmentSynchronizeDTO.getBalance())
      .legacyPaymentMetadata(installmentSynchronizeDTO.getLegacyPaymentMetadata())
      .debtor(map2PersonDTO(installmentSynchronizeDTO))
      .ingestionFlowFileId(installmentSynchronizeDTO.getIngestionFlowFileId())
      .ingestionFlowFileLineNumber(installmentSynchronizeDTO.getIngestionFlowFileLineNumber())
      .notificationDate(installmentSynchronizeDTO.getNotificationDate())
      .status(installmentSynchronizeDTO.getDraft().equals(Boolean.TRUE) ? InstallmentStatus.DRAFT : InstallmentStatus.UNPAID)
      .transfers(installmentSynchronizeDTO.getAdditionalTransfers().stream().map(this::map2TransferDTO).toList())
      .build();
  }

  public TransferDTO map2TransferDTO(TransferSynchronizeDTO transferSynchronizeDTO) {
    return TransferDTO.builder()
      .orgFiscalCode(transferSynchronizeDTO.getOrgFiscalCode())
      .orgName(transferSynchronizeDTO.getOrgName())
      .amountCents(transferSynchronizeDTO.getAmountCents())
      .remittanceInformation(transferSynchronizeDTO.getRemittanceInformation())
      .iban(transferSynchronizeDTO.getIban())
      .category(transferSynchronizeDTO.getCategory())
      .transferIndex(transferSynchronizeDTO.getTransferIndex())
      .build();
  }

  private PersonDTO map2PersonDTO(InstallmentSynchronizeDTO installmentSynchronizeDTO) {
    return PersonDTO.builder()
      .entityType(PersonDTO.EntityTypeEnum.valueOf(installmentSynchronizeDTO.getEntityType().getValue()))
      .fiscalCode(installmentSynchronizeDTO.getFiscalCode())
      .fullName(installmentSynchronizeDTO.getFullName())
      .address(installmentSynchronizeDTO.getAddress())
      .civic(installmentSynchronizeDTO.getCivic())
      .postalCode(installmentSynchronizeDTO.getPostalCode())
      .location(installmentSynchronizeDTO.getLocation())
      .province(installmentSynchronizeDTO.getProvince())
      .nation(installmentSynchronizeDTO.getNation())
      .email(installmentSynchronizeDTO.getEmail())
      .build();
  }
}
