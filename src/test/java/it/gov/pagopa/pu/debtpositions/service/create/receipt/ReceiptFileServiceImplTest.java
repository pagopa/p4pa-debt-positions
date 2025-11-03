package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import freemarker.template.TemplateException;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.FileResourceDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.util.DocumentComposition;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.co.jemos.podam.api.PodamFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.service.create.receipt.ReceiptFileServiceImpl.DATE_TIME_FORMATTER;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReceiptFileServiceImplTest {
  @Mock
  private DocumentComposition documentCompositionMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private ReceiptService receiptServiceMock;

  private ReceiptFileService receiptFileService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final String accessToken = "fakeAccessToken";
  private final String userId = "USERID";

  @BeforeEach
  void setUp() {
    receiptFileService = new ReceiptFileServiceImpl(documentCompositionMock, organizationServiceMock, receiptServiceMock);
    SecurityUtilsTest.configureSecurityContext(accessToken, userId);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      documentCompositionMock,
      organizationServiceMock,
      receiptServiceMock
    );
  }

  @Test
  void givenValidUserWhenGetReceiptPdfThenOk() throws TemplateException, IOException {
    Long receiptId = 123L;
    Long organizationId = 1L;
    ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(ReceiptDetailDTO.class);
    receiptDetailDTO.setReceiptId(receiptId);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode("FISCALCODE");
    byte[] expectedContent = "PDF-DATA".getBytes();

    FileResourceDTO expectedResult = new FileResourceDTO(new ByteArrayResource(expectedContent),
      "RECEIPT_"+organization.getOrgFiscalCode()+"_"+receiptId+".pdf");

    Mockito.when(documentCompositionMock.executePdfTemplate(Mockito.eq(DocumentComposition.TemplateType.RECEIPT),Mockito.argThat((Map<String,Object> o) ->
      o.get(ReceiptFileServiceImpl.RECEIPT_LOGO).equals(organization.getOrgLogo())
        && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_NAME).equals(organization.getOrgName())
        && o.get(ReceiptFileServiceImpl.RECEIPT_IUV).equals(receiptDetailDTO.getIuv())
        && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_NAME).equals(receiptDetailDTO.getDebtor().getFullName())
        && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_FISCAL_CODE).equals(receiptDetailDTO.getDebtor().getFiscalCode())
        && o.get(ReceiptFileServiceImpl.RECEIPT_PAYMENT_DATE).equals(receiptDetailDTO.getPaymentDateTime().format(DATE_TIME_FORMATTER))
        && o.get(ReceiptFileServiceImpl.RECEIPT_PSP_NAME).equals(receiptDetailDTO.getPspCompanyName())
        && o.get(ReceiptFileServiceImpl.RECEIPT_AMOUNT).equals(Utilities.formatPrice(receiptDetailDTO.getPaymentAmountCents()))
        && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE).equals(organization.getOrgFiscalCode())
        && o.get(ReceiptFileServiceImpl.REMITTANCE_INFORMATION).equals(receiptDetailDTO.getRemittanceInformation())
        && o.get(ReceiptFileServiceImpl.IUR).equals(receiptDetailDTO.getIur())
        && o.get(ReceiptFileServiceImpl.IUD).equals(receiptDetailDTO.getIud())
        && o.get(ReceiptFileServiceImpl.EMISSION_DATE) != null
        && !o.get(ReceiptFileServiceImpl.EMISSION_DATE).toString().isEmpty()
        && o.get(ReceiptFileServiceImpl.EMISSION_TIME) != null
        && !o.get(ReceiptFileServiceImpl.EMISSION_TIME).toString().isEmpty()
    ))).thenReturn(expectedContent);

    Mockito.when(receiptServiceMock.getReceiptDetail(receiptId, userId, organizationId))
      .thenReturn(receiptDetailDTO);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(Optional.of(organization));

    FileResourceDTO result = receiptFileService.generateReceiptPdf(receiptId, organizationId);

    assertNotNull(result);
    assertEquals(expectedResult, result);
  }

  @Test
  void givenIOExceptionWhenGetReceiptPdfThenIllegalStateException() throws TemplateException, IOException {
    Long receiptId = 123L;
    Long organizationId = 1L;
    ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(ReceiptDetailDTO.class);
    receiptDetailDTO.setReceiptId(receiptId);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode("FISCALCODE");

    Mockito.when(receiptServiceMock.getReceiptDetail(receiptId, userId, organizationId))
      .thenReturn(receiptDetailDTO);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(Optional.of(organization));

    Mockito.when(documentCompositionMock.executePdfTemplate(Mockito.eq(DocumentComposition.TemplateType.RECEIPT),Mockito.argThat((Map<String,Object> o) ->
      o.get(ReceiptFileServiceImpl.RECEIPT_LOGO).equals(organization.getOrgLogo())
        && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_NAME).equals(organization.getOrgName())
        && o.get(ReceiptFileServiceImpl.RECEIPT_IUV).equals(receiptDetailDTO.getIuv())
        && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_NAME).equals(receiptDetailDTO.getDebtor().getFullName())
        && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_FISCAL_CODE).equals(receiptDetailDTO.getDebtor().getFiscalCode())
        && o.get(ReceiptFileServiceImpl.RECEIPT_PAYMENT_DATE).equals(receiptDetailDTO.getPaymentDateTime().format(DATE_TIME_FORMATTER))
        && o.get(ReceiptFileServiceImpl.RECEIPT_PSP_NAME).equals(receiptDetailDTO.getPspCompanyName())
        && o.get(ReceiptFileServiceImpl.RECEIPT_AMOUNT).equals(Utilities.formatPrice(receiptDetailDTO.getPaymentAmountCents()))
        && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE).equals(organization.getOrgFiscalCode())
        && o.get(ReceiptFileServiceImpl.REMITTANCE_INFORMATION).equals(receiptDetailDTO.getRemittanceInformation())
        && o.get(ReceiptFileServiceImpl.IUR).equals(receiptDetailDTO.getIur())
        && o.get(ReceiptFileServiceImpl.IUD).equals(receiptDetailDTO.getIud())
        && o.get(ReceiptFileServiceImpl.EMISSION_DATE) != null
        && !o.get(ReceiptFileServiceImpl.EMISSION_DATE).toString().isEmpty()
        && o.get(ReceiptFileServiceImpl.EMISSION_TIME) != null
        && !o.get(ReceiptFileServiceImpl.EMISSION_TIME).toString().isEmpty()
    ))).thenThrow(new IOException());

    Assertions.assertThrows(IllegalStateException.class,()-> receiptFileService.generateReceiptPdf(receiptId, organizationId));
  }

  @Test
  void givenTemplateExceptionWhenGetReceiptPdfThenIllegalStateException() throws TemplateException, IOException {
    Long receiptId = 123L;
    Long organizationId = 1L;
    ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(ReceiptDetailDTO.class);
    receiptDetailDTO.setReceiptId(receiptId);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode("FISCALCODE");

    Mockito.when(receiptServiceMock.getReceiptDetail(receiptId, userId, organizationId))
      .thenReturn(receiptDetailDTO);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(Optional.of(organization));


    Mockito.when(documentCompositionMock.executePdfTemplate(Mockito.eq(DocumentComposition.TemplateType.RECEIPT),Mockito.argThat((Map<String,Object> o) ->
      o.get(ReceiptFileServiceImpl.RECEIPT_LOGO).equals(organization.getOrgLogo())
        && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_NAME).equals(organization.getOrgName())
        && o.get(ReceiptFileServiceImpl.RECEIPT_IUV).equals(receiptDetailDTO.getIuv())
        && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_NAME).equals(receiptDetailDTO.getDebtor().getFullName())
        && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_FISCAL_CODE).equals(receiptDetailDTO.getDebtor().getFiscalCode())
        && o.get(ReceiptFileServiceImpl.RECEIPT_PAYMENT_DATE).equals(receiptDetailDTO.getPaymentDateTime().format(DATE_TIME_FORMATTER))
        && o.get(ReceiptFileServiceImpl.RECEIPT_PSP_NAME).equals(receiptDetailDTO.getPspCompanyName())
        && o.get(ReceiptFileServiceImpl.RECEIPT_AMOUNT).equals(Utilities.formatPrice(receiptDetailDTO.getPaymentAmountCents()))
        && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE).equals(organization.getOrgFiscalCode())
        && o.get(ReceiptFileServiceImpl.REMITTANCE_INFORMATION).equals(receiptDetailDTO.getRemittanceInformation())
        && o.get(ReceiptFileServiceImpl.IUR).equals(receiptDetailDTO.getIur())
        && o.get(ReceiptFileServiceImpl.IUD).equals(receiptDetailDTO.getIud())
        && o.get(ReceiptFileServiceImpl.EMISSION_DATE) != null
        && !o.get(ReceiptFileServiceImpl.EMISSION_DATE).toString().isEmpty()
        && o.get(ReceiptFileServiceImpl.EMISSION_TIME) != null
        && !o.get(ReceiptFileServiceImpl.EMISSION_TIME).toString().isEmpty()
    ))).thenThrow(new TemplateException(null));

    Assertions.assertThrows(IllegalStateException.class,()-> receiptFileService.generateReceiptPdf(receiptId, organizationId));
  }
}
