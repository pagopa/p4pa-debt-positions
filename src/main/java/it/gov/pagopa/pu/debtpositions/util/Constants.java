package it.gov.pagopa.pu.debtpositions.util;

import java.time.ZoneId;
import java.util.TimeZone;

public class Constants {

  private Constants(){}

  public static final ZoneId ZONEID = ZoneId.of("Europe/Rome");
  public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone(ZONEID);

  public static final String MIXED_DP_TYPE_ORG_CODE = "MIXED";

  public static final String WS_USER_PREFIX = "WS_USER-";

  public static final Long TECHNICAL_UNKNOWN_ORG_ID = -1L;

  public static final Long DEBT_POSITION_TYPE_MIXED = -2L;

  public static final String UNKNOWN_STATION_ID = "UNKNOWN";
}

