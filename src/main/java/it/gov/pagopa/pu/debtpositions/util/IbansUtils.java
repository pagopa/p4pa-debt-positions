package it.gov.pagopa.pu.debtpositions.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class IbansUtils {
  private IbansUtils() {
  }

  /**
   * Resolves iban and postalIban based on the presence of dptoIban.
   * If dptoIban is present, it is used as the resolvedIban and dptoPostalIban is used as the resolvedPostalIban.
   * If dptoIban is not present, orgIban is used as the resolvedIban and orgPostalIban is used as the resolvedPostalIban.
   */
  public static Pair<String, String> resolveIbanAndPostalIban(String dptoIban, String dptoPostalIban, String orgIban, String orgPostalIban) {
    String resolvedIban = StringUtils.firstNonBlank(dptoIban, orgIban);
    String resolvedPostalIban = StringUtils.isNotBlank(dptoIban) ? dptoPostalIban : orgPostalIban;

    return Pair.of(resolvedIban, resolvedPostalIban);
  }
}
