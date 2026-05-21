package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class CategoryUtils {

  private CategoryUtils() {
  }

  public static final Pattern LEGACY_PAYMENT_METADATA_REGEX_PATTERN = Pattern.compile("^([6-9]/[^/]+/).*$");
  public static final String LEGACY_PAYMENT_METADATA_REGEX = "[0126789]/\\S{3,138}";
  public static final String CATEGORY_PREFIX = "9/";
  public static final String CATEGORY_PREFIX_SPONTANEOUS = "6/";
  public static final String CATEGORY_SUFFIX = "/";
  public static final List<String> CATEGORY_ALLOWED_PREFIXES = List.of("6/","7/","8/","9/");

  public static String getTaxonomyCodeFromCategory(String category) {
    Optional<String> prefix = CATEGORY_ALLOWED_PREFIXES.stream().filter(category::startsWith).findAny();
    boolean isTaxonomyCodeFormat = prefix.isPresent() && category.endsWith(CATEGORY_SUFFIX);
    return isTaxonomyCodeFormat ?
      category.replace(prefix.get(), "").replace(CATEGORY_SUFFIX, "")
      : category;
  }

  public static String formatCategoryTransferFromTaxonomyCode(String taxonomyCode, DebtPositionOrigin debtPositionOrigin){
    String cleaned = taxonomyCode;
    Optional<String> prefix = CategoryUtils.CATEGORY_ALLOWED_PREFIXES.stream().filter(cleaned::startsWith).findAny();
    if (prefix.isPresent()) {
      cleaned = cleaned.substring(prefix.get().length());
    }
    if (cleaned.endsWith(CATEGORY_SUFFIX)) {
      cleaned = cleaned.substring(0, cleaned.length() - CATEGORY_SUFFIX.length());
    }

    if(prefix.isPresent()){
      return prefix.get() + cleaned + CATEGORY_SUFFIX;
    }
    return (InstallmentUtils.ORDINARY_CITIZEN_DEBT_POSITION_ORIGINS.contains(debtPositionOrigin)? CATEGORY_PREFIX_SPONTANEOUS : CATEGORY_PREFIX) + cleaned + CATEGORY_SUFFIX;
  }
}
