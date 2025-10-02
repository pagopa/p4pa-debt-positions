package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomyEmbedded;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.TaxonomyFaker.buildTaxonomy;

@ExtendWith(MockitoExtension.class)
class CategoryValidatorServiceImplTest {

  @Mock
  private TaxonomyService taxonomyServiceMock;
  @InjectMocks
  private CategoryValidatorServiceImpl service;

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
  void givenValidCategoryWhenIsValidThenTrue(){
    // Given
    String validCategory = "001122233";
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
    Assertions.assertTrue(() -> service.isCategoryValid(validCategory));
  }

  @Test
  void givenValidCategoryFetchesPagedModelTaxonomyWithEmptyTaxonomiesWhenIsValidThenFalse(){
    // Given
    String validCategory = "001122233";
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
    Assertions.assertFalse(service.isCategoryValid(validCategory));
  }

  @Test
  void givenValidCategoryFetchesPagedModelTaxonomyWithNullEmbeddedWhenIsValidThenFalse(){
    // Given
    String validCategory = "001122233";
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
    Assertions.assertFalse(service.isCategoryValid(validCategory));
  }

  @Test
  void givenValidCategoryFetchesNullPagedModelTaxonomyWhenIsValidThenFalse(){
    // Given
    String validCategory = "001122233";
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";

    Mockito.when(taxonomyServiceMock.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 5, null, accessToken))
      .thenReturn(null);
    // When, Then
    Assertions.assertFalse(service.isCategoryValid(validCategory));
  }

  @Test
  void givenInvalidCategory_categoryDoesNotMeetRequiredLength_whenIsValidThenFalse(){
    // Given
    String invalidCategory = "0011222"; // valid 001122233
    // When, Then
    Assertions.assertFalse(service.isCategoryValid(invalidCategory));
  }

  @Test
  void givenValidTaxonomyCodeWhenIsValidThenTrue(){
    // Given
    String validTaxonomyCode = "9/001122233/";
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
    Assertions.assertTrue(() -> service.isTaxonomyCodeValid(validTaxonomyCode));
  }

  @Test
  void givenInvalidTaxonomyCode_categoryDoesNotMeetRequiredLength_whenIsValidThenFalse(){
    // Given
    String invalidTaxonomyCode = "9/0011222/"; // valid 9/001122233/
    // When, Then
    Assertions.assertFalse(service.isTaxonomyCodeValid(invalidTaxonomyCode));
  }

  @Test
  void givenInvalidTaxonomyCode_incompleteFormat_whenIsValidThenFalse(){
    // Given
    String invalidTaxonomyCode1 = "9/001122233"; // valid 9/001122233/
    // When, Then
    Assertions.assertFalse(() -> service.isTaxonomyCodeValid(invalidTaxonomyCode1));

    // Given
    String invalidTaxonomyCode2 = "001122233/"; // valid 9/001122233/
    // When, Then
    Assertions.assertFalse(() -> service.isTaxonomyCodeValid(invalidTaxonomyCode2));
  }

}
