package it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeFetchBalanceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstallmentSynchronizeMapper {
  private final InstallmentSynchronizeFetchBalanceService installmentSynchronizeFetchBalanceService;

  public InstallmentSynchronizeMapper(InstallmentSynchronizeFetchBalanceService installmentSynchronizeFetchBalanceService) {
    this.installmentSynchronizeFetchBalanceService = installmentSynchronizeFetchBalanceService;
  }

  public DebtPositionDTO map2DebtPositionDTO(InstallmentSynchronizeDTO installmentSynchronizeDTO, Long debtPositionTypeOrgId, String accessToken) {
    return DebtPositionDTO.builder()
      .iupdOrg(installmentSynchronizeDTO.getIupdOrg())
      .description(installmentSynchronizeDTO.getDescription())
      .debtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL)
      .debtPositionTypeOrgId(debtPositionTypeOrgId)
      .organizationId(installmentSynchronizeDTO.getOrganizationId())
      .validityDate(installmentSynchronizeDTO.getValidityDate())
      .multiDebtor(installmentSynchronizeDTO.getMultiDebtor())
      .flagPuPagoPaPayment(installmentSynchronizeDTO.getFlagPuPagoPaPayment())
      .flagIuvVolatile(Boolean.FALSE)
      .status((Boolean.TRUE).equals(installmentSynchronizeDTO.getDraft()) ? DebtPositionStatus.DRAFT : DebtPositionStatus.UNPAID)
      .paymentOptions(List.of(map2PaymentOptionDTO(installmentSynchronizeDTO, accessToken)))
      .build();
  }

  public PaymentOptionDTO map2PaymentOptionDTO(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken){
    return PaymentOptionDTO.builder()
      .paymentOptionIndex(installmentSynchronizeDTO.getPaymentOptionIndex())
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.valueOf(installmentSynchronizeDTO.getPaymentOptionType()))
      .description(installmentSynchronizeDTO.getPaymentOptionDescription())
      .status(Boolean.TRUE.equals(installmentSynchronizeDTO.getDraft()) ? PaymentOptionStatus.DRAFT :PaymentOptionStatus.UNPAID)
      .installments(List.of(map2Installment(installmentSynchronizeDTO, accessToken)))
      .build();
  }

  public InstallmentDTO map2Installment(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken){
    String balance = StringUtils.isNotBlank(installmentSynchronizeDTO.getBalance()) ? installmentSynchronizeDTO.getBalance() : installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(installmentSynchronizeDTO, accessToken);

    return InstallmentDTO.builder()
      .iud(installmentSynchronizeDTO.getIud())
      .iuv(installmentSynchronizeDTO.getIuv())
      .dueDate(installmentSynchronizeDTO.getDueDate())
      .amountCents(installmentSynchronizeDTO.getAmountCents())
      .remittanceInformation(installmentSynchronizeDTO.getRemittanceInformation())
      .balance(balance)
      .legacyPaymentMetadata(installmentSynchronizeDTO.getLegacyPaymentMetadata())
      .debtor(map2PersonDTO(installmentSynchronizeDTO))
      .ingestionFlowFileId(installmentSynchronizeDTO.getIngestionFlowFileId())
      .ingestionFlowFileLineNumber(installmentSynchronizeDTO.getIngestionFlowFileLineNumber())
      .ingestionFlowFileAction(installmentSynchronizeDTO.getAction())
      .notificationDate(installmentSynchronizeDTO.getNotificationDate())
      .sourceFlowName(installmentSynchronizeDTO.getIngestionFlowFileName())
      .status(Boolean.TRUE.equals(installmentSynchronizeDTO.getDraft()) ? InstallmentStatus.DRAFT : InstallmentStatus.UNPAID)
      .transfers(installmentSynchronizeDTO.getAdditionalTransfers()
        .stream().map(this::map2TransferDTO)
        .collect(Collectors.toCollection(ArrayList<TransferDTO>::new)))
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
      .entityType(installmentSynchronizeDTO.getEntityType())
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
