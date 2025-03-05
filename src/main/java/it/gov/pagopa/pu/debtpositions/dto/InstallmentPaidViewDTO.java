package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.enums.UniqueIdentifierType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InstallmentPaidViewDTO  extends InstallmentPaidViewPIIDTO{

  private String iuf;
  private Integer flowRowNumber;
  @NotNull
  private String iud;
  @NotNull
  private String iuv;
  private Integer objectVersion;
  @NotNull
  private String domainIdentifier;
  private String requestingStationIdentifier;
  @NotNull
  private String receiptMessageIdentifier;
  private OffsetDateTime receiptMessageDateTime;
  @NotNull
  private String requestMessageReference;
  private OffsetDateTime requestDateTimeReference;
  private UniqueIdentifierType uniqueIdentifierType;
  @NotNull
  private String uniqueIdentifierCode;
  @NotNull
  private String attestingName;
  private String attestingUnitOperCode;
  private String attestingUnitOperName;
  private String attestingAddress;
  private String attestingStreetNumber;
  private String attestingPostalCode;
  private String attestingCity;
  private String attestingProvince;
  private String attestingCountry;
  private PersonEntityType beneficiaryEntityType;
  private String beneficiaryUniqueIdentifierCode;
  private String beneficiaryName;
  private String beneficiaryUnitOperCode;
  private String beneficiaryUnitOperName;
  private String beneficiaryAddress;
  private String beneficiaryStreetNumber;
  private String beneficiaryPostalCode;
  private String beneficiaryCity;
  private String beneficiaryProvince;
  private String beneficiaryCountry;
  private PersonEntityType payerEntityType;
  private PersonEntityType payerUniqueIdentifierCode;
  @NotNull
  private PersonEntityType subjectPayingEntityType;
  private Integer paymentOutcomeCode;
  @NotNull
  private Long totalAmountPaidCents;
  @NotNull
  private String uniquePaymentIdentifier;
  @NotNull
  private String paymentContextCode;
  @NotNull
  private Long singleAmountPaidCents;
  private String singlePaymentOutcome;
  private OffsetDateTime singlePaymentOutcomeDateTime;
  @NotNull
  private String uniqueCollectionIdentifier;

  @Min(1)
  @Max(140)
  @NotNull
  private String paymentReason;

  @Min(5)
  @Max(140)
  @NotNull
  private String collectionSpecificData;

  @NotBlank
  @Min(1)
  @Max(1024)
  private String dueType;

  @Min(1)
  @Max(15)
  private Integer signatureType;

  private String rt;
  @NotNull
  private Integer singlePaymentDataIndex;
  private Long pspAppliedFeesCents;
  private String receiptAttachmentType;
  private String receiptAttachmentTest;
  private String balance;
  @NotNull
  private String orgFiscalCode;
  private String orgName;
  @NotNull
  private String dueTaxonomicCode;

}
