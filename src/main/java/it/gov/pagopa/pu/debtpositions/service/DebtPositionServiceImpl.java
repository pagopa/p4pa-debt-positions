package it.gov.pagopa.pu.debtpositions.service;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import jakarta.transaction.Transactional;
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
  public DebtPositionDTO saveDebtPosition(DebtPositionDTO debtPositionDTO) {
    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedDebtPosition = debtPositionMapper.mapToModel(debtPositionDTO);

    DebtPosition savedDebtPosition = debtPositionRepository.save(mappedDebtPosition.getFirst());

    savedDebtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(savedDebtPosition.getDebtPositionId());
      PaymentOption savedPaymentOption = paymentOptionRepository.save(paymentOption);

      savedPaymentOption.getInstallments().forEach(installmentNoPII -> {
        Installment mappedInstallment = mappedDebtPosition.getSecond().get(installmentNoPII);
        mappedInstallment.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());
        if (StringUtils.isBlank(mappedInstallment.getIud())) {
          String iud = Utilities.getRandomIUD();
          mappedInstallment.setIud(iud);
          installmentNoPII.setIud(iud);
        }
        InstallmentNoPII savedInstallment = installmentRepository.save(mappedInstallment).getNoPII();
        installmentNoPII.setPersonalDataId(savedInstallment.getPersonalDataId());
        installmentNoPII.setInstallmentId(savedInstallment.getInstallmentId());
        installmentNoPII.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());

        mappedInstallment.getTransfers().forEach(transfer -> {
          transfer.setInstallmentId(savedInstallment.getInstallmentId());
          transferRepository.save(transfer);
        });
      });
    });

    return debtPositionMapper.mapToDto(savedDebtPosition);
  }
}

