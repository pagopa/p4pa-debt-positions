package it.gov.pagopa.pu.debtpositions.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class Utilities {

  private Utilities() {
  }

  public static OffsetDateTime localDatetimeToOffsetDateTime(LocalDateTime localDateTime) {
    return localDateTime.atOffset(ZoneOffset.UTC);
  }
}
