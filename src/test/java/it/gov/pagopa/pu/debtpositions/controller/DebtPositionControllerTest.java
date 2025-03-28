package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.InstallmentSynchronizeService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebtPositionControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class DebtPositionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  @MockitoBean
  private DebtPositionCreationService createDebtPositionService;

  @MockitoBean
  private DebtPositionService debtPositionService;

  @MockitoBean
  private InstallmentSynchronizeService installmentSynchronizerService;

  private String accesstoken;
  private String userId;

  @BeforeEach
  void init(){
    accesstoken = "ACCESSTOKEN";
    userId = "USERID";
    SecurityUtilsTest.configureSecurityContext(accesstoken, userId);
  }

  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @Test
  void whenFinalizeSyncStatusThenOk() throws Exception {
    Long id = 1L;
    InstallmentStatus newStatus = InstallmentStatus.TO_SYNC;

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .build();

    syncStatusDTO.put("iud", iupdSyncStatusUpdateDTO);

    Mockito.when(debtPositionHierarchyStatusAlignerService.finalizeSyncStatus(id, syncStatusDTO)).thenReturn(buildDebtPositionDTO());

    MvcResult result = mockMvc.perform(
        put("/debt-positions/1/finalize-sync-status")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(syncStatusDTO)))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenCreateDebtPositionThenOk() throws Exception {
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    boolean massive = true;
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .build();

    Mockito.when(createDebtPositionService.createDebtPosition(debtPosition, wfExecutionParameters, accesstoken, userId))
      .thenReturn(Pair.of(buildDebtPositionDTO(), "workflowId"));

    MvcResult result = mockMvc.perform(
        post("/debt-positions")
          .param("massive", String.valueOf(massive))
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(debtPosition)))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenCheckAndUpdateInstallmentExpirationThenOk() throws Exception {
    Long id = 1L;

    Mockito.when(debtPositionHierarchyStatusAlignerService.checkAndUpdateInstallmentExpiration(id, accesstoken)).thenReturn(buildDebtPositionDTO());

    MvcResult result = mockMvc.perform(
        put("/debt-positions/1/check-installment-expiration")
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenGetDebtPositionThenOk() throws Exception {
    Long debtPositionId = 1L;

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    Mockito.when(debtPositionService.getDebtPosition(debtPositionId)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/"+debtPositionId)
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void whenInstallmentSynchronizeThenOk() throws Exception {
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    Boolean massive = true;
    Boolean partialChange=false;
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .partialChange(partialChange)
      .executionConfig(NullNode.instance)
      .build();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;

    Mockito.when(installmentSynchronizerService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accesstoken, userId))
      .thenReturn("workflowId");

    mockMvc.perform(
        put("/debt-positions/installment-synchronize")
          .param("massive", String.valueOf(massive))
          .param("partialChange", String.valueOf(partialChange))
          .param("origin", String.valueOf(debtPositionOrigin))
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(installmentSynchronizeDTO)))
      .andExpect(status().isCreated())
      .andExpect(header().string("x-workflow-id", "workflowId"))
      .andReturn();
  }

  @Test
  void whenGetDebtPositionsByIngestionFlowFileIdThenOk() throws Exception {
    Long ingestionFlowFileId = 1L;

    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder().size(1L).build();
    Mockito.when(debtPositionService.getPagedDebtPositionsByIngestionFlowFileId(ingestionFlowFileId, Pageable.ofSize(1))).thenReturn(expectedPagedDebtPositions);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/ingestion-flow-file/"+ingestionFlowFileId)
          .param("size", "1")
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    PagedDebtPositions resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), PagedDebtPositions.class);
    assertEquals(expectedPagedDebtPositions, resultResponse);
  }
}
