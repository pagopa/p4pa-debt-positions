package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.mapper.PaymentOptionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.TransferMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.*;
import jakarta.transaction.Transactional;

public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentPIIRepository installmentRepository;
  private final TransferRepository transferRepository;
  private final DebtPositionMapper debtPositionMapper;
  private final PaymentOptionMapper paymentOptionMapper;
  private final InstallmentMapper installmentMapper;
  private final TransferMapper transferMapper;

  public DebtPositionServiceImpl(DebtPositionRepository debtPositionRepository, PaymentOptionRepository paymentOptionRepository, InstallmentPIIRepository installmentRepository, TransferRepository transferRepository, DebtPositionMapper debtPositionMapper, PaymentOptionMapper paymentOptionMapper, InstallmentMapper installmentMapper, TransferMapper transferMapper) {
    this.debtPositionRepository = debtPositionRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentRepository = installmentRepository;
    this.transferRepository = transferRepository;
    this.debtPositionMapper = debtPositionMapper;
    this.paymentOptionMapper = paymentOptionMapper;
    this.installmentMapper = installmentMapper;
    this.transferMapper = transferMapper;
  }

  @Transactional
  @Override
  public void saveDebtPosition(DebtPositionDTO debtPositionDTO) {
    DebtPosition debtPosition = debtPositionMapper.mapToModel(debtPositionDTO);
    DebtPosition savedDebtPosition = debtPositionRepository.save(debtPosition);

    debtPositionDTO.getPaymentOptions().forEach(paymentOptionDTO -> {
      PaymentOption paymentOption = paymentOptionMapper.mapToModel(paymentOptionDTO);
      paymentOption.setDebtPositionId(savedDebtPosition.getDebtPositionId());
      Long paymentOptionId = paymentOptionRepository.save(paymentOption).getPaymentOptionId();

      paymentOptionDTO.getInstallments().forEach(installmentDTO -> {
        Installment installment = installmentMapper.mapToModel(installmentDTO);
        installment.setPaymentOptionId(paymentOptionId);
        Long installmentId = installmentRepository.save(installment);

        installmentDTO.getTransfers().forEach(transferDTO -> {
          Transfer transfer = transferMapper.mapToModel(transferDTO);
          transfer.setInstallmentId(installmentId);
          transferRepository.save(transfer);
        });
      });
    });
  }

}

