package it.gov.pagopa.pu.debtpositions.mapper;

import static it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService.DEBT_POSITION_TYPE_MIXED;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedTransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO.PaymentOptionTypeEnum;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MixedDebtPositionMapper {

  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverService;

  public DebtPositionDTO mapToDebtPositionDTO(MixedDebtPositionDTO request) {
    if (request == null) {
      return null;
    }

    Long debtPositionTypeOrgId = getDebtPositionTypeOrgId(request);

    List<TransferDTO> transfers = new ArrayList<>();
    List<MixedTransferDTO> requestTransfers = request.getTransfers();
    for (int i = 0; i < requestTransfers.size(); i++) {
      MixedTransferDTO requestTransfer = requestTransfers.get(i);
      transfers.add(
        TransferDTO.builder()
          .transferIndex(i+1)
          .amountCents(requestTransfer.getAmountCents())
          .stampType(requestTransfer.getStampType())
          .stampHashDocument(requestTransfer.getStampHashDocument())
          .stampProvincialResidence(
            requestTransfer.getStampProvincialResidence())
          .iban(requestTransfer.getIban())
          .postalIban(requestTransfer.getPostalIban())
          .build()
      );
    }

    InstallmentDTO installment = InstallmentDTO.builder()
      .status(InstallmentStatus.UNPAID)
      .iud(Utilities.getRandomIUD())
      .amountCents(requestTransfers.stream()
        .mapToLong(MixedTransferDTO::getAmountCents).sum())
      .balance(null)
      .dueDate(request.getDueDate())
      .debtor(request.getDebtor())
      .legacyPaymentMetadata(null)
      .remittanceInformation(request.getRemittanceInformation())
      .sourceFlowName(request.getSourceFlowName())
      .transfers(transfers)
      .build();

    PaymentOptionDTO paymentOption = PaymentOptionDTO.builder()
      .status(PaymentOptionStatus.UNPAID)
      .paymentOptionIndex(1)
      .paymentOptionType(PaymentOptionTypeEnum.SINGLE_INSTALLMENT)
      .installments(List.of(installment))
      .build();

    return DebtPositionDTO.builder()
      .status(DebtPositionStatus.UNPAID)
      .debtPositionOrigin(request.getDebtPositionOrigin())
      .organizationId(request.getOrganizationId())
      .flagIuvVolatile(true)
      .flagPuPagoPaPayment(true)
      .multiDebtor(false)
      .debtPositionTypeOrgId(debtPositionTypeOrgId)
      .paymentOptions(List.of(paymentOption))
      .build();
  }

  public Map<Long, List<MixedDpAdditionalData>> buildDebtPositionTypeOrgId2TransfersData(
    List<MixedTransferDTO> transfers) {
    Map<Long, List<MixedDpAdditionalData>> debtPositionTypeOrgId2TransfersData = new HashMap<>();

    for (int i = 0; i < transfers.size(); i++) {
      MixedTransferDTO transfer = transfers.get(i);
      Long debtPositionTypeOrgId = transfer.getDebtPositionTypeOrgId();

      if (debtPositionTypeOrgId != null) {
        MixedDpAdditionalData additionalData = MixedDpAdditionalData.builder()
          .transferIndex(i+1)
          .iud(transfer.getIud())
          .legacyPaymentMetadata(transfer.getLegacyPaymentMetadata())
          .balance(transfer.getBalance())
          .build();

        debtPositionTypeOrgId2TransfersData.computeIfAbsent(
            debtPositionTypeOrgId, k -> new ArrayList<>())
          .add(additionalData);
      }
    }

    return debtPositionTypeOrgId2TransfersData;
  }


  private Long getDebtPositionTypeOrgId(
    MixedDebtPositionDTO mixedDebtPositionDTO) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndDebtPositionTypeOrgId(
        mixedDebtPositionDTO.getOrganizationId(),
        DEBT_POSITION_TYPE_MIXED).map(
        DebtPositionTypeOrg::getDebtPositionTypeOrgId)
      .orElseGet(() ->
        mixedDebtPositionTypeOrgRetrieverService.getMixedDebtPositionTypeOrg(
          mixedDebtPositionDTO.getOrganizationId()).getDebtPositionTypeOrgId());
  }

}
