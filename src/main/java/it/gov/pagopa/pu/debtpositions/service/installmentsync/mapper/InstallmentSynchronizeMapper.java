package it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.service.BalanceFetchService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class InstallmentSynchronizeMapper {
  private final BalanceFetchService balanceFetchService;

  public InstallmentSynchronizeMapper(BalanceFetchService balanceFetchService) {
    this.balanceFetchService = balanceFetchService;
  }

  public DebtPositionDTO map2DebtPositionDTO(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    return DebtPositionDTO.builder()
      .iupdOrg(installmentSynchronizeDTO.getIupdOrg())
      .description(installmentSynchronizeDTO.getDescription())
      .debtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL)
      .debtPositionTypeOrgId(debtPositionTypeOrg.getDebtPositionTypeOrgId())
      .organizationId(installmentSynchronizeDTO.getOrganizationId())
      .validityDate(installmentSynchronizeDTO.getValidityDate())
      .multiDebtor(installmentSynchronizeDTO.getMultiDebtor())
      .flagPuPagoPaPayment(installmentSynchronizeDTO.getFlagPuPagoPaPayment())
      .flagIuvVolatile(Boolean.FALSE)
      .status((Boolean.TRUE).equals(installmentSynchronizeDTO.getDraft()) ? DebtPositionStatus.DRAFT : DebtPositionStatus.UNPAID)
      .paymentOptions(List.of(map2PaymentOptionDTO(installmentSynchronizeDTO, debtPositionTypeOrg, accessToken)))
      .build();
  }

  public PaymentOptionDTO map2PaymentOptionDTO(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken){
    return PaymentOptionDTO.builder()
      .paymentOptionIndex(installmentSynchronizeDTO.getPaymentOptionIndex())
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.valueOf(installmentSynchronizeDTO.getPaymentOptionType()))
      .description(installmentSynchronizeDTO.getPaymentOptionDescription())
      .status(Boolean.TRUE.equals(installmentSynchronizeDTO.getDraft()) ? PaymentOptionStatus.DRAFT :PaymentOptionStatus.UNPAID)
      .installments(List.of(map2Installment(installmentSynchronizeDTO, debtPositionTypeOrg, accessToken)))
      .build();
  }

  public InstallmentDTO map2Installment(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken){
    String balance = StringUtils.isNotBlank(installmentSynchronizeDTO.getBalance()) ? installmentSynchronizeDTO.getBalance() : balanceFetchService.getBalanceDefault(installmentSynchronizeDTO.getOrganizationId(), debtPositionTypeOrg, accessToken);

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
