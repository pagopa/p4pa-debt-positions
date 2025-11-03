package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import freemarker.template.TemplateException;
import it.gov.pagopa.pu.debtpositions.util.DocumentComposition;
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
import uk.co.jemos.podam.api.PodamFactory;

import java.io.IOException;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.service.create.receipt.ReceiptFileServiceImpl.DATE_TIME_FORMATTER;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReceiptFileServiceImplTest {
  @Mock
  private DocumentComposition documentCompositionMock;

  private ReceiptFileService receiptFileService;
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    receiptFileService = new ReceiptFileServiceImpl(documentCompositionMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      documentCompositionMock
    );
  }

  @Test
  void givenValidUserWhenGetReceiptPdfThenOk() throws TemplateException, IOException {
    it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    byte[] expectedResult = "PDF-DATA".getBytes();

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
    ))).thenReturn(expectedResult);

    byte[] result = receiptFileService.generateReceiptPdf(receiptDetailDTO, organization);

    assertNotNull(result);
    assertArrayEquals(expectedResult, result);
  }

  @Test
  void givenIOExceptionWhenGetReceiptPdfThenIllegalStateException() throws TemplateException, IOException {
    it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

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

    Assertions.assertThrows(IllegalStateException.class,()-> receiptFileService.generateReceiptPdf(receiptDetailDTO,organization));
  }

  @Test
  void givenTemplateExceptionWhenGetReceiptPdfThenIllegalStateException() throws TemplateException, IOException {
    it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

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

    Assertions.assertThrows(IllegalStateException.class,()-> receiptFileService.generateReceiptPdf(receiptDetailDTO,organization));
  }
}
