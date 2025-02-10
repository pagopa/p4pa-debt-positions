package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.co.jemos.podam.api.PodamFactory;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceiptControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceiptControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ReceiptService receiptServiceMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void whenCreateReceiptThenOk() throws Exception {
    //given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    ReceiptDTO expectedResponse = podamFactory.manufacturePojo(ReceiptDTO.class);

    Mockito.when(receiptServiceMock.createReceipt(Mockito.argThat(r -> receiptDTO.getReceiptId().equals(r.getReceiptId())), Mockito.any())).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.post("/receipts")
          .content(objectMapper.writeValueAsString(receiptDTO))
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    ReceiptDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
    });
    TestUtils.reflectionEqualsByName(expectedResponse, resultResponse, "receiptId", "creationDate", "updateDate");

    Mockito.verify(receiptServiceMock, Mockito.times(1)).createReceipt(
      Mockito.argThat(r -> receiptDTO.getReceiptId().equals(r.getReceiptId())),
      Mockito.any());
  }
}
