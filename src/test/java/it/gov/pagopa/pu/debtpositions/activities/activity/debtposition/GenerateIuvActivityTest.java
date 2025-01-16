package it.gov.pagopa.pu.debtpositions.activities.activity.debtposition;

import it.gov.pagopa.pu.debtpositions.activities.activity.GenerateIuvActivityImpl;
import it.gov.pagopa.pu.debtpositions.activities.connector.organization.OrganizationService;
import it.gov.pagopa.pu.debtpositions.activities.exception.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.activities.service.IuvService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GenerateIuvActivityTest {

  @Mock
  private OrganizationService organizationService;

  @Mock
  private IuvService iuvService;

  @InjectMocks
  private GenerateIuvActivityImpl generateIuvActivity;

  private static final String VALID_ORG_FISCAL_CODE = "VALID_FISCAL_CODE";
  private static final String VALID_ORG_IPA_CODE = "VALID_IPA_CODE";
  private static final Organization VALID_ORG = Organization.builder()
    .organizationId(1L)
    .orgFiscalCode(VALID_ORG_FISCAL_CODE)
    .ipaCode(VALID_ORG_IPA_CODE)
    .build();
  private static final String VALID_IUV = "12345678901234567";

  private static final String INVALID_ORG_FISCAL_CODE = "INVALID_FISCAL_CODE";

  @Test
  void givenValidOrgWhenGenerateIuvThenOk(){
    //Given
    Mockito.when(organizationService.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE)).thenReturn(Optional.of(VALID_ORG));
    Mockito.when(iuvService.generateIuv(VALID_ORG)).thenReturn(VALID_IUV);
    //When
    String result = generateIuvActivity.generateIuv(VALID_ORG_FISCAL_CODE);
    //Verify
    Assertions.assertEquals(VALID_IUV, result);
    Mockito.verify(organizationService, Mockito.times(1)).getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE);
    Mockito.verify(iuvService,Mockito.times(1)).generateIuv(VALID_ORG);
  }

  @Test
  void givenEmptyOrgWhenGenerateIuvThenException(){
    //Verify
    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class, () -> generateIuvActivity.generateIuv(""));
    Assertions.assertEquals("invalid orgFiscalCode", exception.getMessage());
  }

  @Test
  void givenInvalidOrgWhenGenerateIuvThenException(){
    //Given
    Mockito.when(organizationService.getOrganizationByFiscalCode(INVALID_ORG_FISCAL_CODE)).thenReturn(Optional.empty());
    //Verify
    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class, () -> generateIuvActivity.generateIuv(INVALID_ORG_FISCAL_CODE));
    Assertions.assertEquals("invalid organization", exception.getMessage());
  }
}
