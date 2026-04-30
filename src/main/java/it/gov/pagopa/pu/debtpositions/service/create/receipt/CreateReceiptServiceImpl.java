package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.pii.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.mixed.MixedDpPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.PrimaryOrgInstallmentRetrieverService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.PrimaryOrgPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.secondaryorg.SecondaryOrgPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.util.Constants;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class CreateReceiptServiceImpl implements CreateReceiptService {
  private final ReceiptNoPIIRepository receiptNoPIIRepository;
  private final ReceiptPIIRepository receiptPIIRepository;

  private final PrimaryOrgPaymentHandlerService primaryOrgPaymentHandlerService;
  private final SecondaryOrgPaymentHandlerService secondaryOrgPaymentHandlerService;
  private final MixedDpPaymentHandlerService mixedDpPaymentHandlerService;
  private final OrganizationService organizationService;
  private final BrokerService brokerService;
  private final PrimaryOrgInstallmentRetrieverService  primaryOrgInstallmentRetrieverService;

  public CreateReceiptServiceImpl(ReceiptNoPIIRepository receiptNoPIIRepository, ReceiptPIIRepository receiptPIIRepository, PrimaryOrgPaymentHandlerService primaryOrgPaymentHandlerService, SecondaryOrgPaymentHandlerService secondaryOrgPaymentHandlerService, MixedDpPaymentHandlerService mixedDpPaymentHandlerService, OrganizationService organizationService, BrokerService brokerService, PrimaryOrgInstallmentRetrieverService primaryOrgInstallmentRetrieverService) {
    this.receiptNoPIIRepository = receiptNoPIIRepository;
    this.receiptPIIRepository = receiptPIIRepository;
    this.primaryOrgPaymentHandlerService = primaryOrgPaymentHandlerService;
    this.secondaryOrgPaymentHandlerService = secondaryOrgPaymentHandlerService;
    this.mixedDpPaymentHandlerService = mixedDpPaymentHandlerService;
    this.organizationService = organizationService;
    this.brokerService = brokerService;
    this.primaryOrgInstallmentRetrieverService = primaryOrgInstallmentRetrieverService;
  }

  @Override
  @Transactional
  public ReceiptDTO createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    logReceiptData("createReceipt", receiptDTO);

    Pair<Broker, Organization> brokerOrgPair = validateAndRetrieveBrokerOrgPair(receiptDTO.getOrganizationId(), receiptDTO.getOrgFiscalCode(), accessToken);
    Organization primaryOrg = brokerOrgPair.getRight();
    Broker broker = brokerOrgPair.getLeft();

    ReceiptDTO existingReceipt = getExistingReceipt(receiptDTO);

    if (existingReceipt == null && broker != null && Boolean.TRUE.equals(broker.getFlagDelegate())) {
      Optional<InstallmentNoPII> retrievedInstallment =
        primaryOrgInstallmentRetrieverService.retrieve(
          primaryOrg,
          receiptDTO.getNoticeNumber(),
          receiptDTO.getIud()
        );

      if (retrievedInstallment.isEmpty()) {
        log.info("Provided a Receipt for notice {} on organization {} not handled by PU on delegate broker {}", receiptDTO.getNoticeNumber(), receiptDTO.getOrgFiscalCode(), broker.getExternalId());
        return null;
      }
    }

    boolean shouldSave;

    if (existingReceipt == null) {
      shouldSave = true;
    } else {
      shouldSave = shouldUpdateExistingReceipt(receiptDTO, existingReceipt);
    }

    if (shouldSave) {
      // If primaryOrg is null, add UNKNOWN prefix to fiscal code
      if(primaryOrg == null) {
        receiptDTO.setOrgFiscalCode("UNKNOWN_" + receiptDTO.getOrgFiscalCode());
      }

      saveReceipt(receiptDTO);
    }

    Optional<DebtPosition> primaryOrgDp = primaryOrgPaymentHandlerService.handlePayment(primaryOrg, receiptDTO, broker, accessToken);
    secondaryOrgPaymentHandlerService.handle(receiptDTO, accessToken);
    primaryOrgDp.ifPresent(dp -> mixedDpPaymentHandlerService.handle(dp, receiptDTO, accessToken));

    return receiptDTO;
  }

  private Pair<Broker, Organization> validateAndRetrieveBrokerOrgPair(Long receiptOrgId, String receiptOrgFiscalCode, String accessToken) {
    if (Objects.equals(receiptOrgId, Constants.TECHNICAL_UNKNOWN_ORG_ID)) {
      return Pair.of(null, null);
    }

    Organization org = organizationService.getOrganizationById(receiptOrgId, accessToken).orElse(null);
    if (org == null) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_RECEIPT_ORG, String.format("Organization with id %s not found", receiptOrgId));
    }

    Broker broker = brokerService.findById(org.getBrokerId(), accessToken);

    if (!Boolean.TRUE.equals(broker.getFlagDelegate())
      && !org.getOrgFiscalCode().equals(receiptOrgFiscalCode)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_RECEIPT_ORG_MISMATCH, "Org fiscal code doesn't match receipt org fiscal code.");
    }

    return Pair.of(broker, org);
  }

  private ReceiptDTO getExistingReceipt(ReceiptDTO receiptDTO) {
    ReceiptNoPII receiptInDb = receiptNoPIIRepository.getByPaymentReceiptId(receiptDTO.getPaymentReceiptId());
    if (receiptInDb != null) {
      log.info("Receipt with paymentReceiptId[{}] already present in DB with id[{}] and origin[{}]", receiptDTO.getPaymentReceiptId(), receiptInDb.getReceiptId(), receiptInDb.getReceiptOrigin());
      receiptDTO.setReceiptId(receiptInDb.getReceiptId());
      receiptDTO.setNoPII(receiptInDb);
      return receiptDTO;
    }
    return null;
  }

  private static void logReceiptData(String message, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    log.info("{} paymentReceiptId[{}] org/nav/iud[{}/{}/{}]",
      message,
      receiptDTO.getPaymentReceiptId(),
      receiptDTO.getOrgFiscalCode(),
      receiptDTO.getNoticeNumber(),
      receiptDTO.getIud());
  }

  private boolean shouldUpdateExistingReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO, ReceiptDTO existingReceipt) {
    if (StringUtils.isEmpty(receiptDTO.getIud())) {
      log.info("Skipping receipt update: the input is not a manual import");
      return false;
    }

    if (ReceiptOriginType.RECEIPT_FILE.equals(existingReceipt.getNoPII().getReceiptOrigin())) {
      log.info("Updating receipt: the input is a manual import and the stored receipt has technical origin: {}", existingReceipt.getNoPII().getReceiptOrigin());
      return true;
    }

    log.info("Skipping update for manual import: existing receipt origin [{}] is not technical", existingReceipt.getNoPII().getReceiptOrigin());
    return false;
  }

  private void saveReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    ReceiptDTO storedReceipt = receiptPIIRepository.save(receiptDTO);
    receiptDTO.setReceiptId(storedReceipt.getReceiptId());
    receiptDTO.setNoPII(storedReceipt.getNoPII());
    log.debug("Receipt paymentReceiptId[{}] persisted/updated with id[{}]", receiptDTO.getPaymentReceiptId(), storedReceipt.getReceiptId());
  }
}
