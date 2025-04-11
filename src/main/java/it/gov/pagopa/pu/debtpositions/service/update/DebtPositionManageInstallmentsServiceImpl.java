package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.BaseDebtPositionOperationService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.service.update.applier.DebtPositionManageApplierService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DebtPositionManageInstallmentsServiceImpl extends BaseDebtPositionOperationService implements DebtPositionManageInstallmentsService {

    private final DebtPositionService debtPositionService;
    private final DebtPositionManageApplierService debtPositionManageApplierService;
    private final DebtPositionAddInstallmentService debtPositionAddInstallmentService;
    private final DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService;
    private final DebtPositionCancelInstallmentService debtPositionCancelInstallmentService;

    protected DebtPositionManageInstallmentsServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService, DebtPositionService debtPositionService, DebtPositionSyncService debtPositionSyncService, DebtPositionProcessorService debtPositionProcessorService, OrganizationService organizationService, DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, DebtPositionManageApplierService debtPositionManageApplierService, DebtPositionAddInstallmentService debtPositionAddInstallmentService, DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService, DebtPositionCancelInstallmentService debtPositionCancelInstallmentService) {
        super(authorizeOperatorOnDebtPositionTypeService, debtPositionService, debtPositionSyncService, debtPositionProcessorService, organizationService, debtPositionHierarchyStatusAlignerService);
        this.debtPositionService = debtPositionService;
        this.debtPositionManageApplierService = debtPositionManageApplierService;
        this.debtPositionAddInstallmentService = debtPositionAddInstallmentService;
        this.debtPositionUpdateInstallmentService = debtPositionUpdateInstallmentService;
        this.debtPositionCancelInstallmentService = debtPositionCancelInstallmentService;
    }

    private static final Set<InstallmentStatus> MODIFIABLE_DPI_STATUSES = Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED, InstallmentStatus.DRAFT);
    private static final Set<PaymentOptionStatus> MODIFIABLE_PO_STATUSES = Set.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.EXPIRED, PaymentOptionStatus.PARTIALLY_PAID, PaymentOptionStatus.DRAFT);
    private static final Set<DebtPositionStatus> MODIFIABLE_DP_STATUSES = Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED, DebtPositionStatus.PARTIALLY_PAID, DebtPositionStatus.DRAFT);

    @Override
    @Transactional
    public Pair<DebtPositionDTO, String> manageDebtPositionInstallments(Long debtPositionId, ManageDebtPositionDTO manageDebtPositionDTO, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
        DebtPositionDTO storedDebtPosition = debtPositionService.getDebtPosition(debtPositionId);

        if (!MODIFIABLE_DP_STATUSES.contains(storedDebtPosition.getStatus())) {
            throw new ConflictErrorException(String.format("Debt position with id %s cannot be modified because it is not in an allowed status: %s",
                    debtPositionId, storedDebtPosition.getStatus()));
        }

        storedDebtPosition.setDescription(manageDebtPositionDTO.getDebtPositionDescription());
        storedDebtPosition.setValidityDate(manageDebtPositionDTO.getValidityDate());

        PaymentOptionDTO storedPaymentOption = storedDebtPosition.getPaymentOptions().stream()
                .filter(paymentOptionDTO -> paymentOptionDTO.getPaymentOptionId().equals(manageDebtPositionDTO.getPaymentOptionId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Payment option having id %s not found", manageDebtPositionDTO.getPaymentOptionId())));

        if (!MODIFIABLE_PO_STATUSES.contains(storedPaymentOption.getStatus())) {
            throw new ConflictErrorException(String.format("Payment option having id %s cannot be modified because is not in allowed status: %s", storedPaymentOption.getPaymentOptionId(), storedPaymentOption.getStatus()));
        }

        storedPaymentOption.setDescription(manageDebtPositionDTO.getPaymentOptionDescription());

        Pair<DebtPositionDTO, List<InstallmentDTO>> dpManaged = managePaymentOptionInstallments(storedDebtPosition, storedPaymentOption, manageDebtPositionDTO.getInstallments(), accessToken, operatorExternalUserId);

        Pair<DebtPositionDTO, String> debtPositionManaged = execute(dpManaged.getLeft(), dpManaged.getRight(), wfExecutionParameters, PaymentEventType.DP_UPDATED, accessToken, operatorExternalUserId);

        log.debug("Managed installments for debt position with id {}", storedDebtPosition.getDebtPositionId());
        return debtPositionManaged;
    }

    private Pair<DebtPositionDTO, List<InstallmentDTO>> managePaymentOptionInstallments(DebtPositionDTO debtPositionDTO, PaymentOptionDTO paymentOptionDTO, List<ManageInstallmentDTO> manageInstallments, String accessToken, String operatorExternalUserId) {
        ArrayList<InstallmentDTO> installmentsToAdd = new ArrayList<>();
        ArrayList<InstallmentDTO> installmentsToUpdate = new ArrayList<>();
        ArrayList<InstallmentDTO> installmentsToCancel = new ArrayList<>();

        manageInstallments.forEach(
                manageInstallmentDTO -> {
                    InstallmentDTO manageInstallment = manageInstallmentDTO.getInstallment();
                    switch (manageInstallmentDTO.getAction()) {
                        case I -> {
                            paymentOptionDTO.addInstallmentsItem(manageInstallment);
                            installmentsToAdd.add(manageInstallment);
                        }
                        case M -> {
                            InstallmentDTO storedInstallment = findInstallmentToManage(paymentOptionDTO, manageInstallment.getInstallmentId());
                            debtPositionManageApplierService.merge(manageInstallment, storedInstallment);
                            installmentsToUpdate.add(storedInstallment);
                        }
                        case A -> {
                            InstallmentDTO storedInstallment = findInstallmentToManage(paymentOptionDTO, manageInstallment.getInstallmentId());
                            installmentsToCancel.add(storedInstallment);
                        }
                    }
                }
        );

        WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder().massive(false).partialChange(true).build();

        // TODO task P4ADEV-2669: add check wait for workflow completion
        DebtPositionDTO dpWithInstallmentsAdded = debtPositionAddInstallmentService.addInstallment(debtPositionDTO, installmentsToAdd, wfExecutionParameters, accessToken, operatorExternalUserId).getLeft();
        DebtPositionDTO dpWithInstallmentsUpdated = debtPositionUpdateInstallmentService.updateInstallment(dpWithInstallmentsAdded, installmentsToUpdate, wfExecutionParameters, accessToken, operatorExternalUserId).getLeft();
        DebtPositionDTO dpWithInstallmentCancelled = debtPositionCancelInstallmentService.cancelInstallment(dpWithInstallmentsUpdated, installmentsToCancel, wfExecutionParameters, accessToken, operatorExternalUserId).getLeft();

        List<InstallmentDTO> installment2operate = new ArrayList<>();
        installment2operate.addAll(installmentsToAdd);
        installment2operate.addAll(installmentsToUpdate);
        installment2operate.addAll(installmentsToCancel);

        return Pair.of(dpWithInstallmentCancelled, installment2operate);
    }

    private InstallmentDTO findInstallmentToManage(PaymentOptionDTO paymentOptionDTO, Long installmentId) {
        InstallmentDTO installment = paymentOptionDTO.getInstallments().stream()
                .filter(installmentDTO -> installmentDTO.getInstallmentId().equals(installmentId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("The installment with id %s not found", installmentId)));

        if (!MODIFIABLE_DPI_STATUSES.contains(installment.getStatus())) {
            throw new ConflictErrorException(String.format("Installment having id %s cannot be modified because is not in allowed status: %s", installmentId, installment.getStatus()));
        }
        return installment;
    }

    @Override
    protected DebtPositionDTO applyOperation(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, String accessToken, Organization org) {
        return debtPositionDTO;
    }
}
