package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public abstract class BaseInstallmentSynchronizeService {

  private static final Set<InstallmentStatus> installmentStatusesValidForUpdate =
    Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED, InstallmentStatus.DRAFT);

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
      throw new NotFoundException(String.format("The debt position related to iupd %s was not found", installmentSynchronizeDTO.getIupdOrg()));
    }
    if (result.getLeft() == null) {
      throw new NotFoundException(String.format("The payment option with index %s not found", installmentSynchronizeDTO.getPaymentOptionIndex()));
    }
    if (result.getRight() == null) {
      throw new NotFoundException(String.format("The installment with iud %s not found", installmentSynchronizeDTO.getIud()));
    }
    return result;
  }

  public void validateStatus(InstallmentDTO installmentDTO, InstallmentSynchronizeDTO installmentSynchronizeDTO) {
    if (InstallmentStatus.TO_SYNC.equals(installmentDTO.getStatus())) {
      if (!installmentSynchronizeDTO.getIngestionFlowFileId().equals(installmentDTO.getIngestionFlowFileId())) {
        throw new ConflictErrorException(String.format("The installment with %s cannot be updated or cancelled because there was an error in the previous synchronization", installmentSynchronizeDTO.getIud()));
      } else if (installmentDTO.getSyncStatus() != null && !installmentStatusesValidForUpdate.contains(installmentDTO.getSyncStatus().getSyncStatusTo())) {
        throw new ConflictErrorException(String.format("The installment with %s cannot be updated or cancelled because is not in an allowed status to: %s",
          installmentSynchronizeDTO.getIud(), installmentDTO.getSyncStatus().getSyncStatusTo()));
      }
    } else if (!installmentStatusesValidForUpdate.contains(installmentDTO.getStatus())) {
      throw new ConflictErrorException(String.format("The installment with iud %s cannot be updated or cancelled because is not in an allowed status: %s",
        installmentSynchronizeDTO.getIud(), installmentDTO.getStatus()));
    }
  }
}
