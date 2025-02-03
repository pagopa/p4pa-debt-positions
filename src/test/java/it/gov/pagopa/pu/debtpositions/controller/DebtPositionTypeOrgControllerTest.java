package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrgDTO;
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

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrgDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebtPositionTypeOrgControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class DebtPositionTypeOrgControllerTest {

  @MockitoBean
  private DebtPositionTypeOrgService debtPositionTypeOrgService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void whenNotifyReportedTransferIdThenOk() throws Exception {
    Mockito.when(debtPositionTypeOrgService.getDebtPositionTypeOrgByOrganizationIdAndCode(1L, "code")).thenReturn(buildDebtPositionTypeOrgDTO());

    MvcResult result = mockMvc.perform(
        get("/debt-position-type-org/1/code")
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionTypeOrgDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionTypeOrgDTO.class);
    assertEquals(buildDebtPositionTypeOrgDTO(), resultResponse);
  }
}
