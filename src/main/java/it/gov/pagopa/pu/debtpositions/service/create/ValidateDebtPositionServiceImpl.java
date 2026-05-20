package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.TaxonomyValidatorService;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.*;

@Service
public class ValidateDebtPositionServiceImpl implements ValidateDebtPositionService {

  public static final int TRANSFER_INDEX_MAX_SIZE = 5;
  public static final String ANONIMO = "ANONIMO";
  private final TaxonomyValidatorService taxonomyValidatorService;
  private final DebtPositionRepository debtPositionRepository;
  private final BalanceService balanceService;
  private final boolean isOrgPIvaCheckEnabled;
  private final OrganizationService organizationService;
  private final BrokerService brokerService;
  private final String defaultSpontaneousPrefix;
  private final String categoryDefaultPrefix;
  private final String categorySuffix;
  private final List<String> categoryAllowedPrefixes;

  public ValidateDebtPositionServiceImpl(TaxonomyValidatorService taxonomyValidatorService,
                                         DebtPositionRepository debtPositionRepository,
                                         BalanceService balanceService,
                                         OrganizationService organizationService,
                                         BrokerService brokerService,
                                         @Value("${features.organization.piva-check}") boolean isOrgPIvaCheckEnabled,
                                         @Value("${category.prefix}") String categoryDefaultPrefix,
                                         @Value("${category.prefix-spontaneous}") String defaultSpontaneousPrefix,
                                         @Value("${category.suffix}") String categorySuffix,
                                         @Value("${category.allowed-prefixes}") List<String> categoryAllowedPrefixes) {
    this.taxonomyValidatorService = taxonomyValidatorService;
    this.debtPositionRepository = debtPositionRepository;
    this.balanceService = balanceService;
    this.isOrgPIvaCheckEnabled = isOrgPIvaCheckEnabled;
    this.organizationService = organizationService;
    this.brokerService = brokerService;
    this.defaultSpontaneousPrefix = defaultSpontaneousPrefix;
    this.categoryDefaultPrefix = categoryDefaultPrefix;
    this.categorySuffix = categorySuffix;
    this.categoryAllowedPrefixes = categoryAllowedPrefixes;
  }

  public void validate(DebtPositionDTO debtPositionDTO, Organization org, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg) {

    validateDebtPositionOrigin(debtPositionDTO);

    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId());

    if (debtPosition != null) {
      throw new ConflictErrorException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_ALREADY_EXISTS, "Duplicate records found: DebtPosition with same iupdOrg " + debtPositionDTO.getIupdOrg() + " conflicts with existing records.");
    }

    if (debtPositionTypeOrg == null ||
      StringUtils.isBlank(debtPositionTypeOrg.getCode())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_DEBT_POSITION_TYPE_ORG, "Debt position type organization is mandatory");
    }

    if (CollectionUtils.isEmpty(debtPositionDTO.getPaymentOptions())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_PAYMENT_OPTION, "Debt position payment options is mandatory");
    }

    validateDebtorConsistency(debtPositionDTO);

    Set<Integer> poIndexes = HashSet.newHashSet(debtPositionDTO.getPaymentOptions().size());
    for (PaymentOptionDTO paymentOptionDTO : debtPositionDTO.getPaymentOptions()) {
      if (!poIndexes.add(paymentOptionDTO.getPaymentOptionIndex())) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_DUPLICATED_PAYMENT_OPTION_INDEX, "PaymentOption index duplicated: " + paymentOptionDTO.getPaymentOptionIndex());
      }
      if (CollectionUtils.isEmpty(paymentOptionDTO.getInstallments())) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_INSTALLMENT, "At least one installment of the debt position is mandatory");
      }

      for (InstallmentDTO installmentDTO : paymentOptionDTO.getInstallments()) {
        validateInstallment(installmentDTO, org, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin(), debtPositionDTO.getFlagPuPagoPaPayment());
      }
    }
  }

  public void validateDebtorConsistency(DebtPositionDTO debtPositionDTO) {
    PersonDTO dpReferenceDebtor = null;
    boolean isMultiDebtorFalse = Boolean.FALSE.equals(debtPositionDTO.getMultiDebtor());

    for (PaymentOptionDTO paymentOptionDTO : debtPositionDTO.getPaymentOptions()) {
      PersonDTO poReferenceDebtor = null;

      for (InstallmentDTO installmentDTO : paymentOptionDTO.getInstallments()) {
        PersonDTO currentDebtor = installmentDTO.getDebtor();
        if (poReferenceDebtor == null) {
          poReferenceDebtor = currentDebtor;
        } else if (isDifferentDebtor(poReferenceDebtor, currentDebtor)) {
          throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_DIFFERENT_DEBTORS_IN_SAME_PO, "All installments in a PaymentOption must have the same debtor. PO Index: " + paymentOptionDTO.getPaymentOptionIndex());
        }
        if (isMultiDebtorFalse) {
          if (dpReferenceDebtor == null) {
            dpReferenceDebtor = currentDebtor;
          } else if (isDifferentDebtor(dpReferenceDebtor, currentDebtor)) {
            throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MULTIDEBTOR_DISABLED, "Different debtors found but multiDebtor flag is False for this Debt Position");
          }
        }
      }
    }
  }

  private boolean isDifferentDebtor(PersonDTO d1, PersonDTO d2) {
    if (d1 == null || d2 == null) return d1 != d2;
    return !Objects.equals(d1.getFiscalCode(), d2.getFiscalCode());
  }

  private void validateDebtPositionOrigin(DebtPositionDTO debtPositionDTO) {
    DebtPositionOrigin origin = debtPositionDTO.getDebtPositionOrigin();

    if (InstallmentUtils.ORDINARY_ORG_DEBT_POSITION_ORIGINS.contains(origin)
      && debtPositionDTO.getStatus() != DebtPositionStatus.UNPAID && debtPositionDTO.getStatus() != DebtPositionStatus.DRAFT) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_DEBT_POSITION_STATUS, "A Debt Position with origin ORDINARY or ORDINARY_SIL can only be created in UNPAID or DRAFT state");
    }

    if (InstallmentUtils.ORDINARY_CITIZEN_DEBT_POSITION_ORIGINS_NO_MIXED.contains(origin)
      && debtPositionDTO.getStatus() != DebtPositionStatus.UNPAID) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_DEBT_POSITION_STATUS, "A Debt Position with origin SPONTANEOUS, SPONTANEOUS_SIL or SPONTANEOUS_PSP can only be created in UNPAID state");
    }

    if (InstallmentUtils.TECHNICAL_DEBT_POSITION_ORIGINS.contains(origin)
      && debtPositionDTO.getStatus() != DebtPositionStatus.PAID) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_DEBT_POSITION_STATUS, "A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGOPA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
    }
  }

  public void validateInstallment(InstallmentDTO installmentDTO, Organization org, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg, DebtPositionOrigin debtPositionOrigin, Boolean flagPuPagoPaPayment) {
    if (StringUtils.isBlank(installmentDTO.getRemittanceInformation())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_REMITTANCE_INFORMATION, "Remittance information is mandatory");
    }

    validateDueDate(debtPositionTypeOrg.isFlagMandatoryDueDate(), installmentDTO, debtPositionOrigin);

    if (installmentDTO.getAmountCents() <= 0) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_CENTS_AMOUNT, "The installment amount must be greater than 0");
    }
    if (
        InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS.contains(debtPositionOrigin)
         && debtPositionTypeOrg.getAmountCents() != null
         && !installmentDTO.getAmountCents().equals(debtPositionTypeOrg.getAmountCents())
    ) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_AMOUNT, "Amount is not valid for this debt position type org");
    }
    if (StringUtils.isNotBlank(installmentDTO.getBalance()) &&
      BooleanUtils.isNotTrue(balanceService.isValidBalance(installmentDTO.getBalance(), installmentDTO.getAmountCents(), accessToken))) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_BALANCE, "Balance is not formally valid");
    }

    if (StringUtils.isNotBlank(installmentDTO.getLegacyPaymentMetadata())
      && !installmentDTO.getLegacyPaymentMetadata().matches("[0126789]/\\S{3,138}")) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_LEGACY_PAYMENT_METADATA, "Legacy payment metadata is not valid");
    }

    validatePersonData(installmentDTO.getDebtor(), debtPositionTypeOrg);
    validateTransfers(installmentDTO.getTransfers(), org, InstallmentUtils.SPONTANEOUS_DEBT_POSITION_ORIGINS.contains(debtPositionOrigin), accessToken);

    Long totalAmountTransfers = installmentDTO.getTransfers().stream()
      .mapToLong(TransferDTO::getAmountCents).sum();

    if (!installmentDTO.getAmountCents().equals(totalAmountTransfers)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_INSTALLMENT_AMOUNT_CENTS, "The sum of transfers amounts has to be equal to installment amount");
    }

    if (Boolean.FALSE.equals(flagPuPagoPaPayment) && StringUtils.isBlank(installmentDTO.getIuv())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_IUV, "Iuv cannot be empty if flagPuPagoPaPayment is false");
    }
  }

  private void validateDueDate(boolean flagMandatoryDueDate, InstallmentDTO installmentDTO, DebtPositionOrigin debtPositionOrigin) {
    LocalDate dueDate = installmentDTO.getDueDate();
    if (flagMandatoryDueDate && dueDate == null) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_DUE_DATE, "The due date is mandatory");
    }
    if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_DUE_DATE, "The due date cannot be retroactive");
    }

    boolean switchToExpired = flagMandatoryDueDate
        || InstallmentUtils.ORDINARY_CITIZEN_DEBT_POSITION_ORIGINS_NO_MIXED.contains(debtPositionOrigin);

    installmentDTO.setSwitchToExpired(switchToExpired);
  }

  private void validatePersonData(PersonDTO personDTO, DebtPositionTypeOrg debtPositionTypeOrgDTO) {
    if (personDTO == null) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_DEBTOR, "The debtor is mandatory for installment");
    }
    if (StringUtils.isBlank(personDTO.getFiscalCode())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_VAT_CODE, "Fiscal code is mandatory");
    }
    if (PersonEntityType.F.equals(personDTO.getEntityType())) {
      if (Boolean.FALSE.equals(debtPositionTypeOrgDTO.isFlagAnonymousFiscalCode()) && personDTO.getFiscalCode().equals(ANONIMO)) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_VAT_CODE, "The debt position type org does not allow an anonymous unique identification code");
      }
      if (!personDTO.getFiscalCode().equals(ANONIMO) && !isValidFiscalCode(personDTO.getFiscalCode())) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_VAT_CODE, "Fiscal code of person is not valid");
      }
    } else {
      if (!isValidFiscalCodeOrPIVA(personDTO.getFiscalCode(), isOrgPIvaCheckEnabled)) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_VAT_CODE, "Fiscal code or p. iva of legal person is not valid");
      }
    }
    if (StringUtils.isBlank(personDTO.getFullName())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_FULLNAME, "Beneficiary name is mandatory");
    }
    if (StringUtils.isNotBlank(personDTO.getEmail()) && !Utilities.isValidEmail(personDTO.getEmail())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_EMAIL, "Email is not valid");
    }
  }

  private void validateTransfers(List<TransferDTO> transferDTOList, Organization org, boolean isSpontaneous, String accessToken) {
    if (CollectionUtils.isEmpty(transferDTOList)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_TRANSFER, "At least one transfer is mandatory for installment");
    }

    if (transferDTOList.size() > TRANSFER_INDEX_MAX_SIZE) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_TOO_MANY_TRANSFERS_FOR_INSTALLMENT, "At most 5 transfers is allowed for installment");
    }

    Set<Integer> transferIndexes = HashSet.newHashSet(transferDTOList.size());
    transferDTOList.forEach(transferDTO -> {
      Integer index = transferDTO.getTransferIndex();
      if (!transferIndexes.add(index)) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_DUPLICATED_TRANSFER_INDEX, "Transfer index duplicated: " + index);
      }
      if (index < 1 || index > TRANSFER_INDEX_MAX_SIZE) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_TRANSFER_INDEX, "Transfer index should be between 1 and " + TRANSFER_INDEX_MAX_SIZE + ", provided: " + index);
      }
      if (StringUtils.isBlank(transferDTO.getOrgFiscalCode()) ||
        !isValidPIVA(transferDTO.getOrgFiscalCode(), isOrgPIvaCheckEnabled)) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_VAT_CODE, "Fiscal code of transfer with index " + index + " is not valid");
      }
      checkIbanOrStamp(transferDTO);

      if(transferDTO.getFlagOwner() == null && org.getOrgFiscalCode().equals(transferDTO.getOrgFiscalCode())) {
        transferDTO.setFlagOwner(Boolean.TRUE);
      }
      Broker broker = brokerService.findById(org.getBrokerId(), accessToken);
      checkTransferAmount(broker, transferDTO);

      if(!Boolean.TRUE.equals(broker.getFlagDelegate())) {
        String orgTypeCode = organizationService.getOrganizationByFiscalCode(transferDTO.getOrgFiscalCode(), accessToken)
          .map(Organization::getOrgTypeCode)
          .orElse(null);
        checkTaxonomyCategory(transferDTO, orgTypeCode, isSpontaneous);
      }
    });
  }

  private void checkTransferAmount(Broker broker, TransferDTO transferDTO){
    if (Boolean.TRUE.equals(broker.getFlagDelegate()) && Boolean.TRUE.equals(transferDTO.getFlagOwner())) {
      if (transferDTO.getAmountCents() < 0) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_CENTS_AMOUNT, "The amount of transfer with index " + transferDTO.getTransferIndex() + " must be greater than or equal to 0");
      }
    } else {
      if (transferDTO.getAmountCents() <= 0) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_CENTS_AMOUNT, "The amount of transfer with index " + transferDTO.getTransferIndex() + " must be greater than 0");
      }
    }
  }

  private void checkIbanOrStamp(TransferDTO transferDTO) {
    if (StringUtils.isNotBlank(transferDTO.getIban())) {
      if (StringUtils.isNotBlank(transferDTO.getStampType()) ||
        StringUtils.isNotBlank(transferDTO.getStampHashDocument()) ||
        StringUtils.isNotBlank(transferDTO.getStampProvincialResidence())) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_FIELDS, "Stamp attributes of transfer with index " + transferDTO.getTransferIndex() + " has to be null when iban is valued");
      }
      if (!isValidIban(transferDTO.getIban())) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IBAN, "Iban of transfer with index " + transferDTO.getTransferIndex() + " is not valid");
      }

      validateTransferPostalIban(transferDTO);
    } else {
      if (StringUtils.isBlank(transferDTO.getStampType()) ||
        StringUtils.isBlank(transferDTO.getStampHashDocument()) ||
        StringUtils.isBlank(transferDTO.getStampProvincialResidence())) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_FIELDS, "Stamp attributes of transfer with index " + transferDTO.getTransferIndex() + " has to be all valued when iban is null");
      }
    }
  }

  private void validateTransferPostalIban(TransferDTO transferDTO) {
    String postalIban = transferDTO.getPostalIban();
    // Postal IBAN is optional, but if provided, it must not be blank
    if (postalIban != null && !Utilities.isValidIban(postalIban)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_POSTAL_IBAN, "Postal iban of transfer with index " + transferDTO.getTransferIndex() + " is not valid");
    }
  }

  private void checkTaxonomyCategory(TransferDTO transferDTO, String orgTypeCode, boolean isSpontaneous) {
    if (StringUtils.isBlank(transferDTO.getCategory())) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_TAXONOMY_CATEGORY, "Category of transfer with index " + transferDTO.getTransferIndex() + " is mandatory");
    } else {
      String taxonomyCategory = transferDTO.getCategory();
      if(!taxonomyValidatorService.isTaxonomyCategoryValid(taxonomyCategory, orgTypeCode)) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_TAXONOMY_CATEGORY, "Taxonomy category of transfer with index " + transferDTO.getTransferIndex() + " is not valid");
      }
      transferDTO.setCategory(formatCategoryTransferFromTaxonomyCode(taxonomyCategory, categoryAllowedPrefixes, categoryDefaultPrefix, categorySuffix, defaultSpontaneousPrefix, isSpontaneous));
    }
  }
}
