package it.gov.pagopa.pu.debtpositions.service.massive.action;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.mapper.PaymentOptionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.*;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.CreateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class InsertionActionMassiveDebtPositionServiceImpl implements InsertionActionMassiveDebtPositionService {

    private final InstallmentNoPIIRepository installmentNoPIIRepository;
    private final CreateDebtPositionService createDebtPositionService;
    private final DebtPositionMapper debtPositionMapper;

    private static final Set<InstallmentStatus> installmentStatusesValidForInsertion = Set.of(InstallmentStatus.CANCELLED, InstallmentStatus.INVALID);
    private static final Set<PaymentOptionStatus> paymentOptionStatusesValidForInsertion = Set.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.EXPIRED, PaymentOptionStatus.PARTIALLY_PAID);
    private static final Set<DebtPositionStatus> debtPositionStatusesValidForInsertion = Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED, DebtPositionStatus.PARTIALLY_PAID);

    public InsertionActionMassiveDebtPositionServiceImpl(InstallmentNoPIIRepository installmentNoPIIRepository, CreateDebtPositionService createDebtPositionService, DebtPositionMapper debtPositionMapper) {
        this.installmentNoPIIRepository = installmentNoPIIRepository;
        this.createDebtPositionService = createDebtPositionService;
        this.debtPositionMapper = debtPositionMapper;
    }

    @Override
    public String handleInsertion(DebtPositionDTO debtPositionSynchronizeDTO, DebtPosition debtPosition, String accessToken, String operatorExternalUserId) {
        Optional<InstallmentNoPII> installment = installmentNoPIIRepository.getByOrganizationIdAndIudAndPaymentOptionIndexAndIuv(
                debtPositionSynchronizeDTO.getOrganizationId(),
                debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud(),
                debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getPaymentOptionIndex(),
                debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIuv()
        );

        if (installment.isPresent() && !installmentStatusesValidForInsertion.contains(installment.get().getStatus())) {
            throw new ConflictErrorException("The installment cannot be created because it already exists");
        }

        if (debtPosition == null) {
            return createDebtPositionService.createDebtPosition(debtPositionSynchronizeDTO, true, accessToken, operatorExternalUserId).getRight();
        }

        if (!debtPositionStatusesValidForInsertion.contains(debtPosition.getStatus())) {
            throw new ConflictErrorException("The installment cannot created because the debt position is not in an allowed status");
        }

        DebtPositionDTO debtPositionDTO = debtPositionMapper.mapToDto(debtPosition);

        PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().stream()
                .filter(po -> po.getPaymentOptionIndex().equals(debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getPaymentOptionIndex()))
                .findFirst()
                .orElse(null);



        if (paymentOptionDTO != null) {
            if (!paymentOptionStatusesValidForInsertion.contains(paymentOptionDTO.getStatus())) {
                throw new ConflictErrorException("The installment cannot created because the payment option is not in an allowed status");
            }
            return createDebtPositionService.createInstallment(debtPositionDTO, true, accessToken, paymentOptionDTO,
                            debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst())
                    .getRight();
        }

        return createDebtPositionService.createPaymentOption(debtPositionSynchronizeDTO, true, accessToken,
                        debtPositionSynchronizeDTO.getPaymentOptions().getFirst())
                .getRight();
    }

}
