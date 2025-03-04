package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeOrgService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebtPositionTypeOrgControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class DebtPositionTypeOrgControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DebtPositionTypeOrgService debtPositionTypeOrgService;

  @Test
  void whenGetIONotificationThenOk() throws Exception {
    Long debtPositionTypeOrgId = 1L;

    IONotificationDTO expectedResult = new IONotificationDTO();
    Mockito.when(debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, IONotificationOperationType.CREATE_DP)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-position-type-org/" + debtPositionTypeOrgId + "/io-notification/details")
          .param("context", IONotificationOperationType.CREATE_DP.getValue())
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    IONotificationDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), IONotificationDTO.class);
    assertEquals(expectedResult, resultResponse);
  }
}
