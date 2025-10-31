package it.gov.pagopa.pu.debtpositions.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class DateConversionUtils {
  private DateConversionUtils() {}

  public static LocalDateTime offsetDateTime2LocalDateTime(OffsetDateTime value){
    if(value==null){
      return null;
    }
    return value.atZoneSameInstant(Constants.ZONEID).toLocalDateTime();
  }
}
