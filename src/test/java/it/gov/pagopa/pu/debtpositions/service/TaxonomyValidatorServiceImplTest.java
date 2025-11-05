package it.gov.pagopa.pu.debtpositions.service;

import static it.gov.pagopa.pu.debtpositions.util.faker.TaxonomyFaker.buildTaxonomy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomyEmbedded;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxonomyValidatorServiceImplTest {

  public static final String VALID_CATEGORY = "001122233";
  public static final String VALID_TAXONOMY_CODE = "9/001122233/";
  @Mock
  private TaxonomyService taxonomyServiceMock;
  @InjectMocks
  private TaxonomyValidatorServiceImpl service;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
  }

  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(taxonomyServiceMock);
  }

  @Test
  void givenValidVCategoryWhenValidateTaxonomyCategoryThenOk() {
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

    assertDoesNotThrow(() -> service.validateTaxonomyCategory(VALID_CATEGORY));
    assertDoesNotThrow(() -> service.validateTaxonomyCategory(VALID_TAXONOMY_CODE));
  }

  @Test
  void givenInvalidCategoryWhenValidateTaxonomyCategoryThenThrowException() {
    assertThrows(InvalidValueException.class, () -> service.validateTaxonomyCategory("0011222"));
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
