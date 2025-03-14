package it.gov.pagopa.pu.debtpositions.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeDetailDTO;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(DebtPositionTypeControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class DebtPositionTypeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DebtPositionTypeService debtPositionTypeService;

  @Test
  void whenGetDebtPositionTypeDetailThenOk() throws Exception {
    Long debtPositionTypeId = 1L;
    Long brokerId = 1L;

    DebtPositionTypeDetailDTO expectedResult = new DebtPositionTypeDetailDTO();
    Mockito.when(debtPositionTypeService.getDebtPositionTypeDetail(debtPositionTypeId, brokerId, null))
      .thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-position-type/" + debtPositionTypeId)
          .queryParam("brokerId", String.valueOf(brokerId))
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionTypeDetailDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionTypeDetailDTO.class);
    assertEquals(expectedResult, resultResponse);
  }
}
