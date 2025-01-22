package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.connector.organization.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.isValidIban;
import static it.gov.pagopa.pu.debtpositions.util.Utilities.isValidPIVA;

@Service
public class ValidateDebtPositionServiceImpl implements ValidateDebtPositionService {

  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final TaxonomyService taxonomyService;

  public ValidateDebtPositionServiceImpl(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, TaxonomyService taxonomyService) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.taxonomyService = taxonomyService;
  }

  public void validate(DebtPositionDTO debtPositionDTO, String accessToken) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId()).orElse(null);
    if (debtPositionTypeOrg == null ||
      debtPositionTypeOrg.getCode() == null ||
      StringUtils.isBlank(debtPositionTypeOrg.getCode())) {
      throw new InvalidValueException("Debt position type organization is mandatory");
    }

    if (CollectionUtils.isEmpty(debtPositionDTO.getPaymentOptions())) {
      throw new InvalidValueException("Debt position payment options is mandatory");
    }

    for (PaymentOptionDTO paymentOptionDTO : debtPositionDTO.getPaymentOptions()) {
      if (CollectionUtils.isEmpty(paymentOptionDTO.getInstallments())) {
        throw new InvalidValueException("At least one installment of the debt position is mandatory");
      }
      for (InstallmentDTO installmentDTO : paymentOptionDTO.getInstallments()) {
        validateInstallment(installmentDTO, debtPositionTypeOrg);
        validatePersonData(installmentDTO.getDebtor(), debtPositionTypeOrg);
        validateTransfers(installmentDTO.getTransfers(), accessToken);
      }
    }
  }

  private void validateInstallment(InstallmentDTO installmentDTO, DebtPositionTypeOrg debtPositionTypeOrgDTO) {
    if (StringUtils.isBlank(installmentDTO.getRemittanceInformation())) {
      throw new InvalidValueException("Remittance information is mandatory");
    }
    if (installmentDTO.getDueDate() != null && installmentDTO.getDueDate().isBefore(OffsetDateTime.now())) {
      throw new InvalidValueException("The due date cannot be retroactive");
    }
    if (debtPositionTypeOrgDTO.isFlagMandatoryDueDate() && installmentDTO.getDueDate() == null) {
      throw new InvalidValueException("The due date is mandatory");
    }
    if (installmentDTO.getAmountCents() < 0) {
      throw new InvalidValueException("Amount is not valid");
    }
    if (debtPositionTypeOrgDTO.getAmountCents() != null && !installmentDTO.getAmountCents().equals(debtPositionTypeOrgDTO.getAmountCents())) {
      throw new InvalidValueException("Amount is not valid for this debt position type org");
    }
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
    if (StringUtils.isBlank(personDTO.getEmail()) || !Utilities.isValidEmail(personDTO.getEmail())) {
      throw new InvalidValueException("Email is not valid");
    }
  }

  private void validateTransfers(List<TransferDTO> transferDTOList, String accessToken) {
    if (CollectionUtils.isEmpty(transferDTOList)) {
      throw new InvalidValueException("At least one transfer is mandatory for installment");
    }

    if (transferDTOList.size() > 1) {
      TransferDTO transferSecondaryBeneficiary = transferDTOList.stream()
        .filter(transfer -> (transfer.getTransferIndex() == 2)).findAny()
        .orElseThrow(() -> new InvalidValueException("Mismatch with transfers list"));

      if (StringUtils.isBlank(transferSecondaryBeneficiary.getOrgFiscalCode()) ||
        !isValidPIVA(transferSecondaryBeneficiary.getOrgFiscalCode())) {
        throw new InvalidValueException("Fiscal code of secondary beneficiary is not valid");
      }
      if (!isValidIban(transferSecondaryBeneficiary.getIban())) {
        throw new InvalidValueException("Iban of secondary beneficiary is not valid");
      }
      checkTaxonomyCategory(transferSecondaryBeneficiary.getCategory(), accessToken);

      if (transferSecondaryBeneficiary.getAmountCents() < 0) {
        throw new InvalidValueException("The amount of secondary beneficiary is not valid");
      }
    }
  }

  private void checkTaxonomyCategory(String category, String accessToken) {
    if (StringUtils.isBlank(category)) {
      throw new InvalidValueException("Category of secondary beneficiary is mandatory");
    } else {
      String categoryCode = StringUtils.substringBeforeLast(category, "/") + "/";
      Optional<Taxonomy> taxonomy = taxonomyService.getTaxonomyByTaxonomyCode(categoryCode, accessToken);
      if (taxonomy.isEmpty()) {
        throw new InvalidValueException("The category code does not exist in the archive");
      }
    }
  }
}
