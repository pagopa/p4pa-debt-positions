package it.gov.pagopa.pu.debtpositions.util;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DocumentCompositionTest {
  @Spy
  private Configuration cfg;
  private DocumentComposition documentComposition;

  @BeforeEach
  void setup() throws IOException {
    String html = "<html><head><link rel=\"stylesheet\" href=\"styles/index.css\"/></head><body>Test ${testValue}</body></html>";
    StringTemplateLoader loader = new StringTemplateLoader();
    loader.putTemplate("RECEIPT", html);
    cfg.setTemplateLoader(loader);
    Mockito.doNothing().when(cfg).setTemplateLoader(Mockito.any());
    documentComposition = new DocumentComposition(cfg);
  }

  @Test
  void testExecuteTextTemplate() throws IOException, TemplateException {
    Map<String, Object> model = new HashMap<>();
    model.put("testValue", "textValue");

    String result = documentComposition.executeTextTemplate(DocumentComposition.TemplateType.RECEIPT, model);

    assertTrue(result.contains("Test textValue"));
  }

  @Test
  void testExecutePdfTemplate() throws IOException, TemplateException {
    Map<String, Object> model = new HashMap<>();
    model.put("testValue", "pdfValue");

    byte[] result = documentComposition.executePdfTemplate(DocumentComposition.TemplateType.RECEIPT, model);

    assertNotNull(result);
    assertTrue(result.length > 0);
  }
}
