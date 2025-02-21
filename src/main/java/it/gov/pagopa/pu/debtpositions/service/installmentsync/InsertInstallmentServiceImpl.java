package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.CreateDebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class InsertInstallmentServiceImpl {

  private final CreateDebtPositionService createDebtPositionService;
  private final DebtPositionMapper debtPositionMapper;

  private static final Set<InstallmentStatus> installmentStatusesValidForInsertion = Set.of(InstallmentStatus.CANCELLED, InstallmentStatus.INVALID);
  private static final Set<PaymentOptionStatus> paymentOptionStatusesValidForInsertion = Set.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.EXPIRED, PaymentOptionStatus.PARTIALLY_PAID);
  private static final Set<DebtPositionStatus> debtPositionStatusesValidForInsertion = Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED, DebtPositionStatus.PARTIALLY_PAID);

  public InsertInstallmentServiceImpl(CreateDebtPositionService createDebtPositionService, DebtPositionMapper debtPositionMapper) {
    this.createDebtPositionService = createDebtPositionService;
    this.debtPositionMapper = debtPositionMapper;
  }

  public String handleInsertion(DebtPositionDTO debtPositionToSyncDTO, DebtPosition storedDebtPosition, Boolean massive, String accessToken, String operatorExternalUserId) {
    if (storedDebtPosition == null) {
      return createDebtPositionService.createDebtPosition(debtPositionToSyncDTO, massive, accessToken, operatorExternalUserId).getRight();
    }

    if (!debtPositionStatusesValidForInsertion.contains(storedDebtPosition.getStatus())) {
      throw new ConflictErrorException(String.format("The installment cannot created because the debt position with id %s is not in an allowed status %s",
        storedDebtPosition.getDebtPositionId(), storedDebtPosition.getStatus()));
    }

    DebtPositionDTO debtPositionDTO = debtPositionMapper.mapToDto(storedDebtPosition);

    PaymentOptionDTO paymentOptionToSyncDTO = debtPositionToSyncDTO.getPaymentOptions().getFirst();

    PaymentOption storedPaymentOption = storedDebtPosition.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionIndex().equals(paymentOptionToSyncDTO.getPaymentOptionIndex()))
      .findFirst()
      .orElse(null);

    if (storedPaymentOption == null) {
      paymentOptionToSyncDTO.setDebtPositionId(storedDebtPosition.getDebtPositionId());
      debtPositionDTO.getPaymentOptions().add(paymentOptionToSyncDTO);

      return createDebtPositionService.createDebtPosition(debtPositionDTO, massive, accessToken, operatorExternalUserId)
        .getRight();
    }

    if (!paymentOptionStatusesValidForInsertion.contains(storedPaymentOption.getStatus())) {
      throw new ConflictErrorException(String.format("The installment cannot created because the payment option with id %s is not in an allowed status %s",
        storedPaymentOption.getPaymentOptionId(), storedPaymentOption.getStatus()));
    }

    InstallmentDTO installmentToSyncDTO = debtPositionToSyncDTO.getPaymentOptions().getFirst().getInstallments().getFirst();

    InstallmentNoPII storedInstallment = storedPaymentOption.getInstallments().stream()
      .filter(inst -> inst.getIud().equals(installmentToSyncDTO.getIud())).findFirst().orElse(null);

    if (storedInstallment != null && !installmentStatusesValidForInsertion.contains(storedInstallment.getStatus())) {
      throw new ConflictErrorException(String.format("The installment with id %s cannot be created because it already exists in a not modifiable status: %s",
        storedInstallment.getInstallmentId(), storedInstallment.getStatus()));
    }

    installmentToSyncDTO.setPaymentOptionId(storedPaymentOption.getPaymentOptionId());

    debtPositionDTO.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionId().equals(storedPaymentOption.getPaymentOptionId()))
      .findFirst()
      .ifPresent(po -> po.getInstallments().add(installmentToSyncDTO));

    return createDebtPositionService.createDebtPosition(debtPositionDTO, massive, accessToken, operatorExternalUserId)
      .getRight();
  }
}
