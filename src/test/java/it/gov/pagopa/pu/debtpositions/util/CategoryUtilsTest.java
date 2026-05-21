package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryUtilsTest {

  @ParameterizedTest
  @CsvSource({
    "'9/12345678/', '12345678'",
    "'12345678', '12345678'",
    "'9/12345678', '9/12345678'",
    "'12345678/', '12345678/'",
    "'9/12345678/', '12345678'",
    "'12345678', '12345678'",
    "'9//', ''",
    "'', ''",
    "'6/12345678/', '12345678'",
    "'6/12345678', '6/12345678'",
    "'6/12345678/', '12345678'",
    "'6//', ''",
  })
  void testGetTaxonomyCodeFromCategory(String input, String expected) {
    String result = CategoryUtils.getTaxonomyCodeFromCategory(input);
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @MethodSource("provideTaxonomyCodeFormattingCases")
  void testFormatCategoryTransferFromTaxonomyCode(String input, boolean isSpontaneous, String expected) {
    String result = CategoryUtils.formatCategoryTransferFromTaxonomyCode(input, isSpontaneous?DebtPositionOrigin.SPONTANEOUS:DebtPositionOrigin.ORDINARY);
    assertEquals(expected, result);
  }

  private static Stream<Arguments> provideTaxonomyCodeFormattingCases() {
    return Stream.of(
      Arguments.of("12345678", false, "9/12345678/"),
      Arguments.of("9/12345678/", false, "9/12345678/"),
      Arguments.of("9/12345678", false, "9/12345678/"),
      Arguments.of("12345678/", false, "9/12345678/"),
      Arguments.of("12345678", false, "9/12345678/"),
      Arguments.of("9/12345678/", false, "9/12345678/"),
      Arguments.of("", false, "9//"),
      Arguments.of("9/", false, "9//"),
      Arguments.of("/", false, "9//"),
      Arguments.of("6/12345678/", false, "6/12345678/"),
      Arguments.of("6/12345678", false, "6/12345678/"),
      Arguments.of("6/12345678/", false, "6/12345678/"),
      Arguments.of("6/", false, "6//"),
      Arguments.of("12345678", true, "6/12345678/"),
      Arguments.of("9/12345678/", true, "9/12345678/"),
      Arguments.of("6/12345678/", true, "6/12345678/"),
      Arguments.of("6/12345678", true, "6/12345678/"),
      Arguments.of("12345678/", true, "6/12345678/"),
      Arguments.of("12345678", true, "6/12345678/"),
      Arguments.of("9/12345678/", true, "9/12345678/"),
      Arguments.of("6/12345678/", true, "6/12345678/"),
      Arguments.of("", true, "6//"),
      Arguments.of("9/", true, "9//"),
      Arguments.of("6/", true, "6//"),
      Arguments.of("/", true, "6//")
    );
  }
}
