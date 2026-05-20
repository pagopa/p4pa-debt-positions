package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.LEGACY_PAYMENT_METADATA_REGEX;
import static it.gov.pagopa.pu.debtpositions.util.Utilities.formatCategoryTransferFromTaxonomyCode;

@Slf4j
@Service
public class CategoryResolverService {

  private final TaxonomyValidatorService taxonomyValidatorService;
  private final DebtPositionTypeRepository debtPositionTypeRepository;
  private final String categoryDefaultSpontaneousPrefix;
  private final String categoryDefaultPrefix;
  private final String categorySuffix;
  private final List<String> categoryAllowedPrefixes;

  public CategoryResolverService(DebtPositionTypeRepository debtPositionTypeRepository,
                                 TaxonomyValidatorService taxonomyValidatorService,
                                 @Value("${category.prefix-spontaneous}") String categoryDefaultSpontaneousPrefix,
                                 @Value("${category.prefix}") String categoryDefaultPrefix,
                                 @Value("${category.suffix}") String categorySuffix,
                                 @Value("${category.allowed-prefixes}") List<String> categoryAllowedPrefixes) {
    this.taxonomyValidatorService = taxonomyValidatorService;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
    this.categoryDefaultSpontaneousPrefix = categoryDefaultSpontaneousPrefix;
    this.categoryDefaultPrefix = categoryDefaultPrefix;
    this.categorySuffix = categorySuffix;
    this.categoryAllowedPrefixes = categoryAllowedPrefixes;
  }

  public String resolveCategory(String legacyPaymentMetadata, Long debtPositionTypeId, String orgTypeCode, boolean isSpontaneous) {
    String taxonomyCode = null;

    if (StringUtils.isNotBlank(legacyPaymentMetadata)) {
      try {
        String extractedCode = extractTaxonomyFromLegacyPaymentMetadata(legacyPaymentMetadata);

        if (taxonomyValidatorService.isTaxonomyCodeValid(extractedCode, orgTypeCode)) {
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

    return formatCategoryTransferFromTaxonomyCode(taxonomyCode, categoryAllowedPrefixes, categoryDefaultPrefix, categorySuffix, categoryDefaultSpontaneousPrefix, isSpontaneous);
  }

  private String extractTaxonomyFromLegacyPaymentMetadata(String legacyPaymentMetadata) {
    Matcher matcher = LEGACY_PAYMENT_METADATA_REGEX.matcher(legacyPaymentMetadata);
    if (!matcher.find()) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_LEGACY_PAYMENT_METADATA, String.format("The legacy payment metadata [%s] is not valid to extract taxonomy code", legacyPaymentMetadata));
    }
    return matcher.group(1);
  }

  private String getTaxonomyFromRepository(Long debtPositionTypeId) {
    return debtPositionTypeRepository.findById(debtPositionTypeId)
      .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_NOT_FOUND, String.format("The debt position type with id %s is not found", debtPositionTypeId)))
      .getTaxonomyCode();
  }
}
