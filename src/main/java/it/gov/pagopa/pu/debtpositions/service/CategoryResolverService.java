package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.LEGACY_PAYMENT_METADATA_REGEX;
import static it.gov.pagopa.pu.debtpositions.util.Utilities.taxonomyCodeToTransferCategory;

@Slf4j
@Service
public class CategoryResolverService {

  private final TaxonomyValidatorService taxonomyValidatorService;
  private final DebtPositionTypeRepository debtPositionTypeRepository;

  public CategoryResolverService(DebtPositionTypeRepository debtPositionTypeRepository, TaxonomyValidatorService taxonomyValidatorService) {
    this.taxonomyValidatorService = taxonomyValidatorService;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
  }

  public String resolveCategory(String legacyPaymentMetadata, Long debtPositionTypeId) {
    String taxonomyCode = null;

    if (StringUtils.isNotBlank(legacyPaymentMetadata)) {
      try {
        String extractedCode = extractTaxonomyFromLegacyPaymentMetadata(legacyPaymentMetadata);

        if (taxonomyValidatorService.isTaxonomyCategoryValid(extractedCode)) {
          taxonomyCode = extractedCode;
        } else {
          log.warn("Extracted taxonomy [{}] from legacyPaymentMetadata is not valid. Getting taxonomy from debtPositionTypeId {}.", extractedCode, debtPositionTypeId);
        }
      } catch (Exception e) {
        log.warn("Failed to extract taxonomy from legacyPaymentMetadata [{}], getting taxonomy from debtPositionTypeId {}. Error: {}.",
          legacyPaymentMetadata, debtPositionTypeId, e.getMessage());
      }
    }

    if (taxonomyCode == null) {
      taxonomyCode = getTaxonomyFromRepository(debtPositionTypeId);
    }

    return taxonomyCodeToTransferCategory(taxonomyCode);
  }

  private String extractTaxonomyFromLegacyPaymentMetadata(String legacyPaymentMetadata) {
    Matcher matcher = LEGACY_PAYMENT_METADATA_REGEX.matcher(legacyPaymentMetadata);
    if (!matcher.find()) {
      throw new InvalidValueException(String.format("The legacy payment metadata [%s] is not valid to extract taxonomy code", legacyPaymentMetadata));
    }
    return matcher.group(1);
  }

  private String getTaxonomyFromRepository(Long debtPositionTypeId) {
    return debtPositionTypeRepository.findById(debtPositionTypeId)
      .orElseThrow(() -> new NotFoundException(String.format("The debt position type with id %s is not found", debtPositionTypeId)))
      .getTaxonomyCode();
  }
}
