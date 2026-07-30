package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utilities {

  private Utilities() {
  }

  public static final Pattern FISCAL_CODE_STRUCTURE_REGEX = Pattern.compile("^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST][0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z][0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z])$");
  public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
  public static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}\\d{2}[A-Z0-9]{23,30}$");
  public static final String POSTAL_IBAN_ABI_CODE = "07601";
  private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.ITALY);

  public static boolean isValidEmail(final String email) {
    Matcher matcher = EMAIL_PATTERN.matcher(email);
    return matcher.matches();
  }

  public static OffsetDateTime localDatetimeToOffsetDateTime(LocalDateTime localDateTime) {
    return localDateTime != null
      ? localDateTime.atOffset(ZoneId.systemDefault().getRules().getOffset(localDateTime))
      : null;
  }

  public static LocalDateTime offsetDateTimeToLocalDateTime(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null
      ? offsetDateTime.toLocalDateTime()
      : null;
  }

  public static boolean isValidIban(String iban) {
    return iban != null && IBAN_PATTERN.matcher(iban).matches();
  }

  public static boolean isValidPostalIban(String iban){
    if(iban == null || iban.length() < 10) {
      return false;
    }
    String abiCode = iban.substring(5, 10);
    return POSTAL_IBAN_ABI_CODE.equals(abiCode) && isValidIban(iban);
  }

  public static boolean isValidPIVA(String pi, boolean isOrgPIvaCheckEnabled) {
    int i;
    int c;
    int s;
    if (pi.isEmpty())
      return false;
    if (pi.length() != 11)
      return false;
    for (i = 0; i < 11; i++) {
      if (pi.charAt(i) < '0' || pi.charAt(i) > '9')
        return false;
    }
    if (isOrgPIvaCheckEnabled) {
      s = 0;
      for (i = 0; i <= 9; i += 2)
        s += pi.charAt(i) - '0';
      for (i = 1; i <= 9; i += 2) {
        c = 2 * (pi.charAt(i) - '0');
        if (c > 9)
          c = c - 9;
        s += c;
      }
      return (10 - s % 10) % 10 == pi.charAt(10) - '0';
    }
    return true;
  }

  public static boolean isValidFiscalCode(String fiscalCode) {
    Matcher matcher = FISCAL_CODE_STRUCTURE_REGEX.matcher(fiscalCode.toUpperCase());
    return matcher.matches();
  }

  public static boolean isValidFiscalCodeOrPIVA(String fiscalCode, boolean isOrgPIvaCheckEnabled) {
    return isValidFiscalCode(fiscalCode) || isValidPIVA(fiscalCode, isOrgPIvaCheckEnabled);
  }

  public static String getRandomIUD() {
    return "000" + getRandomicUUID();
  }

  public static String getRandomicUUID() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  public static String generateRandomIupd(String orgFiscalCode) {
    String lastUuidPart = UUID.randomUUID().toString().substring(26);
    return String.join("-",
      orgFiscalCode,
      LocalDateTime.now(Constants.ZONEID).format(DateTimeFormatter.ofPattern("ddMMyyHHmmss")),
      lastUuidPart
    );
  }

  public static <T> void checkImmutableField(String fieldName, T original, T updated, List<ErrorFieldDTO> modifiedFields) {
    @SuppressWarnings("unchecked") // suppressing: same type due to same Generic type
    boolean fieldUpdated =
      (original instanceof OffsetDateTime o1 && updated instanceof OffsetDateTime o2)
        ? o1.toEpochSecond() != o2.toEpochSecond()
        : (original instanceof @SuppressWarnings("rawtypes")Comparable c1 && updated instanceof Comparable<?> c2)
        ? c1.compareTo(c2) != 0
        : !Objects.equals(original, updated);
    if (fieldUpdated) {
      modifiedFields.add(new ErrorFieldDTO(fieldName, ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD, "Cannot be updated"));
    }
  }

  public static boolean isValidIntervalBetweenOffsetDateTime(OffsetDateTime dateFrom, OffsetDateTime dateTo, ChronoUnit chronoUnit, long maxInterval) {
    return chronoUnit.between(dateFrom, dateTo) <= maxInterval;
  }

  public static String getTraceId() {
    return MDC.get("traceId");
  }

  public static BigDecimal longCentsToBigDecimalEuro(Long centsAmount) {
    return centsAmount != null ? BigDecimal.valueOf(centsAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_DOWN) : null;
  }

  public static LocalDateTime toLocalDateTime(OffsetDateTime date) {
    return date != null ? date.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() : null;
  }

  public static String formatPrice(Long priceInCents) {
    if (priceInCents == null) {
      return "";
    }
    double price = priceInCents / 100.0;
    return currencyFormat.format(price);
  }

  public static <T> String mapToString(Map<String, T> map, Function<T, String> entry2String) {
    return CollectionUtils.isEmpty(map)
      ? ""
      : map.entrySet().stream()
      .map(e -> e.getKey() + ":" + entry2String.apply(e.getValue()))
      .collect(Collectors.joining(", "));
  }

  /**
   * Resolves iban and postalIban based on the presence of dptoIban.
   * If dptoIban is present, it is used as the resolvedIban and dptoPostalIban is used as the resolvedPostalIban.
   * If dptoIban is not present, orgIban is used as the resolvedIban and orgPostalIban is used as the resolvedPostalIban.
   */
  public static Pair<String, String> resolveIbanAndPostalIban(String dptoIban, String dptoPostalIban, String orgIban, String orgPostalIban) {
    boolean isDptoIbanEmpty = StringUtils.isEmpty(dptoIban);
    String resolvedIban = isDptoIbanEmpty ? orgIban : dptoIban;
    String resolvedPostalIban = isDptoIbanEmpty ? orgPostalIban : dptoPostalIban;

    return Pair.of(resolvedIban, resolvedPostalIban);
  }
}
