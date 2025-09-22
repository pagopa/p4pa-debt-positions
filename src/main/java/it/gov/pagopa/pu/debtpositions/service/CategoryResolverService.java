package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.LEGACY_PAYMENT_METADATA_REGEX;
import static it.gov.pagopa.pu.debtpositions.util.Utilities.taxonomyCodeToTransferCategory;

@Service
public class CategoryResolverService {

  private final DebtPositionTypeRepository debtPositionTypeRepository;

  public CategoryResolverService(DebtPositionTypeRepository debtPositionTypeRepository) {
    this.debtPositionTypeRepository = debtPositionTypeRepository;
  }

  public String resolveCategory(String legacyPaymentMetadata, Long debtPositionTypeId){
    String taxonomyCode = Optional.ofNullable(legacyPaymentMetadata)
      .filter(StringUtils::isNotBlank)
      .map(this::extractTaxonomyFromLegacyPaymentMetadata)
      .orElseGet(() -> getTaxonomyFromRepository(debtPositionTypeId));

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
