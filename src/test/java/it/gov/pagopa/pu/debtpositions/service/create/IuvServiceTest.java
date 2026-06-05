package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IuvServiceTest {

  @Mock
  private IuvSequenceNumberService iuvSequenceNumberService;
  @Mock
  private BrokerService brokerServiceMock;

  private static final String ACCESS_TOKEN = "ACCESSTOKEN";

  private IuvServiceImpl iuvService;

  private static final String INTERNAL_IUV_SYSTEM_ID = "00";

  private static final String VALID_ORG_FISCAL_CODE = "VALID_FISCAL_CODE";
  private static final String VALID_ORG_IPA_CODE = "VALID_IPA_CODE";
  private static final String VALID_APPLICATION_CODE = "01";
  private static final Organization VALID_ORG = OrganizationFaker.buildOrganization()
    .organizationId(1L)
    .brokerId(1L)
    .orgName("ORG_NAME")
    .status(OrganizationStatus.ACTIVE)
    .orgFiscalCode(VALID_ORG_FISCAL_CODE)
    .ipaCode(VALID_ORG_IPA_CODE);
  private static final OrganizationStationDTO VALID_ORG_STATION = OrganizationFaker.buildOrganizationStation()
    .organizationId(1L)
    .orgName("ORG_NAME")
    .segregationCode(VALID_APPLICATION_CODE)
    .orgFiscalCode(VALID_ORG_FISCAL_CODE)
    .status(OrganizationStatus.ACTIVE)
    .ipaCode(VALID_ORG_IPA_CODE);
  private static final Broker BROKER = Broker.builder()
    .brokerId(1L)
    .organizationId(1L)
    .brokerFiscalCode("brokerFiscalCode")
    .brokerName("brokerName")
    .externalId("externalId")
    .flagDelegate(true)
    .flagPaymentsReporting(true)
    .iuvSystemId(INTERNAL_IUV_SYSTEM_ID)
    .build();
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

  private static final String AUX_DIGIT = "3";

  @BeforeEach
  void setUp() {
    iuvService = new IuvServiceImpl(AUX_DIGIT, iuvSequenceNumberService, brokerServiceMock);
  }

  //region test generateIuv
  @Test
  void givenValidOrgWhenGenerateIuvThenOk(){
    //Given
    Mockito.when(iuvSequenceNumberService.getNextIuvSequenceNumber(VALID_ORG.getOrganizationId())).thenReturn(VALID_PAYMENT_INDEX);
    Mockito.when(brokerServiceMock.findById(VALID_ORG.getBrokerId(), ACCESS_TOKEN)).thenReturn(BROKER);
    //When
    String result = iuvService.generateIuv(VALID_ORG, VALID_ORG_STATION.getSegregationCode(), ACCESS_TOKEN);
    //Verify
    Assertions.assertEquals(VALID_IUV, result);
    Mockito.verify(iuvSequenceNumberService, Mockito.times(1)).getNextIuvSequenceNumber(VALID_ORG.getOrganizationId());
  }

  @Test
  void givenNotFoundBrokerWhenGenerateIuvThenThrowNotFoundException(){
    //When
    String segregationCode = VALID_ORG_STATION.getSegregationCode();
    NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> iuvService.generateIuv(VALID_ORG, segregationCode, ACCESS_TOKEN));
    //Verify
    Assertions.assertNotNull(exception);
    Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_BROKER_NOT_FOUND, exception.getCode());
    Assertions.assertEquals("Broker not found having brokerId %d".formatted(VALID_ORG.getBrokerId()), exception.getMessage());
  }

  @Test
  void givenEmptyOrgWhenGenerateIuvThenException(){
    //Given
    Mockito.when(iuvSequenceNumberService.getNextIuvSequenceNumber(INVALID_ORG.getOrganizationId())).thenReturn(INVALID_PAYMENT_INDEX);
    Mockito.when(brokerServiceMock.findById(INVALID_ORG.getBrokerId(), ACCESS_TOKEN)).thenReturn(BROKER);
    //Verify
    Assertions.assertThrows(InvalidValueException.class, () -> iuvService.generateIuv(INVALID_ORG, null, ACCESS_TOKEN));
  }
  //endregion

  //region test iuv2Nav
  @Test
  void whenIuv2NavThenOk(){
    //When
    String result = iuvService.iuv2Nav(VALID_IUV);
    //Verify
    Assertions.assertEquals(AUX_DIGIT+VALID_IUV, result);
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
    String result = iuvService.nav2Iuv(AUX_DIGIT+VALID_IUV);
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
    boolean result = iuvService.isValidNav(AUX_DIGIT+VALID_IUV);
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
    boolean result = iuvService.isValidNav(AUX_DIGIT+WRONG_LENGTH_IUV);
    //Verify
    Assertions.assertFalse(result);
  }

  @Test
  void givenWrongCheckDigitNavWhenIsValidNavThenException(){
    //When
    boolean result = iuvService.isValidNav(AUX_DIGIT+WRONG_CHECK_IUV);
    //Verify
    Assertions.assertFalse(result);
  }

  @Test
  void givenNotNumericNavWhenIsValidNavThenException(){
    //When
    boolean result = iuvService.isValidNav(AUX_DIGIT+"NOT_NUMERIC_12345");
    //Verify
    Assertions.assertFalse(result);
  }

  @Test
  void givenIuvLengthNotValidWhenValidateIuvAndRetrieveNavThenException(){
    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class,
      () -> iuvService.validateIuvAndRetrieveNav(WRONG_LENGTH_IUV, VALID_ORG_STATION.getSegregationCode(), 1234L, ACCESS_TOKEN));

    Assertions.assertEquals("INVALID_IUV",exception.getCode());
    Assertions.assertEquals("The iuv must be 17 characters long", exception.getMessage());
  }

  @Test
  void givenIuvWithSegregationNotValidWhenValidateIuvAndRetrieveNavThenException(){
    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class,
      () -> iuvService.validateIuvAndRetrieveNav("0X000000000004285", VALID_ORG_STATION.getSegregationCode(), 1234L, ACCESS_TOKEN));

    Assertions.assertEquals("INVALID_IUV",exception.getCode());
    Assertions.assertEquals("The first two character of iuv must be the same of segregation code of organization", exception.getMessage());
  }

  @Test
  void givenIuvWithInformationSystemIdNotValidWhenValidateIuvAndRetrieveNavThenException(){
    //GIVEN
    Mockito.when(brokerServiceMock.findById(VALID_ORG.getBrokerId(), ACCESS_TOKEN)).thenReturn(BROKER);

    //WHEN
    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class,
      () -> iuvService.validateIuvAndRetrieveNav("01000000000004285", VALID_ORG_STATION.getSegregationCode(), VALID_ORG.getBrokerId(), ACCESS_TOKEN));

    //THEN
    Assertions.assertEquals("INVALID_IUV", exception.getCode());
    Assertions.assertEquals("The third and fourth characters cannot be '%s' for externally generated IUV".formatted(INTERNAL_IUV_SYSTEM_ID), exception.getMessage());
  }

  @Test
  void givenIuvValidWhenValidateIuvAndRetrieveNavThenOk(){
    //GIVEN
    Mockito.when(brokerServiceMock.findById(VALID_ORG.getBrokerId(), ACCESS_TOKEN)).thenReturn(BROKER);

    String externalIuv = "01990000000004285";
    //WHEN
    String result = iuvService.validateIuvAndRetrieveNav(externalIuv, VALID_ORG_STATION.getSegregationCode(), VALID_ORG.getBrokerId(), ACCESS_TOKEN);
    //THEN
    Assertions.assertEquals(AUX_DIGIT + externalIuv , result);
  }

}
