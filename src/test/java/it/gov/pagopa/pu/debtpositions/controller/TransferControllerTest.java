package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferReportedRequest;
import it.gov.pagopa.pu.debtpositions.service.TaxonomyValidatorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

  @MockitoBean
  private TaxonomyValidatorService taxonomyValidatorService;

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
    TransferReportedRequest request = new TransferReportedRequest("IUF");
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(debtPositionHierarchyStatusAlignerService.notifyReportedTransferId(transferId, request, accessToken))
      .thenReturn(Pair.of(buildDebtPositionDTO(), workflow));

    MvcResult result = mockMvc.perform(
        put("/transfers/1/reported")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(header().string("x-workflow-id", workflow.getWorkflowId()))
      .andExpect(header().string("x-run-id", workflow.getRunId()))
      .andReturn();

    DebtPositionDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void givenNoWorkflowIdWhenNotifyReportedTransferIdThenOk() throws Exception {
    Long transferId = 1L;
    TransferReportedRequest request = new TransferReportedRequest("IUF");

    Mockito.when(debtPositionHierarchyStatusAlignerService.notifyReportedTransferId(transferId, request, accessToken))
      .thenReturn(Pair.of(buildDebtPositionDTO(), null));

    MvcResult result = mockMvc.perform(
        put("/transfers/1/reported")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }


  @Test
  void whenValidateTaxonomyCategoryThenOk() throws Exception {
    Mockito.when(taxonomyValidatorService.validateTaxonomyCategory(anyString(), anyString()))
      .thenReturn(true);

    MvcResult result = mockMvc.perform(
        get("/transfers/taxonomy-category/validate")
          .queryParam("taxonomyCategory", "CATEGORY")
          .queryParam("orgFiscalCode", "orgFiscalCode")
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    Boolean resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), Boolean.class);
    assertTrue(resultResponse);
  }
}
