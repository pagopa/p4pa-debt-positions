package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TaxonomyFaker {

  private static final OffsetDateTime DATETIME = OffsetDateTime.of(LocalDate.of(2099, 1, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC);

  public static Taxonomy buildTaxonomy() {
    return Taxonomy.builder()
      .taxonomyId(1L)
      .taxonomyCode("CODE")
      .organizationTypeDescription("desc")
      .organizationType("00")
      .macroAreaCode("11")
      .serviceTypeCode("222")
      .collectionReason("33")
      .macroAreaDescription("macroAreaDesc")
      .macroAreaName("macroAreaName")
      .serviceType("serviceType")
      .serviceTypeDescription("serviceTypeDescription")
      .startDateValidity(DATETIME)
      .endDateOfValidity(DATETIME)
      .build();
  }
}
