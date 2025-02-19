package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateIuvServiceTest {

  @Mock
  private IuvService iuvService;

  @InjectMocks
  private GenerateIuvServiceImpl generateIuvService;

  private static final String VALID_ORG_FISCAL_CODE = "VALID_FISCAL_CODE";
  private static final String VALID_ORG_IPA_CODE = "VALID_IPA_CODE";
  private static final Organization VALID_ORG = OrganizationFaker.buildOrganization()
    .organizationId(1L)
    .orgName("ORG_NAME")
    .status(Organization.StatusEnum.ACTIVE)
    .orgFiscalCode(VALID_ORG_FISCAL_CODE)
    .ipaCode(VALID_ORG_IPA_CODE);
  private static final String VALID_IUV = "12345678901234567";

  @Test
  void givenValidOrgWhenGenerateIuvThenOk() {
    //Given
    Mockito.when(iuvService.generateIuv(VALID_ORG)).thenReturn(VALID_IUV);
    //When
    String result = generateIuvService.generateIuv(VALID_ORG);
    //Verify
    Assertions.assertEquals(VALID_IUV, result);
    Mockito.verify(iuvService, Mockito.times(1)).generateIuv(VALID_ORG);
  }

  @Test
  void givenValidIuvWhenGenerateNavThenOk() {
    //Given
    String iuv = "generatedIuv";
    String nav = "3generatedIuv";
    Mockito.when(iuvService.iuv2Nav(iuv)).thenReturn(nav);
    //When
    String result = generateIuvService.iuv2Nav(iuv);
    //Verify
    Assertions.assertEquals(nav, result);
    Mockito.verify(iuvService, Mockito.times(1)).iuv2Nav(iuv);
  }
}
