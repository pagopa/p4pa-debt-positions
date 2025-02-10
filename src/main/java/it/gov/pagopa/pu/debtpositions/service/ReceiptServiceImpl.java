package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidInstallmentStatusException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class ReceiptServiceImpl implements ReceiptService {

  private enum CHECK_MODE {EXACTLY_ONE, MOST_RECENT}

  private static final Set<InstallmentStatus> NOT_PAID = Set.of(InstallmentStatus.DRAFT, InstallmentStatus.TO_SYNC, InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED);

  private final ReceiptPIIRepository receiptPIIRepository;
  private final ReceiptMapper receiptMapper;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final OrganizationService organizationService;
  private final DebtPositionRepository debtPositionRepository;
  private final BrokerService brokerService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final DebtPositionMapper debtPositionMapper;

  public ReceiptServiceImpl(ReceiptPIIRepository receiptPIIRepository, ReceiptMapper receiptMapper,
                            InstallmentNoPIIRepository installmentNoPIIRepository, OrganizationService organizationService,
                            DebtPositionRepository debtPositionRepository, BrokerService brokerService,
                            DebtPositionSyncService debtPositionSyncService, DebtPositionMapper debtPositionMapper) {
    this.receiptPIIRepository = receiptPIIRepository;
    this.receiptMapper = receiptMapper;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.organizationService = organizationService;
    this.debtPositionRepository = debtPositionRepository;
    this.brokerService = brokerService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Override
  @Transactional
  public ReceiptDTO createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    //persist receipt
    Receipt receipt = receiptMapper.mapToModel(receiptDTO);
    long newId = receiptPIIRepository.save(receipt);
    receiptDTO.setReceiptId(newId);

    //check if organization who handle the notice is managed by PU
    Optional<Organization> primaryOrg = organizationService.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken);

    if (primaryOrg.isPresent()) {
      Broker primaryBroker = brokerService.findById(primaryOrg.get().getBrokerId(), accessToken);
      // check installment by orgId/noticeNumber
      // filter out installments with status PAID and REPORTED
      List<InstallmentNoPII> installmentList = installmentNoPIIRepository.getByOrganizationIdAndNav(primaryOrg.get().getOrganizationId(), receiptDTO.getNoticeNumber())
        .stream().filter(anInstallment -> !anInstallment.getStatus().equals(InstallmentStatus.REPORTED)
          && !anInstallment.getStatus().equals(InstallmentStatus.PAID)).toList();

      /*
       * Apply this rules to find if a valid installment is associated to the receipt:
       * 1. >1 installments with status UNPAID -> throw exception
       * 2. 1 installment with status UNPAID -> use it
       * 3. >1 installment with status TO_SYNC and sync_status_to UNPAID -> throw exception
       * 4. 1 installment with status TO_SYNC and sync_status_to UNPAID -> use it
       * 5. >=1 installments with status EXPIRED -> use the most recent
       * 6. (default) -> not found (nothing to do)
       */
      checkInstallment(installmentList, CHECK_MODE.EXACTLY_ONE, InstallmentStatus.UNPAID, null)
        .or(() -> checkInstallment(installmentList, CHECK_MODE.EXACTLY_ONE, InstallmentStatus.TO_SYNC, InstallmentStatus.UNPAID))
        .or(() -> checkInstallment(installmentList, CHECK_MODE.MOST_RECENT, InstallmentStatus.EXPIRED, null))
        .ifPresent(installment -> {
          DebtPosition debtPosition = updateInstallmentStatusOfDebtPosition(installment, primaryBroker, receiptDTO);
          // start debt position workflow
          DebtPositionDTO debtPositionDTO = debtPositionMapper.mapToDto(debtPosition);
          invokeWorkflow(debtPositionDTO, accessToken);
        });
    }

    // for every organization having a non-primary transfer, check if it managed by PU
    // if so, create a "technical" installment (and debt position) for it, in status PAID
    // TODO task P4ADEV-2027

    return receiptDTO;
  }

  private Optional<InstallmentNoPII> checkInstallment(List<InstallmentNoPII> installmentList, CHECK_MODE checkMode, InstallmentStatus status, InstallmentStatus syncStatusTo) {
    return installmentList.stream()
      .filter(installmentDTO -> status.equals(installmentDTO.getStatus())
        && (syncStatusTo == null || (installmentDTO.getSyncStatus() != null && syncStatusTo.equals(installmentDTO.getSyncStatus().getSyncStatusTo()))))
      .reduce((a, b) -> {
        if (checkMode.equals(CHECK_MODE.MOST_RECENT)) {
          return a.getUpdateDate().isAfter(b.getUpdateDate()) ? a : b;
        } else {  //mode EXACTLY_ONE
          throw new InvalidInstallmentStatusException("Multiple installments with status " + status
            + (syncStatusTo != null ? " and syncStatusTo " + syncStatusTo : ""));
        }
      });
  }

  private DebtPosition updateInstallmentStatusOfDebtPosition(InstallmentNoPII installment, Broker broker, ReceiptDTO receiptDTO) {
    //retrieve debt position
    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installment.getInstallmentId());

    //update installment status
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      if (paymentOption.getPaymentOptionId().equals(installment.getPaymentOptionId())) {
        // current payment option: find this installment
        InstallmentNoPII primaryInstallment = paymentOption.getInstallments().stream()
          .filter(anInstallment -> !anInstallment.getInstallmentId().equals(installment.getInstallmentId()))
          .findFirst().orElseThrow(() -> new NotFoundException("installment not found " + installment.getInstallmentId()));
        // set status of the found installment to PAID and link it to the receipt
        log.info("Installment [{}] found for receipt [{}]", primaryInstallment.getInstallmentId(), receiptDTO.getReceiptId());
        updateInstallmentFields(primaryInstallment, InstallmentStatus.PAID, broker, receiptDTO.getReceiptId());
      } else {
        // change status of every other installment of different payment options of this debt position to INVALID
        paymentOption.getInstallments().forEach(anInstallment -> {
          if (NOT_PAID.contains(anInstallment.getStatus())) {
            updateInstallmentFields(anInstallment, InstallmentStatus.INVALID, broker, null);
          }
        });
      }
    });

    return debtPosition;
  }

  private void updateInstallmentFields(InstallmentNoPII installment, InstallmentStatus status, Broker broker, Long receiptId) {
    if (receiptId != null) {
      installment.setReceiptId(receiptId);
    }
    switch (broker.getPagoPaInteractionModel()) {
      case SYNC -> installment.setStatus(status);
      case SYNC_ACA, SYNC_GPDPRELOAD, SYNC_ACA_GPDPRELOAD, ASYNC_GPD ->
        updateSyncStatus(installment, status);
    }
  }

  private void updateSyncStatus(InstallmentNoPII installment, InstallmentStatus to) {
    installment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(installment.getStatus())
      .syncStatusTo(to)
      .build()
    );
    installment.setStatus(InstallmentStatus.TO_SYNC);
  }

  private void invokeWorkflow(DebtPositionDTO debtPositionDTO, String accessToken) {
    WorkflowCreatedDTO workflow = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, accessToken, false);
    if (workflow != null) {
      log.info("Workflow creation OK for debtPositionId[{}}: workflowId[{}]", debtPositionDTO.getDebtPositionId(), workflow.getWorkflowId());
    } else {
      log.warn("Workflow creation KO for debtPositionId[{}]: received null response", debtPositionDTO.getDebtPositionId());
    }
  }

}
