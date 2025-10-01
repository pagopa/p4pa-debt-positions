package it.gov.pagopa.pu.debtpositions.model.validator;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
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

import static it.gov.pagopa.pu.debtpositions.util.faker.TaxonomyFaker.buildTaxonomy;

@ExtendWith(MockitoExtension.class)
class TaxonomyCodeConstraintValidatorTest {

  @Mock
  private TaxonomyService taxonomyServiceMock;

  private TaxonomyCodeConstraintValidator validator;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
    validator = new TaxonomyCodeConstraintValidator(taxonomyServiceMock);
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
    Assertions.assertTrue(() -> validator.isValid(validTaxonomyCode, null));
  }

  @Test
  void givenValidTaxonomyCodeFetchesPagedModelTaxonomyWithEmptyTaxonomiesWhenIsValidThenFalse(){
    // Given
    String validTaxonomyCode = "9/001122233/";
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
    Assertions.assertFalse(() -> validator.isValid(validTaxonomyCode, null));
  }

  @Test
  void givenValidTaxonomyCodeFetchesPagedModelTaxonomyWithNullEmbeddedWhenIsValidThenFalse(){
    // Given
    String validTaxonomyCode = "9/001122233/";
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
    Assertions.assertFalse(() -> validator.isValid(validTaxonomyCode, null));
  }

  @Test
  void givenValidTaxonomyCodeFetchesNullPagedModelTaxonomyWhenIsValidThenFalse(){
    // Given
    String validTaxonomyCode = "9/001122233/";
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";

    Mockito.when(taxonomyServiceMock.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 5, null, accessToken))
      .thenReturn(null);
    // When, Then
    Assertions.assertFalse(() -> validator.isValid(validTaxonomyCode, null));
  }

  @Test
  void givenInvalidTaxonomyCode_doesNotMeetRequiredLength_whenIsValidThenFalse(){
    // Given
    String invalidTaxonomyCode = "9/0011222/"; // 9/001122233/
    // When, Then
    Assertions.assertFalse(() -> validator.isValid(invalidTaxonomyCode, null));
  }

  @Test
  void givenInvalidTaxonomyCode_incompleteFormat_whenIsValidThenFalse(){
    // Given
    String invalidTaxonomyCode = "9/001122233"; // 9/001122233/
    // When, Then
    Assertions.assertFalse(() -> validator.isValid(invalidTaxonomyCode, null));
  }

  @Test
  void givenInvalidTaxonomyCode_missingFormat_whenIsValidThenFalse(){
    // Given
    String invalidTaxonomyCode = "001122233"; // 9/001122233/
    // When, Then
    Assertions.assertFalse(() -> validator.isValid(invalidTaxonomyCode, null));
  }
}
