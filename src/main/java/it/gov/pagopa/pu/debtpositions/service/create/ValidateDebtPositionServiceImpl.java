package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.TaxonomyValidatorService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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

  public ValidateDebtPositionServiceImpl(TaxonomyValidatorService taxonomyValidatorService,
                                         DebtPositionRepository debtPositionRepository,
                                         BalanceService balanceService,
                                         OrganizationService organizationService,
                                         @Value("${features.organization.piva-check}") boolean isOrgPIvaCheckEnabled) {
    this.taxonomyValidatorService = taxonomyValidatorService;
    this.debtPositionRepository = debtPositionRepository;
    this.balanceService = balanceService;
    this.isOrgPIvaCheckEnabled = isOrgPIvaCheckEnabled;
    this.organizationService = organizationService;
  }

  public void validate(DebtPositionDTO debtPositionDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg) {

    validateDebtPositionOrigin(debtPositionDTO);

    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId());

    if (debtPosition != null) {
      throw new ConflictErrorException("[DEBT_POSITION_ALREADY_EXISTS] Duplicate records found: DebtPosition with same iupdOrg " + debtPositionDTO.getIupdOrg() + " conflicts with existing records.");
    }

    if (debtPositionTypeOrg == null ||
      StringUtils.isBlank(debtPositionTypeOrg.getCode())) {
      throw new InvalidValueException("[MISSING_DEBT_POSITION_TYPE_ORG] Debt position type organization is mandatory");
    }

    if (CollectionUtils.isEmpty(debtPositionDTO.getPaymentOptions())) {
      throw new InvalidValueException("[MISSING_PAYMENT_OPTION] Debt position payment options is mandatory");
    }

    Set<Integer> poIndexes = HashSet.newHashSet(debtPositionDTO.getPaymentOptions().size());
    for (PaymentOptionDTO paymentOptionDTO : debtPositionDTO.getPaymentOptions()) {
      if (!poIndexes.add(paymentOptionDTO.getPaymentOptionIndex())) {
        throw new InvalidValueException("[DUPLICATED_PAYMENT_OPTION_INDEX] PaymentOption index duplicated: " + paymentOptionDTO.getPaymentOptionIndex());
      }
      if (CollectionUtils.isEmpty(paymentOptionDTO.getInstallments())) {
        throw new InvalidValueException("[MISSING_INSTALLMENT] At least one installment of the debt position is mandatory");
      }
      for (InstallmentDTO installmentDTO : paymentOptionDTO.getInstallments()) {
        validateInstallment(installmentDTO, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin(), debtPositionDTO.getFlagPuPagoPaPayment());
      }
    }
  }

  private void validateDebtPositionOrigin(DebtPositionDTO debtPositionDTO) {
    DebtPositionOrigin origin = debtPositionDTO.getDebtPositionOrigin();

    if ((origin.equals(DebtPositionOrigin.ORDINARY) || origin.equals(DebtPositionOrigin.ORDINARY_SIL))
      && (debtPositionDTO.getStatus() != DebtPositionStatus.UNPAID && debtPositionDTO.getStatus() != DebtPositionStatus.DRAFT)) {
      throw new InvalidValueException("[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin ORDINARY or ORDINARY_SIL can only be created in UNPAID or DRAFT state");
    }

    if (origin.equals(DebtPositionOrigin.SPONTANEOUS) && debtPositionDTO.getStatus() != DebtPositionStatus.UNPAID) {
      throw new InvalidValueException("[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin SPONTANEOUS can only be created in UNPAID state");
    }

    if ((origin.equals(DebtPositionOrigin.SECONDARY_ORG) || origin.equals(DebtPositionOrigin.RECEIPT_PAGOPA)
      || origin.equals(DebtPositionOrigin.RECEIPT_FILE) || origin.equals(DebtPositionOrigin.REPORTING_PAGOPA))
      && debtPositionDTO.getStatus() != DebtPositionStatus.PAID) {
      throw new InvalidValueException("[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
    }
  }

  public void validateInstallment(InstallmentDTO installmentDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg, DebtPositionOrigin debtPositionOrigin, Boolean flagPuPagoPaPayment) {
    if (StringUtils.isBlank(installmentDTO.getRemittanceInformation())) {
      throw new InvalidValueException("[MISSING_REMITTANCE_INFORMATION] Remittance information is mandatory");
    }

    validateDueDate(debtPositionTypeOrg.isFlagMandatoryDueDate(), installmentDTO, debtPositionOrigin);

    if (installmentDTO.getAmountCents() <= 0) {
      throw new InvalidValueException("[INVALID_CENTS_AMOUNT] The installment amount must be greater than 0");
    }
    if (
        (DebtPositionOrigin.SPONTANEOUS.equals(debtPositionOrigin) ||
         DebtPositionOrigin.SPONTANEOUS_SIL.equals(debtPositionOrigin) ||
         DebtPositionOrigin.ORDINARY_SIL.equals(debtPositionOrigin))
         && debtPositionTypeOrg.getAmountCents() != null
         && !installmentDTO.getAmountCents().equals(debtPositionTypeOrg.getAmountCents())
    ) {
      throw new InvalidValueException("[INVALID_AMOUNT] Amount is not valid for this debt position type org");
    }
    if (StringUtils.isNotBlank(installmentDTO.getBalance()) &&
      BooleanUtils.isNotTrue(balanceService.isValidBalance(installmentDTO.getBalance(), accessToken))) {
      throw new InvalidValueException("[INVALID_BALANCE] Balance is not formally valid");
    }

    if (StringUtils.isNotBlank(installmentDTO.getLegacyPaymentMetadata())
      && !installmentDTO.getLegacyPaymentMetadata().matches("[0129]/\\S{3,138}")) {
      throw new InvalidValueException("[INVALID_LEGACY_PAYMENT_METADATA] Legacy payment metadata is not valid");
    }

    validatePersonData(installmentDTO.getDebtor(), debtPositionTypeOrg);
    validateTransfers(installmentDTO.getTransfers(), accessToken);

    Long totalAmountTransfers = installmentDTO.getTransfers().stream()
      .mapToLong(TransferDTO::getAmountCents).sum();

    if (!installmentDTO.getAmountCents().equals(totalAmountTransfers)) {
      throw new InvalidValueException("[INVALID_INSTALLMENT_AMOUNT_CENTS] The sum of transfers amounts has to be equal to installment amount");
    }

    if (Boolean.FALSE.equals(flagPuPagoPaPayment) && StringUtils.isBlank(installmentDTO.getIuv())) {
      throw new InvalidValueException("[MISSING_IUV] Iuv cannot be empty if flagPuPagoPaPayment is false");
    }
  }

  private void validateDueDate(boolean flagMandatoryDueDate, InstallmentDTO installmentDTO, DebtPositionOrigin debtPositionOrigin) {
    if (flagMandatoryDueDate) {
      if (installmentDTO.getDueDate() == null) {
        throw new InvalidValueException("[MISSING_DUE_DATE] The due date is mandatory");
      }
      if (installmentDTO.getDueDate().isBefore(LocalDate.now())) {
        throw new InvalidValueException("[INVALID_DUE_DATE] The due date cannot be retroactive");
      }
    }

    boolean switchToExpired = flagMandatoryDueDate
        || DebtPositionOrigin.SPONTANEOUS_SIL.equals(debtPositionOrigin)
        || DebtPositionOrigin.SPONTANEOUS.equals(debtPositionOrigin);

    installmentDTO.setSwitchToExpired(switchToExpired);
  }

  private void validatePersonData(PersonDTO personDTO, DebtPositionTypeOrg debtPositionTypeOrgDTO) {
    if (personDTO == null) {
      throw new InvalidValueException("[MISSING_DEBTOR] The debtor is mandatory for installment");
    }
    if (StringUtils.isBlank(personDTO.getFiscalCode())) {
      throw new InvalidValueException("[INVALID_VAT_CODE] Fiscal code is mandatory");
    }
    if (PersonEntityType.F.equals(personDTO.getEntityType())) {
      if (Boolean.FALSE.equals(debtPositionTypeOrgDTO.isFlagAnonymousFiscalCode()) && personDTO.getFiscalCode().equals(ANONIMO)) {
        throw new InvalidValueException("[INVALID_VAT_CODE] The debt position type org does not allow an anonymous unique identification code");
      }
      if (!personDTO.getFiscalCode().equals(ANONIMO) && !isValidFiscalCode(personDTO.getFiscalCode())) {
        throw new InvalidValueException("[INVALID_VAT_CODE] Fiscal code of person is not valid");
      }
    } else {
      if (!isValidFiscalCodeOrPIVA(personDTO.getFiscalCode(), isOrgPIvaCheckEnabled)) {
        throw new InvalidValueException("[INVALID_VAT_CODE] Fiscal code or p. iva of legal person is not valid");
      }
    }
    if (StringUtils.isBlank(personDTO.getFullName())) {
      throw new InvalidValueException("[INVALID_FULLNAME] Beneficiary name is mandatory");
    }
    if (StringUtils.isNotBlank(personDTO.getEmail()) && !Utilities.isValidEmail(personDTO.getEmail())) {
      throw new InvalidValueException("[INVALID_EMAIL] Email is not valid");
    }
  }

  private void validateTransfers(List<TransferDTO> transferDTOList, String accessToken) {
    if (CollectionUtils.isEmpty(transferDTOList)) {
      throw new InvalidValueException("[MISSING_TRANSFER] At least one transfer is mandatory for installment");
    }

    if (transferDTOList.size() > TRANSFER_INDEX_MAX_SIZE) {
      throw new InvalidValueException("[TOO_MANY_TRANSFERS_FOR_INSTALLMENT] At most 5 transfers is allowed for installment");
    }

    Set<Integer> transferIndexes = HashSet.newHashSet(transferDTOList.size());
    transferDTOList.forEach(transferDTO -> {
      Integer index = transferDTO.getTransferIndex();
      if (!transferIndexes.add(index)) {
        throw new InvalidValueException("[DUPLICATED_TRANSFER_INDEX] Transfer index duplicated: " + index);
      }
      if (index < 1 || index > TRANSFER_INDEX_MAX_SIZE) {
        throw new InvalidValueException("[INVALID_TRANSFER_INDEX] Transfer index should be between 1 and " + TRANSFER_INDEX_MAX_SIZE + ", provided: " + index);
      }
      if (transferDTO.getAmountCents() <= 0) {
        throw new InvalidValueException("[INVALID_CENTS_AMOUNT] The amount of transfer with index " + index + " must be greater than 0");
      }
      if (StringUtils.isBlank(transferDTO.getOrgFiscalCode()) ||
        !isValidPIVA(transferDTO.getOrgFiscalCode(), isOrgPIvaCheckEnabled)) {
        throw new InvalidValueException("[INVALID_VAT_CODE] Fiscal code of transfer with index " + index + " is not valid");
      }
      checkIbanOrStamp(transferDTO);

      String orgTypeCode = organizationService.getOrganizationByFiscalCode(transferDTO.getOrgFiscalCode(), accessToken)
        .map(Organization::getOrgTypeCode)
        .orElse(null);
      checkTaxonomyCategory(transferDTO, orgTypeCode);
    });
  }

  private void checkIbanOrStamp(TransferDTO transferDTO) {
    if (StringUtils.isNotBlank(transferDTO.getIban())) {
      if (StringUtils.isNotBlank(transferDTO.getStampType()) ||
        StringUtils.isNotBlank(transferDTO.getStampHashDocument()) ||
        StringUtils.isNotBlank(transferDTO.getStampProvincialResidence())) {
        throw new InvalidValueException("[INVALID_FIELDS] Stamp attributes of transfer with index " + transferDTO.getTransferIndex() + " has to be null when iban is valued");
      }
      if (!isValidIban(transferDTO.getIban())) {
        throw new InvalidValueException("[INVALID_IBAN] Iban of transfer with index " + transferDTO.getTransferIndex() + " is not valid");
      }
      if (StringUtils.isNotBlank(transferDTO.getPostalIban()) && !isValidIban(transferDTO.getPostalIban())) {
        throw new InvalidValueException("[INVALID_POSTAL_IBAN] Postal iban of transfer with index " + transferDTO.getTransferIndex() + " is not valid");
      }
    } else {
      if (StringUtils.isBlank(transferDTO.getStampType()) ||
        StringUtils.isBlank(transferDTO.getStampHashDocument()) ||
        StringUtils.isBlank(transferDTO.getStampProvincialResidence())) {
        throw new InvalidValueException("[INVALID_FIELDS] Stamp attributes of transfer with index " + transferDTO.getTransferIndex() + " has to be all valued when iban is null");
      }
    }
  }

  private void checkTaxonomyCategory(TransferDTO transferDTO, String orgTypeCode) {
    if (StringUtils.isBlank(transferDTO.getCategory())) {
      throw new InvalidValueException("[MISSING_TAXONOMY_CATEGORY] Category of transfer with index " + transferDTO.getTransferIndex() + " is mandatory");
    } else {
      String taxonomyCategory = transferDTO.getCategory();
      boolean isTaxonomyCategoryValid = taxonomyValidatorService.isTaxonomyCategoryValid(taxonomyCategory, orgTypeCode);
      if(!isTaxonomyCategoryValid) {
        throw new InvalidValueException("[INVALID_TAXONOMY_CATEGORY] Taxonomy category of transfer with index " + transferDTO.getTransferIndex() + " is not valid");
      }
      transferDTO.setCategory(taxonomyCategory);
    }
  }
}
