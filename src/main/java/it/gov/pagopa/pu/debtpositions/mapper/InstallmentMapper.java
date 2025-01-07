package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class InstallmentMapper {

  private final TransferMapper transferMapper;

  public InstallmentMapper(TransferMapper transferMapper) {
    this.transferMapper = transferMapper;
  }

  public InstallmentDTO mapToDto(InstallmentNoPII installment) {
    return InstallmentDTO.builder()
      .installmentId(installment.getInstallmentId())
      .paymentOptionId(installment.getPaymentOptionId())
      .status(installment.getStatus())
      .iud(installment.getIud())
      .iuv(installment.getIuv())
      .iur(installment.getIur())
      .dueDate(installment.getDueDate())
      .paymentTypeCode(installment.getPaymentTypeCode())
      .amountCents(installment.getAmountCents())
      .notificationFeeCents(installment.getNotificationFeeCents())
      .remittanceInformation(installment.getRemittanceInformation())
      .humanFriendlyRemittanceInformation(installment.getHumanFriendlyRemittanceInformation())
      .transfers(installment.getTransfers().stream()
        .map(transferMapper::mapToDto)
        .collect(Collectors.toList()))
      .creationDate(installment.getCreationDate())
      .updateDate(installment.getUpdateDate())
      .build();
  }

  public InstallmentNoPII mapToModel(InstallmentDTO dto) {
    InstallmentNoPII installmentNoPII = new InstallmentNoPII();
    installmentNoPII.setInstallmentId(dto.getInstallmentId());
    installmentNoPII.setPaymentOptionId(dto.getPaymentOptionId());
    installmentNoPII.setStatus(dto.getStatus());
    installmentNoPII.setIud(dto.getIud());
    installmentNoPII.setIuv(dto.getIuv());
    installmentNoPII.setIur(dto.getIur());
    installmentNoPII.setDueDate(dto.getDueDate());
    installmentNoPII.setPaymentTypeCode(dto.getPaymentTypeCode());
    installmentNoPII.setAmountCents(dto.getAmountCents());
    installmentNoPII.setNotificationFeeCents(dto.getNotificationFeeCents());
    installmentNoPII.setRemittanceInformation(dto.getRemittanceInformation());
    installmentNoPII.setHumanFriendlyRemittanceInformation(dto.getHumanFriendlyRemittanceInformation());
    installmentNoPII.setTransfers(dto.getTransfers().stream()
      .map(transferMapper::mapToModel)
      .collect(Collectors.toList()));
    installmentNoPII.setCreationDate(dto.getCreationDate());
    installmentNoPII.setUpdateDate(dto.getUpdateDate());
    return installmentNoPII;
  }

}
