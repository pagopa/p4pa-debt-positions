package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.mapper.PaymentOptionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.TransferMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DebtPositionSaveService {

  private final DebtPositionRepository debtPositionRepository;
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentPIIRepository installmentRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final TransferRepository transferRepository;
  private final DebtPositionMapper debtPositionMapper;

  public DebtPositionSaveService(DebtPositionRepository debtPositionRepository, PaymentOptionRepository paymentOptionRepository,
                                 InstallmentPIIRepository installmentRepository, InstallmentNoPIIRepository installmentNoPIIRepository, TransferRepository transferRepository,
                                 DebtPositionMapper debtPositionMapper) {
    this.debtPositionRepository = debtPositionRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentRepository = installmentRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.transferRepository = transferRepository;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Transactional
  public void saveDebtPositionDTO(DebtPositionDTO debtPositionDTO) {
    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedDebtPosition = debtPositionMapper.mapToModel(debtPositionDTO);

    DebtPosition savedDebtPosition = debtPositionRepository.save(mappedDebtPosition.getFirst());
    alignDebtPositionDTO(debtPositionDTO, savedDebtPosition);

    mappedDebtPosition.getFirst().getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(savedDebtPosition.getDebtPositionId());
      PaymentOption savedPaymentOption = paymentOptionRepository.save(paymentOption);
      PaymentOptionDTO paymentOptionDTO = alignPaymentOptionDTO(debtPositionDTO, savedPaymentOption);

      paymentOption.getInstallments().forEach(installmentNoPII -> {
        Installment mappedInstallment = mappedDebtPosition.getSecond().get(installmentNoPII);

        mappedInstallment.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());
        InstallmentNoPII savedInstallment = installmentRepository.save(mappedInstallment).getNoPII();
        InstallmentDTO installmentDTO = alignInstallmentDTO(paymentOptionDTO, savedInstallment);

        mappedInstallment.getTransfers().forEach(transfer -> {
          transfer.setInstallmentId(savedInstallment.getInstallmentId());
          Transfer savedTransfer = transferRepository.save(transfer);
          alignTransferDTO(installmentDTO, savedTransfer);
        });
      });
    });
  }

  private static void alignDebtPositionDTO(DebtPositionDTO debtPositionDTO, DebtPosition savedDebtPosition) {
    DebtPositionMapper.setToDtoAutoDbFields(debtPositionDTO, savedDebtPosition);
  }

  private PaymentOptionDTO alignPaymentOptionDTO(DebtPositionDTO debtPositionDTO, PaymentOption savedPaymentOption) {
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionIndex().equals(savedPaymentOption.getPaymentOptionIndex()))
      .findFirst().orElseThrow();
    paymentOptionDTO.setDebtPositionId(savedPaymentOption.getDebtPositionId());

    PaymentOptionMapper.setToDtoAutoDbFields(paymentOptionDTO, savedPaymentOption);
    return paymentOptionDTO;
  }

  private InstallmentDTO alignInstallmentDTO(PaymentOptionDTO paymentOptionDTO, InstallmentNoPII savedInstallment) {
    InstallmentDTO installmentDTO = paymentOptionDTO.getInstallments().stream()
      .filter(i -> i.getIud().equals(savedInstallment.getIud()))
      .findFirst().orElseThrow();
    installmentDTO.setPaymentOptionId(savedInstallment.getPaymentOptionId());

    InstallmentMapper.setToDtoAutoDbFields(installmentDTO, savedInstallment);
    return installmentDTO;
  }

  private void alignTransferDTO(InstallmentDTO installmentDTO, Transfer savedTransfer) {
    TransferDTO transferDTO = installmentDTO.getTransfers().stream()
      .filter(t -> t.getTransferIndex().equals(savedTransfer.getTransferIndex()))
      .findFirst().orElseThrow();
    transferDTO.setInstallmentId(savedTransfer.getInstallmentId());

    TransferMapper.setToDtoAutoDbFields(transferDTO, savedTransfer);
  }

  @Transactional
  public void saveDebtPosition(DebtPosition debtPosition) {
    DebtPosition savedDebtPosition = debtPositionRepository.save(debtPosition);
    alignDebtPosition(debtPosition, savedDebtPosition);

    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(savedDebtPosition.getDebtPositionId());
      PaymentOption savedPaymentOption = paymentOptionRepository.save(paymentOption);
      alignPaymentOption(paymentOption, savedPaymentOption);

      paymentOption.getInstallments().forEach(installmentNoPII -> {
        installmentNoPII.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());
        InstallmentNoPII savedInstallment = installmentNoPIIRepository.save(installmentNoPII);
        alignInstallmentNoPII(installmentNoPII, savedInstallment);

        installmentNoPII.getTransfers().forEach(transfer -> {
          transfer.setInstallmentId(savedInstallment.getInstallmentId());
          Transfer savedTransfer = transferRepository.save(transfer);
          alignTransfer(transfer, savedTransfer);
        });
      });
    });
  }

  private void alignDebtPosition(DebtPosition debtPosition, DebtPosition savedDebtPosition) {
    debtPosition.setDebtPositionId(savedDebtPosition.getDebtPositionId());
    debtPosition.setCreationDate(savedDebtPosition.getCreationDate());
    debtPosition.setUpdateDate(savedDebtPosition.getUpdateDate());
    debtPosition.setUpdateOperatorExternalId(savedDebtPosition.getUpdateOperatorExternalId());
  }

  private void alignPaymentOption(PaymentOption paymentOption, PaymentOption savedPaymentOption) {
    paymentOption.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());
    paymentOption.setCreationDate(savedPaymentOption.getCreationDate());
    paymentOption.setUpdateDate(savedPaymentOption.getUpdateDate());
    paymentOption.setUpdateOperatorExternalId(savedPaymentOption.getUpdateOperatorExternalId());
  }

  private void alignInstallmentNoPII(InstallmentNoPII installmentNoPII, InstallmentNoPII savedInstallmentNoPII) {
    installmentNoPII.setInstallmentId(savedInstallmentNoPII.getInstallmentId());
    installmentNoPII.setCreationDate(savedInstallmentNoPII.getCreationDate());
    installmentNoPII.setUpdateDate(savedInstallmentNoPII.getUpdateDate());
    installmentNoPII.setUpdateOperatorExternalId(savedInstallmentNoPII.getUpdateOperatorExternalId());
  }

  private void alignTransfer(Transfer transfer, Transfer savedTransfer) {
    transfer.setTransferId(savedTransfer.getTransferId());
    transfer.setCreationDate(savedTransfer.getCreationDate());
    transfer.setUpdateDate(savedTransfer.getUpdateDate());
    transfer.setUpdateOperatorExternalId(savedTransfer.getUpdateOperatorExternalId());
  }

}

