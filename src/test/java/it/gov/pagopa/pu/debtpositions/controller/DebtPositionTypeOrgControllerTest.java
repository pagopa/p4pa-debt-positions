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
  void whenGetAppIONotificationThenOk() throws Exception {
    Long debtPositionTypeOrgId = 1L;

    AppIONotificationDTO expectedResult = new AppIONotificationDTO();
    Mockito.when(debtPositionTypeOrgService.getAppIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_CREATED)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-position-type-orgs/" + debtPositionTypeOrgId + "/notifications")
          .param("context", PaymentEventType.DP_CREATED.getValue())
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    AppIONotificationDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AppIONotificationDTO.class);
    assertEquals(expectedResult, resultResponse);
  }
}
