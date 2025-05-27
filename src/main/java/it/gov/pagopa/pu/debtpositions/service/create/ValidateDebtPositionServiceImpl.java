package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.isValidIban;
import static it.gov.pagopa.pu.debtpositions.util.Utilities.isValidPIVA;

@Service
public class ValidateDebtPositionServiceImpl implements ValidateDebtPositionService {

  public static final int TRANSFER_INDEX_MAX_SIZE = 5;
  private final TaxonomyService taxonomyService;
  private final DebtPositionRepository debtPositionRepository;
  private final BalanceService balanceService;
  private final boolean isCheckEnabled;

  public ValidateDebtPositionServiceImpl(TaxonomyService taxonomyService,
                                         DebtPositionRepository debtPositionRepository,
                                         BalanceService balanceService,
                                         @Value("${features.organization.piva-check}") boolean isCheckEnabled) {
    this.taxonomyService = taxonomyService;
    this.debtPositionRepository = debtPositionRepository;
    this.balanceService = balanceService;
    this.isCheckEnabled = isCheckEnabled;
  }

  public void validate(DebtPositionDTO debtPositionDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg) {

    validateDebtPositionOrigin(debtPositionDTO);

    DebtPosition debtPosition = debtPositionRepository.findByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId());

    if (debtPosition != null) {
      throw new ConflictErrorException("Duplicate records found: DebtPosition with same iupdOrg " + debtPositionDTO.getIupdOrg() + " conflicts with existing records.");
    }

    if (debtPositionTypeOrg == null ||
      StringUtils.isBlank(debtPositionTypeOrg.getCode())) {
      throw new InvalidValueException("Debt position type organization is mandatory");
    }

    if (CollectionUtils.isEmpty(debtPositionDTO.getPaymentOptions())) {
      throw new InvalidValueException("Debt position payment options is mandatory");
    }

    Set<Integer> poIndexes = HashSet.newHashSet(debtPositionDTO.getPaymentOptions().size());
    for (PaymentOptionDTO paymentOptionDTO : debtPositionDTO.getPaymentOptions()) {
      if(!poIndexes.add(paymentOptionDTO.getPaymentOptionIndex())){
        throw new InvalidValueException("PaymentOption index duplicated: " + paymentOptionDTO.getPaymentOptionIndex());
      }
      if (CollectionUtils.isEmpty(paymentOptionDTO.getInstallments())) {
        throw new InvalidValueException("At least one installment of the debt position is mandatory");
      }
      for (InstallmentDTO installmentDTO : paymentOptionDTO.getInstallments()) {
        validateInstallment(installmentDTO, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin());
      }
    }
  }

  private void validateDebtPositionOrigin(DebtPositionDTO debtPositionDTO) {
    DebtPositionOrigin origin = debtPositionDTO.getDebtPositionOrigin();

    if ((origin.equals(DebtPositionOrigin.ORDINARY) || origin.equals(DebtPositionOrigin.ORDINARY_SIL))
      && (debtPositionDTO.getStatus() != DebtPositionStatus.UNPAID && debtPositionDTO.getStatus() != DebtPositionStatus.DRAFT)) {
      throw new InvalidValueException("A Debt Position with origin ORDINARY or ORDINARY_SIL can only be created in UNPAID or DRAFT state");
    }

    if (origin.equals(DebtPositionOrigin.SPONTANEOUS) && debtPositionDTO.getStatus() != DebtPositionStatus.UNPAID) {
      throw new InvalidValueException("A Debt Position with origin SPONTANEOUS can only be created in UNPAID state");
    }

    if ((origin.equals(DebtPositionOrigin.SECONDARY_ORG) || origin.equals(DebtPositionOrigin.RECEIPT_PAGOPA)
      || origin.equals(DebtPositionOrigin.RECEIPT_FILE) || origin.equals(DebtPositionOrigin.REPORTING_PAGOPA))
      && debtPositionDTO.getStatus() != DebtPositionStatus.PAID) {
      throw new InvalidValueException("A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
    }
  }

  public void validateInstallment(InstallmentDTO installmentDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg, DebtPositionOrigin debtPositionOrigin) {
    if (StringUtils.isBlank(installmentDTO.getRemittanceInformation())) {
      throw new InvalidValueException("Remittance information is mandatory");
    }
    if (installmentDTO.getDueDate() != null && installmentDTO.getDueDate().isBefore(LocalDate.now())) {
      throw new InvalidValueException("The due date cannot be retroactive");
    }
    if (debtPositionTypeOrg.isFlagMandatoryDueDate() && installmentDTO.getDueDate() == null) {
      throw new InvalidValueException("The due date is mandatory");
    }
    if (installmentDTO.getAmountCents() < 0) {
      throw new InvalidValueException("Amount is not valid");
    }
    if (DebtPositionOrigin.SPONTANEOUS.equals(debtPositionOrigin) &&
      debtPositionTypeOrg.getAmountCents() != null && !installmentDTO.getAmountCents().equals(debtPositionTypeOrg.getAmountCents())) {
      throw new InvalidValueException("Amount is not valid for this debt position type org");
    }
    if(StringUtils.isNotBlank(installmentDTO.getBalance()) &&
      BooleanUtils.isNotTrue(balanceService.isValidBalance(installmentDTO.getBalance(), accessToken))){
        throw new InvalidValueException("Balance is not formally valid");
    }

    validatePersonData(installmentDTO.getDebtor(), debtPositionTypeOrg);
    validateTransfers(installmentDTO.getTransfers(), accessToken);
  }

  private void validatePersonData(PersonDTO personDTO, DebtPositionTypeOrg debtPositionTypeOrgDTO) {
    if (personDTO == null) {
      throw new InvalidValueException("The debtor is mandatory for installment");
    }
    if (StringUtils.isBlank(personDTO.getFiscalCode())) {
      throw new InvalidValueException("Fiscal code is mandatory");
    }
    if (Boolean.FALSE.equals(debtPositionTypeOrgDTO.isFlagAnonymousFiscalCode()) && personDTO.getFiscalCode().equals("ANONIMO")) {
      throw new InvalidValueException("This organization installment type or installment does not allow an anonymous unique identification code");
    }
    if (StringUtils.isBlank(personDTO.getFullName())) {
      throw new InvalidValueException("Beneficiary name is mandatory");
    }
    if (StringUtils.isNotBlank(personDTO.getEmail()) && !Utilities.isValidEmail(personDTO.getEmail())) {
      throw new InvalidValueException("Email is not valid");
    }
  }

  private void validateTransfers(List<TransferDTO> transferDTOList, String accessToken) {
    if (CollectionUtils.isEmpty(transferDTOList)) {
      throw new InvalidValueException("At least one transfer is mandatory for installment");
    }

    if(transferDTOList.size() > TRANSFER_INDEX_MAX_SIZE){
      throw new InvalidValueException("At most 5 transfers is allowed for installment");
    }

    Set<Integer> transferIndexes = HashSet.newHashSet(transferDTOList.size());
    transferDTOList.forEach(transferDTO -> {
      Integer index = transferDTO.getTransferIndex();
      if(!transferIndexes.add(index)){
        throw new InvalidValueException("Transfer index duplicated: " + index);
      }
      if(index < 1 || index > TRANSFER_INDEX_MAX_SIZE){
        throw new InvalidValueException("Transfer index should be between 1 and " + TRANSFER_INDEX_MAX_SIZE + ", provided: " + index);
      }
      if(transferDTO.getAmountCents() <= 0) {
        throw new InvalidValueException("The amount of transfer with index " + index + " must be greater than 0");
      }
      if (StringUtils.isBlank(transferDTO.getOrgFiscalCode()) ||
        !isValidPIVA(transferDTO.getOrgFiscalCode(), isCheckEnabled)) {
        throw new InvalidValueException("Fiscal code of transfer with index " + index + " is not valid");
      }
      if (!isValidIban(transferDTO.getIban())) {
        throw new InvalidValueException("Iban of transfer with index " + index + " is not valid");
      }
      checkTaxonomyCategory(transferDTO, accessToken);
    });
  }

  private void checkTaxonomyCategory(TransferDTO transferDTO, String accessToken) {
    if (StringUtils.isBlank(transferDTO.getCategory())) {
      throw new InvalidValueException("Category of transfer with index " + transferDTO.getTransferIndex() + " is mandatory");
    }
    // TODO fix with task P4ADEV-2379
//    else {
//      Optional<Taxonomy> taxonomy = taxonomyService.getTaxonomyByTaxonomyCode(transferDTO.getCategory(), accessToken);
//      if (taxonomy.isEmpty()) {
//        throw new InvalidValueException("The category code " + transferDTO.getCategory() + " does not exist in the archive");
//      }
//    }
  }
}
