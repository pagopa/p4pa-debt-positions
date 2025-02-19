package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.CreateDebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class InsertInstallmentServiceImpl {

  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final CreateDebtPositionService createDebtPositionService;
  private final DebtPositionMapper debtPositionMapper;

  private static final Set<InstallmentStatus> installmentStatusesValidForInsertion = Set.of(InstallmentStatus.CANCELLED, InstallmentStatus.INVALID);
  private static final Set<PaymentOptionStatus> paymentOptionStatusesValidForInsertion = Set.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.EXPIRED, PaymentOptionStatus.PARTIALLY_PAID);
  private static final Set<DebtPositionStatus> debtPositionStatusesValidForInsertion = Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED, DebtPositionStatus.PARTIALLY_PAID);

  public InsertInstallmentServiceImpl(InstallmentNoPIIRepository installmentNoPIIRepository, CreateDebtPositionService createDebtPositionService, DebtPositionMapper debtPositionMapper) {
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.createDebtPositionService = createDebtPositionService;
    this.debtPositionMapper = debtPositionMapper;
  }

  public String handleInsertion(DebtPositionDTO debtPositionSynchronizeDTO, DebtPosition storedDebtPosition, Boolean massive, String accessToken, String operatorExternalUserId) {
    if (storedDebtPosition == null) {
      return createDebtPositionService.createDebtPosition(debtPositionSynchronizeDTO, massive, accessToken, operatorExternalUserId).getRight();
    }

    if (!debtPositionStatusesValidForInsertion.contains(storedDebtPosition.getStatus())) {
      throw new ConflictErrorException(String.format("The installment cannot created because the debt position with id %s is not in an allowed status %s",
        storedDebtPosition.getDebtPositionId(), storedDebtPosition.getStatus()));
    }

    DebtPositionDTO debtPositionDTO = debtPositionMapper.mapToDto(storedDebtPosition);

    PaymentOptionDTO storedPaymentOptionDTO = debtPositionDTO.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionIndex().equals(debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getPaymentOptionIndex()))
      .findFirst()
      .orElse(null);

    if (storedPaymentOptionDTO == null) {
      PaymentOptionDTO paymentOptionSyncDTO = debtPositionSynchronizeDTO.getPaymentOptions().getFirst();
      paymentOptionSyncDTO.setDebtPositionId(storedDebtPosition.getDebtPositionId());
      return createDebtPositionService.createPaymentOption(debtPositionDTO, massive, accessToken, paymentOptionSyncDTO)
        .getRight();
    }

    if (!paymentOptionStatusesValidForInsertion.contains(storedPaymentOptionDTO.getStatus())) {
      throw new ConflictErrorException(String.format("The installment cannot created because the payment option with id %s is not in an allowed status %s",
        storedPaymentOptionDTO.getPaymentOptionId(), storedPaymentOptionDTO.getStatus()));
    }

    Optional<InstallmentNoPII> storedInstallment = installmentNoPIIRepository.getByOrganizationIdAndIudAndPaymentOptionIndexAndIuv(
      debtPositionSynchronizeDTO.getOrganizationId(),
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud(),
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getPaymentOptionIndex(),
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIuv()
    );

    if (storedInstallment.map(i -> !installmentStatusesValidForInsertion.contains(i.getStatus())).orElse(false)) {
      throw new ConflictErrorException(String.format("The installment with id %s cannot be created because it already exists in a not modifiable status: %s",
        storedInstallment.get().getInstallmentId(), storedInstallment.get().getStatus()));
    }

    InstallmentDTO installmentSyncDTO = debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    installmentSyncDTO.setPaymentOptionId(storedPaymentOptionDTO.getPaymentOptionId());

    return createDebtPositionService.createInstallment(debtPositionDTO, massive, accessToken, installmentSyncDTO)
      .getRight();

  }
}
