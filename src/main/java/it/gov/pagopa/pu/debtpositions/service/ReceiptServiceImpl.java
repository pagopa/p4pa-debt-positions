package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidInstallmentStatusException;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
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

  public ReceiptServiceImpl(ReceiptPIIRepository receiptPIIRepository, ReceiptMapper receiptMapper, InstallmentNoPIIRepository installmentNoPIIRepository, OrganizationService organizationService, DebtPositionRepository debtPositionRepository, BrokerService brokerService) {
    this.receiptPIIRepository = receiptPIIRepository;
    this.receiptMapper = receiptMapper;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.organizationService = organizationService;
    this.debtPositionRepository = debtPositionRepository;
    this.brokerService = brokerService;
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
        .stream().filter(installmentDTO -> !installmentDTO.getStatus().equals(InstallmentStatus.REPORTED)
          && !installmentDTO.getStatus().equals(InstallmentStatus.PAID)).toList();

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
        .ifPresent(installmentDTO -> {
          // set status of the found installment to PAID and link it to the receipt
          log.info("Installment [{}] found for receipt [{}]", installmentDTO.getInstallmentId(), receiptDTO.getReceiptId());
          updateInstallmentFields(installmentDTO, InstallmentStatus.PAID, primaryBroker, receiptDTO.getReceiptId());

          // change status of every other installment of different payment options of this debt position to INVALID
          DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installmentDTO.getInstallmentId());

          debtPosition.getPaymentOptions().stream()
            .filter(paymentOption -> !paymentOption.getPaymentOptionId().equals(installmentDTO.getPaymentOptionId()))
            .forEach(paymentOption -> paymentOption.getInstallments().forEach(installment -> {
              if (NOT_PAID.contains(installment.getStatus())) {
                updateInstallmentFields(installment, InstallmentStatus.INVALID, primaryBroker, null);
              }
            }));
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

  private void updateInstallmentFields(InstallmentNoPII installment, InstallmentStatus status, Broker broker, Long receiptId) {
    if (receiptId != null) {
      //installment.setReceiptId(receiptId);  //TODO task P4PADEV-2070
    }
    switch (broker.getPagoPaInteractionModel()) {
      case SYNC -> {
        installment.setStatus(status);

        break;
      }
      case SYNC_ACA -> {
        installment.setSyncStatus(InstallmentSyncStatus.builder()
          .syncStatusFrom(installment.getStatus())
          .syncStatusTo(status)
          .build());
        installment.setStatus(InstallmentStatus.TO_SYNC);
      }
      case SYNC_GPDPRELOAD -> {

      }
      case SYNC_ACA_GPDPRELOAD -> {

      }
      case ASYNC_GPD -> {

      }
    }
  }

}
