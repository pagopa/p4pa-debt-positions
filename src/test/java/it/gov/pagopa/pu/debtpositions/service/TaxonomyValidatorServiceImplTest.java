package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomyEmbedded;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.debtpositions.util.faker.TaxonomyFaker.buildTaxonomy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TaxonomyValidatorServiceImplTest {

  public static final String VALID_CATEGORY = "001122233";
  public static final String VALID_TAXONOMY_CODE = "9/001122233/";
  @Mock
  private TaxonomyService taxonomyServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;

  private TaxonomyValidatorService service;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    service = new TaxonomyValidatorServiceImpl(taxonomyServiceMock, organizationServiceMock, List.of("9/"), "/");
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
  }

  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(taxonomyServiceMock, organizationServiceMock);
  }

  @Test
  void givenValidVCategoryWhenValidateTaxonomyCategoryThenOk() {
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";
    String orgFiscalCode = "orgFiscalCode";
    Organization org = buildOrganization();
    org.setOrgTypeCode("00");

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(taxonomyServiceMock.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 5, null, accessToken))
      .thenReturn(pagedModelTaxonomy);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(orgFiscalCode, accessToken))
      .thenReturn(Optional.of(org));

    assertTrue(() -> service.validateTaxonomyCategory(VALID_CATEGORY, orgFiscalCode));
    assertTrue(() -> service.validateTaxonomyCategory(VALID_TAXONOMY_CODE, orgFiscalCode));
  }

  @Test
  void givenInvalidCategoryWhenValidateTaxonomyCategoryThenThrowException() {
    String orgFiscalCode = "orgFiscalCode";
    Organization org = buildOrganization();
    org.setOrgTypeCode(null);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(orgFiscalCode, accessToken))
      .thenReturn(Optional.of(org));

    assertFalse(() -> service.validateTaxonomyCategory("0011222", "orgFiscalCode"));
  }

  @Test
  void givenValidCategoryWhenIsValidThenTrue(){
    // Given
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(taxonomyServiceMock.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 5, null, accessToken))
      .thenReturn(pagedModelTaxonomy);
    // When, Then
    Assertions.assertTrue(() -> service.isTaxonomyCategoryValid(VALID_CATEGORY, organizationType));
  }

  @Test
  void givenValidCategoryFetchesPagedModelTaxonomyWithEmptyTaxonomiesWhenIsValidThenFalse(){
    // Given
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(Collections.emptyList())
        .build())
      .build();

    Mockito.when(taxonomyServiceMock.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 5, null, accessToken))
      .thenReturn(pagedModelTaxonomy);
    // When, Then
    Assertions.assertFalse(service.isTaxonomyCategoryValid(VALID_CATEGORY, organizationType));
  }

  @Test
  void givenValidCategoryFetchesPagedModelTaxonomyWithNullEmbeddedWhenIsValidThenFalse(){
    // Given
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(null)
      .build();

    Mockito.when(taxonomyServiceMock.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 5, null, accessToken))
      .thenReturn(pagedModelTaxonomy);
    // When, Then
    Assertions.assertFalse(service.isTaxonomyCategoryValid(VALID_CATEGORY, organizationType));
  }

  @Test
  void givenValidCategoryFetchesNullPagedModelTaxonomyWhenIsValidThenFalse(){
    // Given
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";

    Mockito.when(taxonomyServiceMock.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 5, null, accessToken))
      .thenReturn(null);
    // When, Then
    Assertions.assertFalse(service.isTaxonomyCategoryValid(VALID_CATEGORY, organizationType));
  }

  @Test
  void givenInvalidCategory_categoryDoesNotMeetRequiredLength_whenIsValidThenFalse(){
    // Given
    String invalidCategory = "0011222"; // valid 001122233
    String organizationType = "00";
    // When, Then
    Assertions.assertFalse(service.isTaxonomyCategoryValid(invalidCategory, organizationType));
  }

  @Test
  void givenValidTaxonomyCodeWhenIsValidThenTrue(){
    // Given
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(taxonomyServiceMock.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 5, null, accessToken))
      .thenReturn(pagedModelTaxonomy);
    // When, Then
    Assertions.assertTrue(() -> service.isTaxonomyCodeValid(VALID_TAXONOMY_CODE, organizationType));
  }

  @Test
  void givenInvalidTaxonomyCode_categoryDoesNotMeetRequiredLength_whenIsValidThenFalse(){
    // Given
    String invalidTaxonomyCode = "9/0011222/"; // valid 9/001122233/
    String organizationType = "00";
    // When, Then
    Assertions.assertFalse(service.isTaxonomyCodeValid(invalidTaxonomyCode, organizationType));
  }

  @Test
  void givenInvalidTaxonomyCode_incompleteFormat_whenIsValidThenFalse(){
    // Given
    String invalidTaxonomyCode1 = "9/001122233"; // valid 9/001122233/
    String organizationType = "00";
    // When, Then
    Assertions.assertFalse(() -> service.isTaxonomyCodeValid(invalidTaxonomyCode1, organizationType));

    // Given
    String invalidTaxonomyCode2 = "001122233/"; // valid 9/001122233/
    // When, Then
    Assertions.assertFalse(() -> service.isTaxonomyCodeValid(invalidTaxonomyCode2, organizationType));
  }

  @Test
  void givenValidCategoryAndNullOrgTypeCodeWhenIsValidThenTrue(){
    // Given
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(taxonomyServiceMock.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 5, null, accessToken))
      .thenReturn(pagedModelTaxonomy);
    // When, Then
    Assertions.assertTrue(() -> service.isTaxonomyCategoryValid(VALID_CATEGORY, null));
  }

  @Test
  void givenMismatchedOrgTypeCodeWhenIsTaxonomyCategoryValidThenFalse() {
    // Given
    String mismatchedOrgTypeCode = "99";
    // When, Then
    Assertions.assertFalse(service.isTaxonomyCategoryValid(VALID_CATEGORY, mismatchedOrgTypeCode));
  }

  @Test
  void givenMismatchedOrgTypeCodeWhenIsTaxonomyCodeValidThenFalse() {
    // Given
    String mismatchedOrgTypeCode = "99";
    // When, Then
    Assertions.assertFalse(() -> service.isTaxonomyCodeValid(
      VALID_TAXONOMY_CODE, mismatchedOrgTypeCode));
  }

}
