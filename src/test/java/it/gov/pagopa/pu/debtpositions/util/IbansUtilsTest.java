package it.gov.pagopa.pu.debtpositions.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IbansUtilsTest {
  @Test
  void givenDptoIbanWhenResolveIbanAndPostalIbanThenReturnDptoIbans() {
    String dptoIban = "IT0000000000000000000000000";
    String dptoPostalIban = "IT0000000000000000000000001";
    String orgIban = "IT0000000000000000000000002";
    String orgPostalIban = "IT0000000000000000000000003";

    Pair<String, String> result = IbansUtils.resolveIbanAndPostalIban(dptoIban, dptoPostalIban, orgIban, orgPostalIban);

    assertEquals(dptoIban, result.getLeft());
    assertEquals(dptoPostalIban, result.getRight());
  }

  @Test
  void givenNullDptoIbanWhenResolveIbanAndPostalIbanThenReturnOrgIbans() {
    String dptoPostalIban = "IT0000000000000000000000001";
    String orgIban = "IT0000000000000000000000002";
    String orgPostalIban = "IT0000000000000000000000003";

    Pair<String, String> result = IbansUtils.resolveIbanAndPostalIban(null, dptoPostalIban, orgIban, orgPostalIban);

    assertEquals(orgIban, result.getLeft());
    assertEquals(orgPostalIban, result.getRight());
  }
}
