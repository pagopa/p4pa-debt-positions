package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IuvServiceTest {

  @Mock
  private IuvSequenceNumberService iuvSequenceNumberService;

  private IuvServiceImpl iuvService;

  private static final String VALID_ORG_FISCAL_CODE = "VALID_FISCAL_CODE";
  private static final String VALID_ORG_IPA_CODE = "VALID_IPA_CODE";
  private static final String VALID_APPLICATION_CODE = "01";
  private static final Organization VALID_ORG = OrganizationFaker.buildOrganization()
    .organizationId(1L)
    .orgName("ORG_NAME")
    .status(OrganizationStatus.ACTIVE)
    .orgFiscalCode(VALID_ORG_FISCAL_CODE)
    .ipaCode(VALID_ORG_IPA_CODE)
    .segregationCode(VALID_APPLICATION_CODE);
  private static final long VALID_PAYMENT_INDEX = 42L;
  private static final String VALID_IUV = "01000000000004285";
  private static final String WRONG_CHECK_IUV = "01000000000004286";
  private static final String WRONG_LENGTH_IUV = "010000000000004285";

  private static final String INVALID_ORG_FISCAL_CODE = "INVALID_VAT_CODE";
  private static final String INVALID_ORG_IPA_CODE = "INVALID_IPA_CODE";
  private static final long INVALID_PAYMENT_INDEX = 0L;
  private static final Organization INVALID_ORG = OrganizationFaker.buildOrganization()
    .organizationId(99L)
    .orgName("INVALID_ORG_NAME")
    .status(OrganizationStatus.DRAFT)
    .orgFiscalCode(INVALID_ORG_FISCAL_CODE)
    .ipaCode(INVALID_ORG_IPA_CODE);

  @BeforeEach
  void setUp() {
    iuvService = new IuvServiceImpl("00", iuvSequenceNumberService);
  }

  //region test generateIuv
  @Test
  void givenValidOrgWhenGenerateIuvThenOk(){
    //Given
    Mockito.when(iuvSequenceNumberService.getNextIuvSequenceNumber(VALID_ORG.getOrganizationId())).thenReturn(VALID_PAYMENT_INDEX);
    //When
    String result = iuvService.generateIuv(VALID_ORG);
    //Verify
    Assertions.assertEquals(VALID_IUV, result);
    Mockito.verify(iuvSequenceNumberService, Mockito.times(1)).getNextIuvSequenceNumber(VALID_ORG.getOrganizationId());
  }

  @Test
  void givenEmptyOrgWhenGenerateIuvThenException(){
    //Given
    Mockito.when(iuvSequenceNumberService.getNextIuvSequenceNumber(INVALID_ORG.getOrganizationId())).thenReturn(INVALID_PAYMENT_INDEX);
    //Verify
    Assertions.assertThrows(InvalidValueException.class, () -> iuvService.generateIuv(INVALID_ORG));
  }
  //endregion

  //region test iuv2Nav
  @Test
  void whenIuv2NavThenOk(){
    //When
    String result = iuvService.iuv2Nav(VALID_IUV);
    //Verify
    Assertions.assertEquals(IuvServiceImpl.AUX_DIGIT+VALID_IUV, result);
  }

  @Test
  void givenInvalidIuvWhenIuv2NavThenException(){
    //Verify
    Assertions.assertThrows(InvalidValueException.class, () -> iuvService.iuv2Nav(WRONG_CHECK_IUV));
  }
  //endregion

  //region test nav2Iuv
  @Test
  void givenValidNavWhenNav2IuvThenOk(){
    //When
    String result = iuvService.nav2Iuv(IuvServiceImpl.AUX_DIGIT+VALID_IUV);
    //Verify
    Assertions.assertEquals(VALID_IUV, result);
  }

  @Test
  void givenInvalidNavWhenNav2IuvThenException(){
    //Verify
    Assertions.assertThrows(InvalidValueException.class, () -> iuvService.nav2Iuv("4"+VALID_IUV));
  }
  //endregion

  //region test isValidNav
  @Test
  void givenValidNavWhenIsValidNavThenOk(){
    //When
    boolean result = iuvService.isValidNav(IuvServiceImpl.AUX_DIGIT+VALID_IUV);
    //Verify
    Assertions.assertTrue(result);
  }

  @Test
  void givenInvalidNavWhenIsValidNavThenException(){
    //When
    boolean result = iuvService.isValidNav("4"+VALID_IUV);
    //Verify
    Assertions.assertFalse(result);
  }

  @Test
  void givenWrongLengthNavWhenIsValidNavThenException(){
    //When
    boolean result = iuvService.isValidNav(IuvServiceImpl.AUX_DIGIT+WRONG_LENGTH_IUV);
    //Verify
    Assertions.assertFalse(result);
  }

  @Test
  void givenWrongCheckDigitNavWhenIsValidNavThenException(){
    //When
    boolean result = iuvService.isValidNav(IuvServiceImpl.AUX_DIGIT+WRONG_CHECK_IUV);
    //Verify
    Assertions.assertFalse(result);
  }

  @Test
  void givenNotNumericNavWhenIsValidNavThenException(){
    //When
    boolean result = iuvService.isValidNav(IuvServiceImpl.AUX_DIGIT+"NOT_NUMERIC_12345");
    //Verify
    Assertions.assertFalse(result);
  }

  @Test
  void givenIuvLengthNotValidWhenValidateIuvAndRetrieveNavThenException(){
    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class,
      () -> iuvService.validateIuvAndRetrieveNav(WRONG_LENGTH_IUV, VALID_ORG, DebtPositionOrigin.ORDINARY));

    Assertions.assertEquals("[INVALID_IUV] The iuv must be 17 characters long", exception.getMessage());
  }

  @Test
  void givenIuvWithSegregationNotValidWhenValidateIuvAndRetrieveNavThenException(){
    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class,
      () -> iuvService.validateIuvAndRetrieveNav("0X000000000004285", VALID_ORG, DebtPositionOrigin.ORDINARY));

    Assertions.assertEquals("[INVALID_IUV] The first two character of iuv must be the same of segregation code of organization", exception.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ORDINARY", "SPONTANEOUS", "SPONTANEOUS_SIL"})
  void givenIuvWithInformationSystemIdNotValidWhenValidateIuvAndRetrieveNavThenException(String debtPositionOrigin) {
    DebtPositionOrigin origin = DebtPositionOrigin.valueOf(debtPositionOrigin);

    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class,
      () -> iuvService.validateIuvAndRetrieveNav("01032000000004285", IuvServiceTest.VALID_ORG, origin));

    Assertions.assertEquals("[INVALID_IUV] The third and fourth characters must be '00' for the origin: " + origin, exception.getMessage());
  }

  @Test
  void givenIuvWithInformationSystemIdNotValidWhenValidateIuvAndRetrieveNavThenException(){
    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class,
      () -> iuvService.validateIuvAndRetrieveNav("01000000000004285", VALID_ORG, DebtPositionOrigin.SECONDARY_ORG));

    Assertions.assertEquals("[INVALID_IUV] The third and fourth characters cannot be '00' for the origin: " + DebtPositionOrigin.SECONDARY_ORG, exception.getMessage());
  }

  @Test
  void givenIuvValidWhenValidateIuvAndRetrieveNavThenOk(){
    String externalIuv = "01990000000004285";
    String result = iuvService.validateIuvAndRetrieveNav(externalIuv, VALID_ORG, DebtPositionOrigin.SECONDARY_ORG);

    Assertions.assertEquals(3+externalIuv , result);
  }

  @Test
  void givenIuvValidAndOriginOrdinaryWhenValidateIuvAndRetrieveNavThenOk(){
    String externalIuv = "01000000000004285";
    String result = iuvService.validateIuvAndRetrieveNav(externalIuv, VALID_ORG, DebtPositionOrigin.ORDINARY);

    Assertions.assertEquals(3+externalIuv , result);
  }
}
