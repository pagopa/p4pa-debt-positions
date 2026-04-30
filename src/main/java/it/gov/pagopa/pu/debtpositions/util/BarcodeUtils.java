package it.gov.pagopa.pu.debtpositions.util;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BarcodeUtils {

  private static final String BASE64_PNG_PREFIX = "data:image/png;base64,";

  private static final float BAR_HEIGHT_MM = 14f;
  private static final float MODULE_WIDTH_MM = 0.25f;
  private static final float MM_TO_POINTS = 2.83465f;

  private static final float BAR_HEIGHT_POINTS = BAR_HEIGHT_MM * MM_TO_POINTS;
  private static final float MODULE_WIDTH_POINTS = MODULE_WIDTH_MM * MM_TO_POINTS;

  public static String generateCode128AsBase64(String content) {
    return generateCode128AsBase64(content, null);
  }

  public static String generateCode128AsBase64(String content, Float barHeight) {
    if (StringUtils.isBlank(content)) {
      return "";
    }

    float effectiveBarHeight = barHeight != null ? barHeight : BAR_HEIGHT_POINTS;

    // iText requires a PdfDocument to instantiate Barcode128, even though we only need the AWT image.
    // pdfBackingStream is a disposable stream used solely to satisfy this API requirement.
    try (ByteArrayOutputStream barcodeOutputStream = new ByteArrayOutputStream();
         ByteArrayOutputStream pdfBackingStream = new ByteArrayOutputStream();
         PdfWriter writer = new PdfWriter(pdfBackingStream);
         PdfDocument pdfDocument = new PdfDocument(writer)) {

      Barcode128 barcode = new Barcode128(pdfDocument);
      barcode.setCodeType(Barcode128.CODE128);
      barcode.setCode(content);
      barcode.setFont(null);
      barcode.setBarHeight(effectiveBarHeight);
      barcode.setX(MODULE_WIDTH_POINTS);

      Image awtImage = barcode.createAwtImage(Color.BLACK, Color.WHITE);
      BufferedImage bufferedImage = toBufferedImage(awtImage);

      ImageIO.write(bufferedImage, "PNG", barcodeOutputStream);
      return BASE64_PNG_PREFIX + Base64.getEncoder().encodeToString(barcodeOutputStream.toByteArray());

    } catch (Exception e) {
      // Returning empty string on failure: the caller (PDF template) will render without barcode
      // rather than failing the entire receipt generation.
      log.error("Error generating barcode for content: {}", content, e);
      return "";
    }
  }

  private static BufferedImage toBufferedImage(Image img) {
    if (img instanceof BufferedImage bufferedImage) {
      return bufferedImage;
    }

    BufferedImage bufferedImage = new BufferedImage(
      img.getWidth(null),
      img.getHeight(null),
      BufferedImage.TYPE_INT_ARGB
    );

    Graphics2D g2d = bufferedImage.createGraphics();
    g2d.drawImage(img, 0, 0, null);
    g2d.dispose();

    return bufferedImage;
  }
}
