package it.gov.pagopa.pu.debtpositions.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeOrgService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(DebtPositionTypeOrgControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class DebtPositionTypeOrgControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DebtPositionTypeOrgService debtPositionTypeOrgService;
  @MockitoBean
  private BalanceService balanceServiceMock;

  @Test
  void whenGetIONotificationThenOk() throws Exception {
    Long debtPositionTypeOrgId = 1L;

    IONotificationDTO expectedResult = new IONotificationDTO();
    Mockito.when(debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_CREATED)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-position-type-org/" + debtPositionTypeOrgId + "/io-notification/details")
          .param("context", PaymentEventType.DP_CREATED.getValue())
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    IONotificationDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), IONotificationDTO.class);
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void whenDeleteDebtPositionTypeOrgThenOk() throws Exception {
    Long debtPositionTypeOrgId = 1L;

    Mockito.doNothing().when(debtPositionTypeOrgService).deleteDebtPositionTypeOrg(debtPositionTypeOrgId);

    mockMvc.perform(
        delete("/debt-position-type-org/" + debtPositionTypeOrgId))
      .andExpect(status().isOk())
      .andReturn();
  }

  @Test
  void whenSaveDebtPositionTypeOrgThenOk() throws Exception {
    SaveDebtPositionTypeOrgDTO requestBody = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setDebtPositionTypeId(1L);
    debtPositionTypeOrg.setOrganizationId(1L);
    debtPositionTypeOrg.setCode("code");
    debtPositionTypeOrg.setDescription("description");
    debtPositionTypeOrg.setIban("iban");
    requestBody.setDebtPositionTypeOrg(debtPositionTypeOrg);
    DebtPositionTypeOrg expectedResult = new DebtPositionTypeOrg();
    Mockito.when(debtPositionTypeOrgService.saveDebtPositionTypeOrg(requestBody)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        post("/debt-position-type-org")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(requestBody)))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionTypeOrg response = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionTypeOrg.class);
    assertEquals(expectedResult,response);
  }

  @Test
  void whenUpdateFlagActiveDebtPositionTypeOrgThenOk() throws Exception {
    Long debtPositionTypeOrgId = 1L;

    Mockito.doNothing().when(debtPositionTypeOrgService).updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true);

    mockMvc.perform(
        patch("/debt-position-type-org/{debtPositionTypeOrgId}", debtPositionTypeOrgId)
          .param("flagActive", String.valueOf(true)))
      .andExpect(status().isOk())
      .andReturn();
  }
}
