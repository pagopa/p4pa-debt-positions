package it.gov.pagopa.pu.debtpositions.util;

import com.itextpdf.barcodes.Barcode128;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BarcodeUtils {

  private static final String BASE64_SVG_PREFIX = "data:image/svg+xml;base64,";
  private static final float BAR_HEIGHT_MM = 14f;
  private static final float MODULE_WIDTH_MM = 0.25f;

  public static String generateCode128AsBase64(String content) {
    if (StringUtils.isBlank(content)) {
      return "";
    }

    try {
      byte[] runs = Barcode128.getBarsCode128Raw(Barcode128.getRawText(content, false));

      int totalModules = calculateTotalModules(runs);
      String barsSvg = generateRectanglesSvg(runs);

      // OpenHTMLtoPDF treats a root viewBox as a size in pixels. Keep physical
      // dimensions on the outer SVG and module coordinates on the inner SVG.
      String svg = String.format(Locale.ROOT,
        "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%.2fmm\" height=\"%smm\">" +
          "<svg viewBox=\"0 0 %d 1\" preserveAspectRatio=\"none\">%s</svg></svg>",
        totalModules * MODULE_WIDTH_MM, BAR_HEIGHT_MM, totalModules, barsSvg);

      return BASE64_SVG_PREFIX + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      // Returning empty string on failure: the caller (PDF template) will render without barcode
      // rather than failing the entire receipt generation.
      log.error("Error generating barcode for content: {}", content, e);
      return "";
    }
  }

  // The 'runs' array contains alternating widths for black bars and white spaces.
  // The total width of the barcode is simply the sum of all these widths.
  private static int calculateTotalModules(byte[] runs) {
    int sum = 0;
    for (byte run : runs) {
      sum += run;
    }
    return sum;
  }

  // The 'runs' array contains an alternating sequence of widths:
  // - Even indices (0, 2, 4...): width of the black bars
  // - Odd indices (1, 3, 5...): width of the white spaces
  // This method draws SVG rectangles (<rect>) only for the black bars (i % 2 == 0),
  // but the horizontal cursor ('currentX') always advances to maintain correct spacing.
  private static String generateRectanglesSvg(byte[] runs) {
    StringBuilder bars = new StringBuilder();
    int currentX = 0;

    for (int i = 0; i < runs.length; i++) {
      int width = runs[i];
      if (i % 2 == 0 && width > 0) {
        bars.append("<rect x=\"").append(currentX).append("\" width=\"").append(width).append("\" height=\"1\"/>");
      }
      currentX += width;
    }
    return bars.toString();
  }
}
