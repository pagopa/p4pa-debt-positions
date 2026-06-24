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
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class ReceiptFileServiceImpl implements ReceiptFileService {
  public static final String RECEIPT_LOGO = "logo";
  public static final String RECEIPT_ORG_NAME = "orgName";
  public static final String RECEIPT_HEADER_ORG_NAME = "headerOrgName";
  public static final String RECEIPT_NAV = "nav";
  public static final String RECEIPT_NAV_BARCODE = "navBarcode";
  public static final String RECEIPT_ORG_FISCAL_CODE_BARCODE = "orgFiscalCodeBarcode";
  public static final String RECEIPT_DEBTOR_NAME = "debtorName";
  public static final String RECEIPT_DEBTOR_FISCAL_CODE = "debtorFiscalCode";
  public static final String RECEIPT_PAYMENT_DATE = "paymentDate";
  public static final String RECEIPT_PSP_NAME = "pspName";
  public static final String RECEIPT_AMOUNT = "amount";
  public static final String RECEIPT_ORG_FISCAL_CODE = "orgFiscalCode";
  public static final String RECEIPT_FOOTER = "receiptFooter";
  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.ITALIAN);
  public static final DateTimeFormatter DATE_FORMATTER_FOOTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ITALIAN);
  public static final DateTimeFormatter TIME_FORMATTER_FOOTER = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN);
  public static final String REMITTANCE_INFORMATION = "remittanceInformation";
  public static final String IUD = "iud";
  public static final String IUR = "iur";

  private final DocumentComposition documentComposition;
  private final OrganizationService organizationService;
  private final ReceiptService receiptService;
  private final BrokerService brokerService;
  private final TransferRepository transferRepository;

  public ReceiptFileServiceImpl(DocumentComposition documentComposition,
                                OrganizationService organizationService,
                                ReceiptService receiptService,
                                BrokerService brokerService,
                                TransferRepository transferRepository) {
    this.documentComposition = documentComposition;
    this.organizationService = organizationService;
    this.receiptService = receiptService;
    this.brokerService = brokerService;
    this.transferRepository = transferRepository;
  }


  public FileResourceDTO generateReceiptPdf(Long receiptId, Long organizationId, String accessToken, String operatorExternalUserId) {
    ReceiptDetailDTO receiptDetail = receiptService.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, null);
    if (receiptDetail == null) {
      throw new NotFoundException(ErrorCodeConstants.ERROR_CODE_RECEIPT_NOT_FOUND, "Receipt with id " + receiptId + " not found");
    }

    Organization organization = organizationService.getOrganizationById(organizationId, accessToken)
      .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_ORGANIZATION_NOT_FOUND, "Organization with id " + organizationId + " not found"));

    Broker broker = brokerService.findById(organization.getBrokerId(), accessToken);
    BrokerConfiguration brokerConfiguration = brokerService.getBrokerConfigurationsById(organization.getBrokerId(), accessToken);

    String orgName = organization.getOrgName();
    String headerOrgName = orgName;
    String footerOrgName = orgName;
    String orgFiscalCode = organization.getOrgFiscalCode();

    if (broker.getFlagDelegate()) {
      Transfer ownerTransfer = transferRepository.findOwnerTransferByOrganizationIdAndReceiptId(organizationId, receiptId)
        .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_TRANSFER_NOT_FOUND, "Transfer with flag owner not found for receiptId " + receiptId));

      orgName = ownerTransfer.getOrgName();
      headerOrgName = "";
      footerOrgName = broker.getBrokerName();
      orgFiscalCode = ownerTransfer.getOrgFiscalCode();
    }

    String receiptFooter = buildReceiptFooter(brokerConfiguration, footerOrgName);

    byte[] receiptPdf;
    try {
      receiptPdf = documentComposition.executePdfTemplate(
        DocumentComposition.TemplateType.RECEIPT,
        buildTemplateModel(
          receiptDetail,
          organization.getOrgLogo(),
          headerOrgName,
          orgName,
          orgFiscalCode,
          receiptFooter
        )
      );
    } catch (IOException | TemplateException e) {
      throw new IllegalStateException(e);
    }

    return new FileResourceDTO(new ByteArrayResource(receiptPdf),
      "RECEIPT_" + organization.getOrgFiscalCode() + "_" + receiptId + ".pdf");
  }

  private String buildReceiptFooter(BrokerConfiguration brokerConfiguration, String orgName) {
    String receiptFooter = brokerConfiguration.getReceiptFooter();
    if (receiptFooter == null) {
      return "";
    }

    LocalDateTime now = LocalDateTime.now();
    String emissionDateStr = now.format(DATE_FORMATTER_FOOTER);
    String emissionTimeStr = now.format(TIME_FORMATTER_FOOTER);

    return receiptFooter
      .replace("{{emissionDate}}", emissionDateStr)
      .replace("{{emissionTime}}", emissionTimeStr)
      .replace("{{orgName}}", orgName);
  }

  private Map<String, Object> buildTemplateModel(ReceiptDetailDTO receiptDetail, String logo, String headerOrgName, String orgName, String fiscalCode, String footer) {
    Map<String, Object> templateModel = new HashMap<>();

    templateModel.put(RECEIPT_LOGO, StringUtils.defaultString(logo));
    templateModel.put(RECEIPT_HEADER_ORG_NAME, headerOrgName);
    templateModel.put(RECEIPT_ORG_NAME, orgName);

    String nav = StringUtils.defaultString(receiptDetail.getNav());
    templateModel.put(RECEIPT_NAV, nav);
    templateModel.put(RECEIPT_NAV_BARCODE, BarcodeUtils.generateCode128AsBase64(nav));

    String orgFiscalCode = StringUtils.defaultString(fiscalCode);
    templateModel.put(RECEIPT_ORG_FISCAL_CODE, orgFiscalCode);
    templateModel.put(RECEIPT_ORG_FISCAL_CODE_BARCODE, BarcodeUtils.generateCode128AsBase64(orgFiscalCode));

    templateModel.put(RECEIPT_DEBTOR_NAME, receiptDetail.getDebtor().getFullName());
    templateModel.put(RECEIPT_DEBTOR_FISCAL_CODE, receiptDetail.getDebtor().getFiscalCode());
    templateModel.put(RECEIPT_PAYMENT_DATE, receiptDetail.getPaymentDateTime() != null ? receiptDetail.getPaymentDateTime().format(DATE_TIME_FORMATTER) : "");
    templateModel.put(RECEIPT_PSP_NAME, receiptDetail.getPspCompanyName());
    templateModel.put(RECEIPT_AMOUNT, Utilities.formatPrice(receiptDetail.getPaymentAmountCents()));

    String remittanceInformation = StringUtils.isNotBlank(receiptDetail.getOriginalRemittanceInformation()) ?
      receiptDetail.getOriginalRemittanceInformation() : StringUtils.defaultString(receiptDetail.getRemittanceInformation());
    templateModel.put(REMITTANCE_INFORMATION, remittanceInformation);
    templateModel.put(IUR, receiptDetail.getIur());
    templateModel.put(IUD, receiptDetail.getIud());
    templateModel.put(RECEIPT_FOOTER, footer);

    return templateModel;
  }
}
