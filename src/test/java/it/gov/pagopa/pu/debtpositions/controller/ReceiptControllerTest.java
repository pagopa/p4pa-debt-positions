package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.dto.FileResourceDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.CreateReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.ReceiptFileService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import uk.co.jemos.podam.api.PodamFactory;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceiptControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceiptControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

  @MockitoBean
  private CreateReceiptService createReceiptServiceMock;

  @MockitoBean
  private ReceiptService receiptServiceMock;

  @MockitoBean
  private ReceiptFileService receiptFileServiceMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();


  @Test
  void whenCreateReceiptThenOk() throws Exception {
    //given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    ReceiptDTO expectedResponse = podamFactory.manufacturePojo(ReceiptDTO.class);

    Mockito.when(createReceiptServiceMock.createReceipt(Mockito.argThat(r -> receiptDTO.getReceiptId().equals(r.getReceiptId())), Mockito.any())).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.post("/receipts")
          .content(jsonMapper.writeValueAsString(receiptDTO))
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    ReceiptDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
    });
    TestUtils.reflectionEqualsByName(expectedResponse, resultResponse, "receiptId", "creationDate", "updateDate", "noPII");

    Mockito.verify(createReceiptServiceMock, Mockito.times(1)).createReceipt(
      Mockito.argThat(r -> receiptDTO.getReceiptId().equals(r.getReceiptId())),
      Mockito.any());
  }

  @Test
  void whenGetReceiptThenOk() throws Exception {
    //given
    Long receiptId = 1L;
    ReceiptDTO expectedResponse = podamFactory.manufacturePojo(ReceiptDTO.class);

    Mockito.when(receiptServiceMock.getReceipt(receiptId)).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/receipts/"+receiptId))
      .andExpect(status().isOk())
      .andReturn();

    ReceiptDTO response = jsonMapper.readValue(result.getResponse().getContentAsString(), ReceiptDTO.class);
    TestUtils.reflectionEqualsByName(expectedResponse,response, "noPII");

    Mockito.verify(receiptServiceMock).getReceipt(receiptId);
  }

  @Test
  void whenGetReceiptDetailThenOk() throws Exception {
    //given
    Long organizationId = 1L;
    Long receiptId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    String iud = "iud";
    ReceiptDetailDTO expectedResponse = podamFactory.manufacturePojo(ReceiptDetailDTO.class);

    Mockito.when(receiptServiceMock.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud)).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/receipts/"+receiptId+"/detail")
          .param("operatorExternalUserId", operatorExternalUserId)
          .param("organizationId", organizationId.toString())
          .param("iud", iud))
      .andExpect(status().isOk())
      .andReturn();

    ReceiptDetailDTO response = jsonMapper.readValue(result.getResponse().getContentAsString(), ReceiptDetailDTO.class);
    TestUtils.reflectionEqualsByName(expectedResponse,response);

    Mockito.verify(receiptServiceMock).getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud);
  }

  @Test
  void whenGetReceiptPdfThenOk() throws Exception {
    // GIVEN
    Long organizationId = 1L;
    Long receiptId = 99L;
    byte[] pdfBytes = "PDF_CONTENT".getBytes(StandardCharsets.UTF_8);
    FileResourceDTO expectedResult = new FileResourceDTO(new ByteArrayResource(pdfBytes),
      "RECEIPT_CFENTE_"+receiptId+".pdf");

    Mockito.when(receiptFileServiceMock.generateReceiptPdf(receiptId, organizationId)).thenReturn(expectedResult);

    String urlPattern = "/receipts/{receiptId}/pdf";
    String expectedFileName = "RECEIPT_CFENTE_"+receiptId+".pdf";

    mockMvc.perform(
        MockMvcRequestBuilders.get(urlPattern, receiptId)
          .param("organizationId", organizationId.toString()))

      .andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PDF_VALUE))
      .andExpect(MockMvcResultMatchers.header().string(
        HttpHeaders.CONTENT_DISPOSITION,
        Matchers.containsString("attachment; filename=\"" + expectedFileName + "\"")
      ))
      .andExpect(MockMvcResultMatchers.content().bytes(pdfBytes))
      .andReturn();

    Mockito.verify(receiptFileServiceMock, Mockito.times(1)).generateReceiptPdf(receiptId, organizationId);
  }
}
