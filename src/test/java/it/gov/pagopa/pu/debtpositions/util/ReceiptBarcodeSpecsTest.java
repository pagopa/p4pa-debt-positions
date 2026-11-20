package it.gov.pagopa.pu.debtpositions.util;

import com.itextpdf.barcodes.Barcode128;
import freemarker.template.Configuration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDAttributeObject;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDLayoutAttributeObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReceiptBarcodeSpecsTest {
  private static final String FISCAL_CODE = "12345678901";
  private static final String NAV = "301234567890123456";

  // Standard conversion factor (1 point = 1/72 inch, 1 inch = 25.4 mm)
  private static final double MM_PER_POINT = 25.4 / 72.0;

  // PagoPA spec
  private static final double NOMINAL_X_DIMENSION_MM = 0.25;
  private static final double MIN_HEIGHT_MM = 14.0;
  private static final double TOLERANCE = 0.10;

  @Spy
  private Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);

  private DocumentComposition documentComposition;

  @BeforeEach
  void setup() throws IOException {
    // DocumentComposition loads the real RECEIPT template and styles/index.css from resources
    documentComposition = new DocumentComposition(cfg);
  }

  @Test
  void checkBarcodesMeetPagoPaSpecs() throws Exception {
    Map<String, String> model = Map.ofEntries(
      entry("logo", "static/images/pagoPA.png"),
      entry("headerOrgName", "Org name"),
      entry("amount", "10,00"),
      entry("paymentDate", "10 gennaio 2026"),
      entry("pspName", "PSP"),
      entry("remittanceInformation", "Remittance Information"),
      entry("debtorName", "Debtor Name"),
      entry("debtorFiscalCode", "RSSMRA80A01H501U"),
      entry("orgName", "Org Name"),
      entry("orgFiscalCode", FISCAL_CODE),
      entry("orgFiscalCodeBarcode", BarcodeUtils.generateCode128AsBase64(FISCAL_CODE)),
      entry("nav", NAV),
      entry("navBarcode", BarcodeUtils.generateCode128AsBase64(NAV)),
      entry("iur", "IUR-123"),
      entry("iud", "IUD-456"),
      entry("receiptFooter", "Receipt Footer")
    );

    try (PDDocument pdf = PDDocument.load(documentComposition.executePdfTemplate(DocumentComposition.TemplateType.RECEIPT, model))) {
      List<PDStructureElement> figures = new ArrayList<>();
      collectFigures(pdf.getDocumentCatalog().getStructureTreeRoot(), figures);

      List<PDStructureElement> barcodes = figures.stream()
        .filter(f -> f.getAlternateDescription() != null && f.getAlternateDescription().startsWith("Barcode"))
        .toList();

      assertAll(
        () -> assertEquals(2, barcodes.size()),
        () -> assertBarcodeGeometry(barcodes, "Barcode Ente Creditore", FISCAL_CODE),
        () -> assertBarcodeGeometry(barcodes, "Barcode Codice Avviso", NAV)
      );
    }
  }

  // Recursively traverses the PDF structure tree to collect all elements tagged as "Figure"
  private static void collectFigures(PDStructureNode node, List<PDStructureElement> figures) {
    if (node == null || node.getKids() == null) return;
    for (Object child : node.getKids()) {
      if (child instanceof PDStructureElement element) {
        if ("Figure".equals(element.getStructureType())) {
          figures.add(element);
        } else {
          collectFigures(element, figures);
        }
      }
    }
  }

  private static void assertBarcodeGeometry(List<PDStructureElement> barcodes, String altLabel, String value) {
    List<PDStructureElement> matches = barcodes.stream().filter(f -> altLabel.equals(f.getAlternateDescription())).toList();
    assertEquals(1, matches.size());

    PDAttributeObject genericAttribute = matches.getFirst().getAttributes().getObject(0);

    if (genericAttribute instanceof PDLayoutAttributeObject layout) {
      PDRectangle box = layout.getBBox();

      // Calculate the theoretical number of Code128 modules
      byte[] runs = Barcode128.getBarsCode128Raw(Barcode128.getRawText(value, false));
      int modules = IntStream.range(0, runs.length)
        .map(i -> runs[i])
        .sum();

      // Calculate physical dimensions in millimeters
      double heightMm = box.getHeight() * MM_PER_POINT;
      double xDimensionMm = (box.getWidth() * MM_PER_POINT) / modules;

      // Define acceptable limits based on 10% tolerance
      double minAllowedX = NOMINAL_X_DIMENSION_MM * (1.0 - TOLERANCE);
      double maxAllowedX = NOMINAL_X_DIMENSION_MM * (1.0 + TOLERANCE);
      double minAllowedHeight = MIN_HEIGHT_MM * (1.0 - TOLERANCE);

      assertAll(
        // Specification: Module width tolerance +- 10%
        () -> assertTrue(xDimensionMm >= minAllowedX,
          String.format("%s: X-Dimension %.3f mm is below the minimum tolerated limit of %.3f mm", altLabel, xDimensionMm, minAllowedX)),
        () -> assertTrue(xDimensionMm <= maxAllowedX,
          String.format("%s: X-Dimension %.3f mm is above the maximum tolerated limit of %.3f mm", altLabel, xDimensionMm, maxAllowedX)),

        // Specification: Minimum bar height --> 14 mm
        () -> assertTrue(heightMm >= minAllowedHeight,
          String.format("%s: Height %.1f mm is below the required minimum", altLabel, heightMm))
      );
    } else {
      fail(altLabel + ": PDF attribute does not contain layout information (BBox).");
    }
  }
}
