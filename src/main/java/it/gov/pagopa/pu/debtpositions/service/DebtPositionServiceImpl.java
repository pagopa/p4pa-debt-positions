package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentPIIRepository installmentRepository;
  private final TransferRepository transferRepository;
  private final DebtPositionMapper debtPositionMapper;

  public DebtPositionServiceImpl(DebtPositionRepository debtPositionRepository, PaymentOptionRepository paymentOptionRepository,
                                 InstallmentPIIRepository installmentRepository, TransferRepository transferRepository,
                                 DebtPositionMapper debtPositionMapper) {
    this.debtPositionRepository = debtPositionRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentRepository = installmentRepository;
    this.transferRepository = transferRepository;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Transactional
  @Override
  public DebtPosition saveDebtPosition(DebtPositionDTO debtPositionDTO, Organization org) {
    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedDebtPosition = debtPositionMapper.mapToModel(debtPositionDTO);

    DebtPosition savedDebtPosition = debtPositionRepository.save(mappedDebtPosition.getFirst());
    mappedDebtPosition.getFirst().setDebtPositionId(savedDebtPosition.getDebtPositionId());
    debtPositionDTO.setDebtPositionId(savedDebtPosition.getDebtPositionId());

    mappedDebtPosition.getFirst().getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(savedDebtPosition.getDebtPositionId());
      PaymentOption savedPaymentOption = paymentOptionRepository.save(paymentOption);
      paymentOption.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());

      PaymentOptionDTO paymentOptionDTO = alignPaymentOptionDTO(debtPositionDTO, savedPaymentOption);

      paymentOption.getInstallments().forEach(installmentNoPII -> {
        Installment mappedInstallment = mappedDebtPosition.getSecond().get(installmentNoPII);
        mappedInstallment.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());

        InstallmentNoPII savedInstallment = installmentRepository.save(mappedInstallment).getNoPII();
        mappedInstallment.setInstallmentId(savedInstallment.getInstallmentId());
        mappedInstallment.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());
        installmentNoPII.setPersonalDataId(savedInstallment.getPersonalDataId());
        installmentNoPII.setInstallmentId(savedInstallment.getInstallmentId());
        installmentNoPII.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());

        InstallmentDTO installmentDTO = alignInstallmentDTO(paymentOptionDTO, savedInstallment);

        mappedInstallment.getTransfers().forEach(transfer -> {
          transfer.setInstallmentId(savedInstallment.getInstallmentId());
          Transfer savedTransfer = transferRepository.save(transfer);
          transfer.setTransferId(savedTransfer.getTransferId());

          alignTransferDTO(installmentDTO, savedTransfer);
        });
      });
    });

    return mappedDebtPosition.getFirst();
  }

  private PaymentOptionDTO alignPaymentOptionDTO(DebtPositionDTO debtPositionDTO, PaymentOption paymentOption) {
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionIndex().equals(paymentOption.getPaymentOptionIndex()))
      .findFirst().orElseThrow();
    paymentOptionDTO.setDebtPositionId(paymentOption.getDebtPositionId());
    paymentOptionDTO.setPaymentOptionId(paymentOption.getPaymentOptionId());
    return paymentOptionDTO;
  }

  private InstallmentDTO alignInstallmentDTO(PaymentOptionDTO paymentOptionDTO, InstallmentNoPII savedInstallment) {
    InstallmentDTO installmentDTO = paymentOptionDTO.getInstallments().stream()
      .filter(i -> i.getIud().equals(savedInstallment.getIud()))
      .findFirst().orElseThrow();
    installmentDTO.setInstallmentId(savedInstallment.getInstallmentId());
    installmentDTO.setPaymentOptionId(savedInstallment.getPaymentOptionId());
    return installmentDTO;
  }

  private void alignTransferDTO(InstallmentDTO installmentDTO, Transfer savedTransfer) {
    TransferDTO transferDTO = installmentDTO.getTransfers().stream()
      .filter(t -> t.getTransferIndex().equals(savedTransfer.getTransferIndex()))
      .findFirst().orElseThrow();
    transferDTO.setTransferId(savedTransfer.getTransferId());
    transferDTO.setInstallmentId(savedTransfer.getInstallmentId());
  }

  @Transactional
  @Override
  public DebtPositionDTO saveDebtPositionAndRemap(DebtPositionDTO debtPositionDTO, Organization org) {
    return debtPositionMapper.mapToDto(saveDebtPosition(debtPositionDTO, org));
  }

  @Override
  public DebtPositionDTO getDebtPosition(Long debtPositionId) {
    DebtPosition debtPosition = debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId);
    if (debtPosition == null) {
      throw new NotFoundException("DebtPosition having debtPositionId %d not found".formatted(debtPositionId));
    }
    return debtPositionMapper.mapToDto(debtPosition);
  }

  @Override
  public PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, Pageable pageable) {
    Page<DebtPosition> pagedDebtPositionsDTO = debtPositionRepository.findByIngestionFlowFileId(ingestionFlowFileId, pageable);

    return debtPositionMapper.mapToPagedDebtPositions(pagedDebtPositionsDTO);
  }
}

