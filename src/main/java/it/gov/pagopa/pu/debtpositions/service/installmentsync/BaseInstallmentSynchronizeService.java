package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public abstract class BaseInstallmentSynchronizeService {

  public Pair<PaymentOptionDTO, InstallmentDTO> findInstallment(DebtPositionDTO debtPositionDTO, InstallmentSynchronizeDTO installmentSynchronizeDTO) {
    if (debtPositionDTO == null) {
      return null;
    }

    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionIndex().equals(installmentSynchronizeDTO.getPaymentOptionIndex()))
      .findFirst()
      .orElse(null);

    InstallmentDTO installmentDTO = null;

    if (paymentOptionDTO != null) {
      installmentDTO = paymentOptionDTO.getInstallments().stream()
        .filter(inst -> inst.getIud().equals(installmentSynchronizeDTO.getIud()))
        .findFirst()
        .orElse(null);
    }
    return Pair.of(paymentOptionDTO, installmentDTO);
  }

  public Pair<PaymentOptionDTO, InstallmentDTO> findInstallmentAndThrowException(DebtPositionDTO debtPositionDTO, InstallmentSynchronizeDTO installmentSynchronizeDTO) {
    Pair<PaymentOptionDTO, InstallmentDTO> result = findInstallment(debtPositionDTO, installmentSynchronizeDTO);
    if (result == null) {
      throw new NotFoundException(String.format("[DP_NOT_FOUND] The debt position related to iupd %s was not found", installmentSynchronizeDTO.getIupdOrg()));
    }
    if (result.getLeft() == null) {
      throw new NotFoundException(String.format("[PO_NOT_FOUND] The payment option with index %s of debt position with iupd %s not found",
        installmentSynchronizeDTO.getPaymentOptionIndex(), installmentSynchronizeDTO.getIupdOrg()));
    }
    if (result.getRight() == null) {
      throw new NotFoundException(String.format("[INSTALLMENT_NOT_FOUND] The installment with iud %s not found", installmentSynchronizeDTO.getIud()));
    }
    return result;
  }

  public boolean isInstallmentAlreadyElaborated(InstallmentDTO installmentDTO, InstallmentSynchronizeDTO installmentSynchronizeDTO) {
    if (installmentDTO == null) return false;
    return installmentSynchronizeDTO.getIngestionFlowFileId().equals(installmentDTO.getIngestionFlowFileId()) &&
      installmentSynchronizeDTO.getIngestionFlowFileLineNumber().equals(installmentDTO.getIngestionFlowFileLineNumber());
  }

  public void checkIunPresence(InstallmentDTO installmentDTO) {
    if (StringUtils.isNotBlank(installmentDTO.getIun())) {
      throw new ConflictErrorException("[INVALID_INSTALLMENT_STATUS] The installment with id " + installmentDTO.getInstallmentId() + " cannot be updated or cancelled because is been notified by SEND");
    }
  }

  public void validateStatus(InstallmentDTO installmentDTO, InstallmentSynchronizeDTO installmentSynchronizeDTO) {
    if (InstallmentStatus.TO_SYNC.equals(installmentDTO.getStatus())) {
      if (!installmentSynchronizeDTO.getIngestionFlowFileId().equals(installmentDTO.getIngestionFlowFileId())) {
        throw new ConflictErrorException(String.format("The installment with iud %s cannot be updated or cancelled because there was an error in the previous synchronization", installmentSynchronizeDTO.getIud()));
      } else if (installmentDTO.getSyncStatus() != null && !InstallmentUtils.MODIFIABLE_STATUSES.contains(installmentDTO.getSyncStatus().getSyncStatusTo())) {
        throw new ConflictErrorException(String.format("[INVALID_INSTALLMENT_STATUS] The installment with iud %s cannot be updated or cancelled because is not in an allowed status to: %s",
          installmentSynchronizeDTO.getIud(), installmentDTO.getSyncStatus().getSyncStatusTo()));
      }
    } else if (!InstallmentUtils.MODIFIABLE_STATUSES.contains(installmentDTO.getStatus())) {
      throw new ConflictErrorException(String.format("[INVALID_INSTALLMENT_STATUS] The installment with iud %s cannot be updated or cancelled because is not in an allowed status: %s",
        installmentSynchronizeDTO.getIud(), installmentDTO.getStatus()));
    }
  }
}
