package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.repository.*;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionCancelInstallmentServiceImpl;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class DebtPositionDeletionServiceImpl implements DebtPositionDeletionService {

  private final DebtPositionService debtPositionService;
  private final DebtPositionRepository debtPositionRepository;
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentPIIRepository installmentPIIRepository;
  private final TransferRepository transferRepository;
  private final InstallmentMapper installmentMapper;
  private final DebtPositionCancelInstallmentServiceImpl debtPositionCancelInstallmentService;

  public DebtPositionDeletionServiceImpl(DebtPositionService debtPositionService, DebtPositionRepository debtPositionRepository, PaymentOptionRepository paymentOptionRepository, InstallmentPIIRepository installmentPIIRepository, TransferRepository transferRepository, InstallmentMapper installmentMapper, DebtPositionCancelInstallmentServiceImpl debtPositionCancelInstallmentService) {
    this.debtPositionService = debtPositionService;
    this.debtPositionRepository = debtPositionRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentPIIRepository = installmentPIIRepository;
    this.transferRepository = transferRepository;
    this.installmentMapper = installmentMapper;
    this.debtPositionCancelInstallmentService = debtPositionCancelInstallmentService;
  }

  @Override
  @Transactional
  public String deleteDebtPosition(Long debtPositionId, String accessToken, String operatorExternalUserId) {
    log.info("Cancelling debt position having id {}", debtPositionId);

    DebtPositionDTO debtPositionDTO = debtPositionService.getDebtPosition(debtPositionId);

    if (DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      deleteEntireDebtPosition(debtPositionDTO);
      return null;
    }

    if(isIunPresent(debtPositionDTO)) {
      throw new ConflictErrorException("The debt position with id " + debtPositionId + " cannot be deleted because it is been notified");
    }

    if(!InstallmentUtils.DELETABLE_DP_STATUSES.contains(debtPositionDTO.getStatus())){
      throw new ConflictErrorException("The debt position with id " + debtPositionId + " cannot be deleted because is not in allowed status: " + debtPositionDTO.getStatus());
    }

    List<InstallmentDTO> installment2operate = debtPositionDTO.getPaymentOptions().stream()
      .map(PaymentOptionDTO::getInstallments).flatMap(Collection::stream).toList();

    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .partialChange(false)
      .massive(false)
      .build();

    return debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, installment2operate, wfExecutionParameters, accessToken, operatorExternalUserId);
  }


  private void deleteEntireDebtPosition(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> {
          paymentOptionDTO.getInstallments()
            .forEach(installmentDTO -> {
                installmentDTO.getTransfers()
                  .forEach(transferDTO -> transferRepository.deleteById(transferDTO.getTransferId()));
                Installment installment = installmentMapper.mapToModel(installmentDTO);
                installmentPIIRepository.delete(installment);
              }
            );
          paymentOptionRepository.deleteById(paymentOptionDTO.getPaymentOptionId());
        }
      );
    debtPositionRepository.deleteById(debtPositionDTO.getDebtPositionId());
  }

  private boolean isIunPresent(DebtPositionDTO debtPositionDTO) {
    return debtPositionDTO.getPaymentOptions().stream()
      .flatMap(paymentOptionDTO -> paymentOptionDTO.getInstallments()
        .stream())
        .anyMatch(installmentDTO -> StringUtils.isNotBlank(installmentDTO.getIun()));
  }
}
