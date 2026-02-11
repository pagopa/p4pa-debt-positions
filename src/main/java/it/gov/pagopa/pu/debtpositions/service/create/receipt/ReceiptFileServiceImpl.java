package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import freemarker.template.TemplateException;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.FileResourceDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.util.BarcodeUtils;
import it.gov.pagopa.pu.debtpositions.util.DocumentComposition;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
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
  public static final String RECEIPT_NAV = "nav";
  public static final String RECEIPT_NAV_BARCODE = "navBarcode";
  public static final String RECEIPT_DEBTOR_NAME = "debtorName";
  public static final String RECEIPT_DEBTOR_FISCAL_CODE = "debtorFiscalCode";
  public static final String RECEIPT_PAYMENT_DATE = "paymentDate";
  public static final String RECEIPT_PSP_NAME = "pspName";
  public static final String RECEIPT_AMOUNT = "amount";
  public static final String RECEIPT_ORG_FISCAL_CODE = "orgFiscalCode";
  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.ITALIAN);
  public static final DateTimeFormatter DATE_FORMATTER_FOOTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ITALIAN);
  public static final DateTimeFormatter TIME_FORMATTER_FOOTER = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN);
  public static final String REMITTANCE_INFORMATION = "remittanceInformation";
  public static final String IUD = "iud";
  public static final String IUR = "iur";
  public static final String EMISSION_DATE = "emissionDate";
  public static final String EMISSION_TIME = "emissionTime";

  private final DocumentComposition documentComposition;
  private final OrganizationService organizationService;
  private final ReceiptService receiptService;

  public ReceiptFileServiceImpl(DocumentComposition documentComposition,
                                OrganizationService organizationService,
                                ReceiptService receiptService) {
    this.documentComposition = documentComposition;
    this.organizationService = organizationService;
    this.receiptService = receiptService;
  }


  public FileResourceDTO generateReceiptPdf(Long receiptId, Long organizationId) {
    ReceiptDetailDTO receiptDetail = receiptService.getReceiptDetail(receiptId, SecurityUtils.getCurrentUserExternalId(), organizationId, null);
    if (receiptDetail == null) {
      throw new NotFoundException("[RECEIPT_NOT_FOUND] Receipt with id " + receiptId + " not found");
    }
    Organization organization = organizationService.getOrganizationById(organizationId, SecurityUtils.getAccessToken())
      .orElseThrow(() -> new NotFoundException("[ORGANIZATION_NOT_FOUND] Organization with id " + organizationId + " not found"));

    byte[] receiptPdf;
    try {
      receiptPdf = documentComposition.executePdfTemplate(DocumentComposition.TemplateType.RECEIPT, buildTemplateModel(receiptDetail, organization));
    } catch (IOException | TemplateException e) {
      throw new IllegalStateException(e);
    }

    return new FileResourceDTO(new ByteArrayResource(receiptPdf),
      "RECEIPT_" + organization.getOrgFiscalCode() + "_" + receiptId + ".pdf");
  }

  private Map<String, Object> buildTemplateModel(ReceiptDetailDTO receiptDetail, Organization organization) {
    Map<String, Object> templateModel = new HashMap<>();

    templateModel.put(RECEIPT_LOGO, StringUtils.defaultString(organization.getOrgLogo()));
    templateModel.put(RECEIPT_ORG_NAME, organization.getOrgName());

    String nav = StringUtils.defaultString(receiptDetail.getNav());
    templateModel.put(RECEIPT_NAV, nav);
    templateModel.put(RECEIPT_NAV_BARCODE, BarcodeUtils.generateCode128AsBase64(nav));

    templateModel.put(RECEIPT_DEBTOR_NAME, receiptDetail.getDebtor().getFullName());
    templateModel.put(RECEIPT_DEBTOR_FISCAL_CODE, receiptDetail.getDebtor().getFiscalCode());
    templateModel.put(RECEIPT_PAYMENT_DATE, receiptDetail.getPaymentDateTime() != null ? receiptDetail.getPaymentDateTime().format(DATE_TIME_FORMATTER) : "");
    templateModel.put(RECEIPT_PSP_NAME, receiptDetail.getPspCompanyName());
    templateModel.put(RECEIPT_AMOUNT, Utilities.formatPrice(receiptDetail.getPaymentAmountCents()));
    templateModel.put(RECEIPT_ORG_FISCAL_CODE, organization.getOrgFiscalCode());
    templateModel.put(REMITTANCE_INFORMATION, StringUtils.defaultString(receiptDetail.getRemittanceInformation()));
    templateModel.put(IUR, receiptDetail.getIur());
    templateModel.put(IUD, receiptDetail.getIud());
    templateModel.put(EMISSION_DATE, LocalDateTime.now().format(DATE_FORMATTER_FOOTER));
    templateModel.put(EMISSION_TIME, LocalDateTime.now().format(TIME_FORMATTER_FOOTER));
    return templateModel;
  }
}
