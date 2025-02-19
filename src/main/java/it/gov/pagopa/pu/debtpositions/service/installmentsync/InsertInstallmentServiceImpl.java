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

    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionIndex().equals(debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getPaymentOptionIndex()))
      .findFirst()
      .orElse(null);

    if (paymentOptionDTO == null) {
      return createDebtPositionService.createPaymentOption(debtPositionDTO, massive, accessToken,
          debtPositionSynchronizeDTO.getPaymentOptions().getFirst())
        .getRight();
    }

    if (!paymentOptionStatusesValidForInsertion.contains(paymentOptionDTO.getStatus())) {
      throw new ConflictErrorException(String.format("The installment cannot created because the payment option with id %s is not in an allowed status %s",
        paymentOptionDTO.getPaymentOptionId(), paymentOptionDTO.getStatus()));
    }

    Optional<InstallmentNoPII> installment = installmentNoPIIRepository.getByOrganizationIdAndIudAndPaymentOptionIndexAndIuv(
      debtPositionSynchronizeDTO.getOrganizationId(),
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud(),
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getPaymentOptionIndex(),
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIuv()
    );

    if (installment.map(i -> !installmentStatusesValidForInsertion.contains(i.getStatus())).orElse(false)) {
      throw new ConflictErrorException(String.format("The installment with id %s cannot be created because it already exists in a not modifiable status: %s",
        installment.get().getInstallmentId(), installment.get().getStatus()));
    }

    return createDebtPositionService.createInstallment(debtPositionDTO, massive, accessToken, paymentOptionDTO,
        debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst())
      .getRight();

  }
}
