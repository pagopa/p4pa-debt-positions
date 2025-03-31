package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init(){
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
  }

  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @Test
  void whenNotifyReportedTransferIdThenOk() throws Exception {
    Long transferId = 1L;

    Mockito.when(debtPositionHierarchyStatusAlignerService.notifyReportedTransferId(transferId, accessToken))
      .thenReturn(Pair.of(buildDebtPositionDTO(), "workflowId"));

    MvcResult result = mockMvc.perform(
        put("/transfers/1/reported")
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andExpect(header().string("x-workflow-id", "workflowId"))
      .andReturn();

    DebtPositionDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void givenNoWorkflowIdWhenNotifyReportedTransferIdThenOk() throws Exception {
    Long transferId = 1L;

    Mockito.when(debtPositionHierarchyStatusAlignerService.notifyReportedTransferId(transferId, accessToken))
      .thenReturn(Pair.of(buildDebtPositionDTO(), null));

    MvcResult result = mockMvc.perform(
        put("/transfers/1/reported")
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }
}
