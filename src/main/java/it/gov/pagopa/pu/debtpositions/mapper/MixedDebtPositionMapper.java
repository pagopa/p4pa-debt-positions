package it.gov.pagopa.pu.debtpositions.mapper;

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
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MixedDebtPositionMapper {

  public DebtPositionDTO mapToDebtPositionDTO(MixedDebtPositionDTO request,
    Long debtPositionTypeOrgId) {

    List<TransferDTO> transfers = new ArrayList<>();
    List<MixedTransferDTO> requestTransfers = request.getTransfers();
    for (int i = 0; i < requestTransfers.size(); i++) {
      MixedTransferDTO requestTransfer = requestTransfers.get(i);
      transfers.add(
        TransferDTO.builder()
          .transferIndex(i)
          .amountCents(requestTransfer.getAmountCents())
          .stampType(requestTransfer.getStampType())
          .stampHashDocument(requestTransfer.getStampHashDocument())
          .stampProvincialResidence(requestTransfer.getStampProvincialResidence())
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
      .legacyPaymentMetadata("TODO")
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

}
