package it.gov.pagopa.pu.debtpositions.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

  private Utilities() {}
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    public static final int IBAN_LENGTH = 27;

    public static boolean isValidEmail(final String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

  public static OffsetDateTime localDatetimeToOffsetDateTime(LocalDateTime localDateTime){
    return localDateTime != null
      ? localDateTime.atOffset(ZoneId.systemDefault().getRules().getOffset(localDateTime))
      : null;
  }
    public static boolean isValidIban(String iban) {
        return iban != null && iban.length() == IBAN_LENGTH;
    }

    public static boolean isValidPIVA(String pi) {
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

  public static String getRandomIUD() {
    return "000" + getRandomicUUID();
  }

  public static String getRandomicUUID() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
