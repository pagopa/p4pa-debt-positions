package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO.PaymentOptionTypeEnum;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.CategoryResolverService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MixedDebtPositionMapper {

  private final MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverService;
  private final CategoryResolverService categoryResolverService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  public DebtPositionDTO mapToDebtPositionDTO(Organization organization, MixedDebtPositionDTO request) {
    if (request == null) {
      return null;
    }

    Long debtPositionTypeOrgId = getDebtPositionTypeOrgId(request);

    List<TransferDTO> transfers = new ArrayList<>();
    List<MixedTransferDTO> requestTransfers = request.getTransfers();
    for (int i = 0; i < requestTransfers.size(); i++) {
      MixedTransferDTO requestTransfer = requestTransfers.get(i);
      Long debtPositionTypeId = debtPositionTypeOrgRepository.findById(requestTransfer.getDebtPositionTypeOrgId())
        .orElseThrow(() -> new NotFoundException(String.format("The debt position type org with id %s is not found", requestTransfer.getDebtPositionTypeOrgId())))
        .getDebtPositionTypeId();
      String category = categoryResolverService.resolveCategory(requestTransfer.getLegacyPaymentMetadata(), debtPositionTypeId, organization.getOrgTypeCode());
      transfers.add(
        TransferDTO.builder()
          .transferIndex(i + 1)
          .orgFiscalCode(organization.getOrgFiscalCode())
          .orgName(organization.getOrgName())
          .amountCents(requestTransfer.getAmountCents())
          .stampType(requestTransfer.getStampType())
          .stampHashDocument(requestTransfer.getStampHashDocument())
          .stampProvincialResidence(
            requestTransfer.getStampProvincialResidence())
          .iban(requestTransfer.getIban())
          .postalIban(requestTransfer.getPostalIban())
          .category(category)
          .remittanceInformation(request.getRemittanceInformation())
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
      .description(request.getDescription())
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

      MixedDpAdditionalData additionalData = MixedDpAdditionalData.builder()
        .transferIndex(i + 1)
        .iud(transfer.getIud())
        .legacyPaymentMetadata(transfer.getLegacyPaymentMetadata())
        .balance(transfer.getBalance())
        .build();

      debtPositionTypeOrgId2TransfersData.computeIfAbsent(
          debtPositionTypeOrgId, k -> new ArrayList<>())
        .add(additionalData);
    }

    return debtPositionTypeOrgId2TransfersData;
  }


  private Long getDebtPositionTypeOrgId(
    MixedDebtPositionDTO mixedDebtPositionDTO) {
    return mixedDebtPositionTypeOrgRetrieverService.getMixedDebtPositionTypeOrg(
      mixedDebtPositionDTO.getOrganizationId()).getDebtPositionTypeOrgId();
  }

}
