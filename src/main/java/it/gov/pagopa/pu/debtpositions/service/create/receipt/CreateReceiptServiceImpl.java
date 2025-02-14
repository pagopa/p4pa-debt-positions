package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CreateReceiptServiceImpl implements CreateReceiptService {


  private final ReceiptNoPIIRepository receiptNoPIIRepository;
  private final ReceiptPIIRepository receiptPIIRepository;
  private final ReceiptMapper receiptMapper;
  private final OrganizationService organizationService;
  private final BrokerService brokerService;
  private final DebtPositionSyncService debtPositionSyncService;
  private final DebtPositionMapper debtPositionMapper;
  private final PrimaryOrgInstallmentService primaryOrgInstallmentService;
  private final InstallmentUpdateService installmentUpdateService;

  public CreateReceiptServiceImpl(ReceiptNoPIIRepository receiptNoPIIRepository, ReceiptPIIRepository receiptPIIRepository,
                                  ReceiptMapper receiptMapper, OrganizationService organizationService,
                                  BrokerService brokerService, DebtPositionSyncService debtPositionSyncService,
                                  DebtPositionMapper debtPositionMapper, PrimaryOrgInstallmentService primaryOrgInstallmentService,
                                  InstallmentUpdateService installmentUpdateService) {
    this.receiptNoPIIRepository = receiptNoPIIRepository;
    this.receiptPIIRepository = receiptPIIRepository;
    this.receiptMapper = receiptMapper;
    this.organizationService = organizationService;
    this.brokerService = brokerService;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionMapper = debtPositionMapper;
    this.primaryOrgInstallmentService = primaryOrgInstallmentService;
    this.installmentUpdateService = installmentUpdateService;
  }

  @Override
  @Transactional
  public ReceiptDTO createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    log.info("createReceipt paymentReceiptId[{}} org/nav[{}/{}]", receiptDTO.getPaymentReceiptId(), receiptDTO.getOrgFiscalCode(), receiptDTO.getNoticeNumber());

    //check if the same receipt is already present on DB
    Optional<ReceiptDTO> receiptInDb = checkIfAlreadyStored(receiptDTO);
    if (receiptInDb.isPresent())
      return receiptInDb.get();

    //persist receipt
    saveReceipt(receiptDTO);

    //check if organization who handle the notice is managed by PU
    boolean[] primaryOrgFound = new boolean[]{false};
    organizationService.getOrganizationByFiscalCode(receiptDTO.getOrgFiscalCode(), accessToken)
      .ifPresent(primaryOrg -> primaryOrgInstallmentService.findPrimaryOrgInstallment(primaryOrg, receiptDTO.getNoticeNumber())
        .ifPresent(installment -> {
          log.debug("primaryOrg installment found id[{}]", installment.getInstallmentId());
          Broker primaryBroker = brokerService.findById(primaryOrg.getBrokerId(), accessToken);
          primaryOrgFound[0] = true;
          //update installment status
          DebtPosition debtPosition = installmentUpdateService.updateInstallmentStatusOfDebtPosition(installment, primaryBroker, receiptDTO);
          //start debt position workflow
          DebtPositionDTO debtPositionDTO = debtPositionMapper.mapToDto(debtPosition);
          invokeWorkflow(debtPositionDTO, accessToken);
        }));

    //for every organization handled by PU and mentioned in the receipt
    receiptDTO.getTransfers().stream()
      //get the fiscal code of the organization
      .map(ReceiptTransferDTO::getFiscalCodePA)
      .distinct()
      //exclude primary org in case it has a valid debt position associated to it
      .filter(fiscalCode -> !primaryOrgFound[0] || !fiscalCode.equals(receiptDTO.getOrgFiscalCode()))
      //check if the organization is managed by PU
      .map(fiscalCode -> organizationService.getOrganizationByFiscalCode(fiscalCode, accessToken))
      .filter(Optional::isPresent)
      .map(Optional::get)
      //create a "technical" debt position, in status PAID
      .forEach(organization ->
        //TODO tast P4ADEV-2027
        log.info("TODO P4ADEV-2027 create technical debt position for org[{}] nav[{}]", organization.getOrganizationId(), receiptDTO.getNoticeNumber())
      );

    return receiptDTO;
  }

  // check if the same receipt is already present on DB.
  // in this case just ignore it since it simply means that the same receipt has been broadcast
  // to multiple organizations managed by PU
  private Optional<ReceiptDTO> checkIfAlreadyStored(ReceiptDTO receiptDTO) {
    ReceiptNoPII receiptInDb = receiptNoPIIRepository.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId());
    if (receiptInDb != null) {
      log.info("Receipt with paymentReceiptId[{}] already present in DB id[{}]", receiptDTO.getPaymentReceiptId(), receiptInDb.getReceiptId());
      receiptDTO.setReceiptId(receiptInDb.getReceiptId());
      return Optional.of(receiptDTO);
    }
    return Optional.empty();
  }

  private void saveReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    Receipt receipt = receiptMapper.mapToModel(receiptDTO);
    long newId = receiptPIIRepository.save(receipt);
    receiptDTO.setReceiptId(newId);
    log.debug("Receipt paymentReceiptId[{}} persisted with id[{}]", receiptDTO.getPaymentReceiptId(), newId);
  }


  private void invokeWorkflow(DebtPositionDTO debtPositionDTO, String accessToken) {
    if (!DebtPositionStatus.DRAFT.equals(debtPositionDTO.getStatus())) {
      log.info("Invoking alignment workflow for debt position with id {}", debtPositionDTO.getDebtPositionId());
      WorkflowCreatedDTO workflow = debtPositionSyncService.syncDebtPosition(debtPositionDTO, false, PaymentEventType.RT_RECEIVED, accessToken);
      if (workflow != null) {
        log.info("Workflow creation OK for debtPositionId[{}}: workflowId[{}]", debtPositionDTO.getDebtPositionId(), workflow.getWorkflowId());
      } else {
        log.warn("Workflow creation KO for debtPositionId[{}]: received null response", debtPositionDTO.getDebtPositionId());
      }
    }
  }

}
