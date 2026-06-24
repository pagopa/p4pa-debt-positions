package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import freemarker.template.TemplateException;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.FileResourceDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.util.BarcodeUtils;
import it.gov.pagopa.pu.debtpositions.util.DocumentComposition;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
  @Mock
  private BrokerService brokerServiceMock;
  @Mock
  private TransferRepository transferRepositoryMock;

  private ReceiptFileService receiptFileService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  private final String accessToken = "fakeAccessToken";
  private final String operatorExternalUserId = "operatorExternalUserId";

  private static final String FAKE_NAV_BARCODE_BASE64 = "data:image/png;base64,fakeNavBarcode";
  private static final String FAKE_ORG_BARCODE_BASE64 = "data:image/png;base64,fakeOrgBarcode";

  @BeforeEach
  void setUp() {
    receiptFileService = new ReceiptFileServiceImpl(
      documentCompositionMock,
      organizationServiceMock,
      receiptServiceMock,
      brokerServiceMock,
      transferRepositoryMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      documentCompositionMock,
      organizationServiceMock,
      receiptServiceMock,
      brokerServiceMock,
      transferRepositoryMock
    );
  }

  @Test
  void givenValidUserWhenGetReceiptPdfThenOk() throws TemplateException, IOException {
    Long receiptId = 123L;
    Long organizationId = 1L;
    Long brokerId = 1L;

    ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(ReceiptDetailDTO.class);
    receiptDetailDTO.setReceiptId(receiptId);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode("FISCALCODE");
    organization.setBrokerId(brokerId);

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setBrokerId(brokerId);
    broker.setFlagDelegate(false);

    BrokerConfiguration brokerConfiguration = podamFactory.manufacturePojo(BrokerConfiguration.class);
    brokerConfiguration.setReceiptFooter("Test footer {{orgName}}");

    String expectedFooter = "Test footer " + organization.getOrgName();
    byte[] expectedContent = "PDF-DATA".getBytes();

    FileResourceDTO expectedResult = new FileResourceDTO(new ByteArrayResource(expectedContent),
      "RECEIPT_"+organization.getOrgFiscalCode()+"_"+receiptId+".pdf");

    try (MockedStatic<BarcodeUtils> barcodeUtilsMock = Mockito.mockStatic(BarcodeUtils.class)) {
      barcodeUtilsMock.when(() -> BarcodeUtils.generateCode128AsBase64(receiptDetailDTO.getNav()))
        .thenReturn(FAKE_NAV_BARCODE_BASE64);
      barcodeUtilsMock.when(() -> BarcodeUtils.generateCode128AsBase64(organization.getOrgFiscalCode()))
        .thenReturn(FAKE_ORG_BARCODE_BASE64);

      Mockito.when(brokerServiceMock.findById(organization.getBrokerId(), accessToken))
        .thenReturn(broker);
      Mockito.when(brokerServiceMock.getBrokerConfigurationsById(organization.getBrokerId(), accessToken))
        .thenReturn(brokerConfiguration);

      Mockito.when(documentCompositionMock.executePdfTemplate(Mockito.eq(DocumentComposition.TemplateType.RECEIPT), Mockito.argThat((Map<String, Object> o) ->
        o.get(ReceiptFileServiceImpl.RECEIPT_LOGO).equals(organization.getOrgLogo())
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_NAME).equals(organization.getOrgName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_HEADER_ORG_NAME).equals(organization.getOrgName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_NAV).equals(receiptDetailDTO.getNav())
          && o.get(ReceiptFileServiceImpl.RECEIPT_NAV_BARCODE).equals(FAKE_NAV_BARCODE_BASE64)
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE_BARCODE).equals(FAKE_ORG_BARCODE_BASE64)
          && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_NAME).equals(receiptDetailDTO.getDebtor().getFullName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_FISCAL_CODE).equals(receiptDetailDTO.getDebtor().getFiscalCode())
          && o.get(ReceiptFileServiceImpl.RECEIPT_PAYMENT_DATE).equals(receiptDetailDTO.getPaymentDateTime().format(DATE_TIME_FORMATTER))
          && o.get(ReceiptFileServiceImpl.RECEIPT_PSP_NAME).equals(receiptDetailDTO.getPspCompanyName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_AMOUNT).equals(Utilities.formatPrice(receiptDetailDTO.getPaymentAmountCents()))
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE).equals(organization.getOrgFiscalCode())
          && o.get(ReceiptFileServiceImpl.REMITTANCE_INFORMATION).equals(receiptDetailDTO.getOriginalRemittanceInformation())
          && o.get(ReceiptFileServiceImpl.IUR).equals(receiptDetailDTO.getIur())
          && o.get(ReceiptFileServiceImpl.IUD).equals(receiptDetailDTO.getIud())
          && o.get(ReceiptFileServiceImpl.RECEIPT_FOOTER).equals(expectedFooter)
      ))).thenReturn(expectedContent);

      Mockito.when(receiptServiceMock.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, null))
        .thenReturn(receiptDetailDTO);

      Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
        .thenReturn(Optional.of(organization));

      FileResourceDTO result = receiptFileService.generateReceiptPdf(receiptId, organizationId, accessToken, operatorExternalUserId);

      assertNotNull(result);
      assertEquals(expectedResult, result);
    }
  }

  @Test
  void givenIOExceptionWhenGetReceiptPdfThenIllegalStateException() throws TemplateException, IOException {
    Long receiptId = 123L;
    Long organizationId = 1L;
    Long brokerId = 1L;

    ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(ReceiptDetailDTO.class);
    receiptDetailDTO.setReceiptId(receiptId);
    receiptDetailDTO.setOriginalRemittanceInformation(null);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode("FISCALCODE");
    organization.setBrokerId(brokerId);

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setBrokerId(brokerId);
    broker.setFlagDelegate(false);

    BrokerConfiguration brokerConfiguration = podamFactory.manufacturePojo(BrokerConfiguration.class);

    try (MockedStatic<BarcodeUtils> barcodeUtilsMock = Mockito.mockStatic(BarcodeUtils.class)) {
      barcodeUtilsMock.when(() -> BarcodeUtils.generateCode128AsBase64(receiptDetailDTO.getNav()))
        .thenReturn(FAKE_NAV_BARCODE_BASE64);
      barcodeUtilsMock.when(() -> BarcodeUtils.generateCode128AsBase64(organization.getOrgFiscalCode()))
        .thenReturn(FAKE_ORG_BARCODE_BASE64);

      Mockito.when(receiptServiceMock.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, null))
        .thenReturn(receiptDetailDTO);

      Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
        .thenReturn(Optional.of(organization));

      Mockito.when(brokerServiceMock.findById(organization.getBrokerId(), accessToken))
        .thenReturn(broker);
      Mockito.when(brokerServiceMock.getBrokerConfigurationsById(organization.getBrokerId(), accessToken))
        .thenReturn(brokerConfiguration);

      Mockito.when(documentCompositionMock.executePdfTemplate(Mockito.eq(DocumentComposition.TemplateType.RECEIPT), Mockito.argThat((Map<String, Object> o) ->
        o.get(ReceiptFileServiceImpl.RECEIPT_LOGO).equals(organization.getOrgLogo())
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_NAME).equals(organization.getOrgName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_HEADER_ORG_NAME).equals(organization.getOrgName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_NAV).equals(receiptDetailDTO.getNav())
          && o.get(ReceiptFileServiceImpl.RECEIPT_NAV_BARCODE).equals(FAKE_NAV_BARCODE_BASE64)
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE_BARCODE).equals(FAKE_ORG_BARCODE_BASE64)
          && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_NAME).equals(receiptDetailDTO.getDebtor().getFullName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_FISCAL_CODE).equals(receiptDetailDTO.getDebtor().getFiscalCode())
          && o.get(ReceiptFileServiceImpl.RECEIPT_PAYMENT_DATE).equals(receiptDetailDTO.getPaymentDateTime().format(DATE_TIME_FORMATTER))
          && o.get(ReceiptFileServiceImpl.RECEIPT_PSP_NAME).equals(receiptDetailDTO.getPspCompanyName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_AMOUNT).equals(Utilities.formatPrice(receiptDetailDTO.getPaymentAmountCents()))
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE).equals(organization.getOrgFiscalCode())
          && o.get(ReceiptFileServiceImpl.REMITTANCE_INFORMATION).equals(receiptDetailDTO.getRemittanceInformation())
          && o.get(ReceiptFileServiceImpl.IUR).equals(receiptDetailDTO.getIur())
          && o.get(ReceiptFileServiceImpl.IUD).equals(receiptDetailDTO.getIud())
          && o.get(ReceiptFileServiceImpl.RECEIPT_FOOTER).equals(brokerConfiguration.getReceiptFooter())
      ))).thenThrow(new IOException());

      Assertions.assertThrows(IllegalStateException.class, () -> receiptFileService.generateReceiptPdf(receiptId, organizationId, accessToken, operatorExternalUserId));
    }
  }

  @Test
  void givenTemplateExceptionWhenGetReceiptPdfThenIllegalStateException() throws TemplateException, IOException {
    Long receiptId = 123L;
    Long organizationId = 1L;
    Long brokerId = 1L;

    ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(ReceiptDetailDTO.class);
    receiptDetailDTO.setReceiptId(receiptId);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode("FISCALCODE");
    organization.setBrokerId(brokerId);

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setBrokerId(brokerId);
    broker.setFlagDelegate(false);

    BrokerConfiguration brokerConfiguration = podamFactory.manufacturePojo(BrokerConfiguration.class);

    try (MockedStatic<BarcodeUtils> barcodeUtilsMock = Mockito.mockStatic(BarcodeUtils.class)) {
      barcodeUtilsMock.when(() -> BarcodeUtils.generateCode128AsBase64(receiptDetailDTO.getNav()))
        .thenReturn(FAKE_NAV_BARCODE_BASE64);
      barcodeUtilsMock.when(() -> BarcodeUtils.generateCode128AsBase64(organization.getOrgFiscalCode()))
        .thenReturn(FAKE_ORG_BARCODE_BASE64);

      Mockito.when(receiptServiceMock.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, null))
        .thenReturn(receiptDetailDTO);

      Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
        .thenReturn(Optional.of(organization));

      Mockito.when(brokerServiceMock.findById(organization.getBrokerId(), accessToken))
        .thenReturn(broker);
      Mockito.when(brokerServiceMock.getBrokerConfigurationsById(organization.getBrokerId(), accessToken))
        .thenReturn(brokerConfiguration);

      Mockito.when(documentCompositionMock.executePdfTemplate(Mockito.eq(DocumentComposition.TemplateType.RECEIPT), Mockito.argThat((Map<String, Object> o) ->
        o.get(ReceiptFileServiceImpl.RECEIPT_LOGO).equals(organization.getOrgLogo())
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_NAME).equals(organization.getOrgName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_HEADER_ORG_NAME).equals(organization.getOrgName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_NAV).equals(receiptDetailDTO.getNav())
          && o.get(ReceiptFileServiceImpl.RECEIPT_NAV_BARCODE).equals(FAKE_NAV_BARCODE_BASE64)
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE_BARCODE).equals(FAKE_ORG_BARCODE_BASE64)
          && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_NAME).equals(receiptDetailDTO.getDebtor().getFullName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_FISCAL_CODE).equals(receiptDetailDTO.getDebtor().getFiscalCode())
          && o.get(ReceiptFileServiceImpl.RECEIPT_PAYMENT_DATE).equals(receiptDetailDTO.getPaymentDateTime().format(DATE_TIME_FORMATTER))
          && o.get(ReceiptFileServiceImpl.RECEIPT_PSP_NAME).equals(receiptDetailDTO.getPspCompanyName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_AMOUNT).equals(Utilities.formatPrice(receiptDetailDTO.getPaymentAmountCents()))
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE).equals(organization.getOrgFiscalCode())
          && o.get(ReceiptFileServiceImpl.REMITTANCE_INFORMATION).equals(receiptDetailDTO.getOriginalRemittanceInformation())
          && o.get(ReceiptFileServiceImpl.IUR).equals(receiptDetailDTO.getIur())
          && o.get(ReceiptFileServiceImpl.IUD).equals(receiptDetailDTO.getIud())
          && o.get(ReceiptFileServiceImpl.RECEIPT_FOOTER).equals(brokerConfiguration.getReceiptFooter())
      ))).thenThrow(new TemplateException(null));

      Assertions.assertThrows(IllegalStateException.class, () -> receiptFileService.generateReceiptPdf(receiptId, organizationId, accessToken, operatorExternalUserId));
    }
  }

  @Test
  void givenFlagBrokerDelegateTrueWhenGetReceiptPdfThenOk() throws TemplateException, IOException {
    Long receiptId = 123L;
    Long organizationId = 1L;
    Long brokerId = 1L;

    ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(ReceiptDetailDTO.class);
    receiptDetailDTO.setReceiptId(receiptId);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode("FISCALCODE");
    organization.setBrokerId(brokerId);

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setBrokerId(brokerId);
    broker.setFlagDelegate(true);

    BrokerConfiguration brokerConfiguration = podamFactory.manufacturePojo(BrokerConfiguration.class);
    brokerConfiguration.setReceiptFooter("Delegate footer per {{orgName}}");

    Transfer ownerTransfer = podamFactory.manufacturePojo(Transfer.class);
    ownerTransfer.setOrgName("OWNER_ORG_NAME");
    ownerTransfer.setOrgFiscalCode("OWNER_FISCAL_CODE");

    String expectedFooter = "Delegate footer per " + broker.getBrokerName();
    byte[] expectedContent = "PDF-DATA".getBytes();

    FileResourceDTO expectedResult = new FileResourceDTO(new ByteArrayResource(expectedContent),
      "RECEIPT_"+organization.getOrgFiscalCode()+"_"+receiptId+".pdf");

    try (MockedStatic<BarcodeUtils> barcodeUtilsMock = Mockito.mockStatic(BarcodeUtils.class)) {
      barcodeUtilsMock.when(() -> BarcodeUtils.generateCode128AsBase64(receiptDetailDTO.getNav()))
        .thenReturn(FAKE_NAV_BARCODE_BASE64);
      barcodeUtilsMock.when(() -> BarcodeUtils.generateCode128AsBase64(ownerTransfer.getOrgFiscalCode()))
        .thenReturn(FAKE_ORG_BARCODE_BASE64);

      Mockito.when(receiptServiceMock.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, null))
        .thenReturn(receiptDetailDTO);

      Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
        .thenReturn(Optional.of(organization));

      Mockito.when(brokerServiceMock.findById(organization.getBrokerId(), accessToken))
        .thenReturn(broker);
      Mockito.when(brokerServiceMock.getBrokerConfigurationsById(organization.getBrokerId(), accessToken))
        .thenReturn(brokerConfiguration);

      Mockito.when(transferRepositoryMock.findOwnerTransferByOrganizationIdAndReceiptId(organizationId, receiptId))
        .thenReturn(Optional.of(ownerTransfer));

      Mockito.when(documentCompositionMock.executePdfTemplate(Mockito.eq(DocumentComposition.TemplateType.RECEIPT), Mockito.argThat((Map<String, Object> o) ->
        o.get(ReceiptFileServiceImpl.RECEIPT_LOGO).equals(organization.getOrgLogo())
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_NAME).equals(ownerTransfer.getOrgName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_HEADER_ORG_NAME).equals("")
          && o.get(ReceiptFileServiceImpl.RECEIPT_NAV).equals(receiptDetailDTO.getNav())
          && o.get(ReceiptFileServiceImpl.RECEIPT_NAV_BARCODE).equals(FAKE_NAV_BARCODE_BASE64)
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE_BARCODE).equals(FAKE_ORG_BARCODE_BASE64)
          && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_NAME).equals(receiptDetailDTO.getDebtor().getFullName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_DEBTOR_FISCAL_CODE).equals(receiptDetailDTO.getDebtor().getFiscalCode())
          && o.get(ReceiptFileServiceImpl.RECEIPT_PAYMENT_DATE).equals(receiptDetailDTO.getPaymentDateTime().format(DATE_TIME_FORMATTER))
          && o.get(ReceiptFileServiceImpl.RECEIPT_PSP_NAME).equals(receiptDetailDTO.getPspCompanyName())
          && o.get(ReceiptFileServiceImpl.RECEIPT_AMOUNT).equals(Utilities.formatPrice(receiptDetailDTO.getPaymentAmountCents()))
          && o.get(ReceiptFileServiceImpl.RECEIPT_ORG_FISCAL_CODE).equals(ownerTransfer.getOrgFiscalCode())
          && o.get(ReceiptFileServiceImpl.REMITTANCE_INFORMATION).equals(receiptDetailDTO.getOriginalRemittanceInformation())
          && o.get(ReceiptFileServiceImpl.IUR).equals(receiptDetailDTO.getIur())
          && o.get(ReceiptFileServiceImpl.IUD).equals(receiptDetailDTO.getIud())
          && o.get(ReceiptFileServiceImpl.RECEIPT_FOOTER).equals(expectedFooter)
      ))).thenReturn(expectedContent);

      FileResourceDTO result = receiptFileService.generateReceiptPdf(receiptId, organizationId, accessToken, operatorExternalUserId);

      assertNotNull(result);
      assertEquals(expectedResult, result);
    }
  }

  @Test
  void givenBrokerDelegateTrueAndTransferNotFoundWhenGetReceiptPdfThenThrowNotFoundException() {
    Long receiptId = 123L;
    Long organizationId = 1L;
    Long brokerId = 1L;

    ReceiptDetailDTO receiptDetailDTO = podamFactory.manufacturePojo(ReceiptDetailDTO.class);
    receiptDetailDTO.setReceiptId(receiptId);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(organizationId);
    organization.setBrokerId(brokerId);

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setBrokerId(brokerId);
    broker.setFlagDelegate(true);

    BrokerConfiguration brokerConfiguration = podamFactory.manufacturePojo(BrokerConfiguration.class);

    Mockito.when(receiptServiceMock.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, null))
      .thenReturn(receiptDetailDTO);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(Optional.of(organization));

    Mockito.when(brokerServiceMock.findById(organization.getBrokerId(), accessToken))
      .thenReturn(broker);
    Mockito.when(brokerServiceMock.getBrokerConfigurationsById(organization.getBrokerId(), accessToken))
      .thenReturn(brokerConfiguration);

    Mockito.when(transferRepositoryMock.findOwnerTransferByOrganizationIdAndReceiptId(organizationId, receiptId))
      .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class, () -> receiptFileService.generateReceiptPdf(receiptId, organizationId, accessToken, operatorExternalUserId));
  }
}
