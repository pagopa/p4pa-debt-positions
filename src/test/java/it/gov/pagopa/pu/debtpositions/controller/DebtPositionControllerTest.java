package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.InstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

  @Test
  void whenFinalizeSyncStatusThenOk() throws Exception {
    Long id = 1L;
    InstallmentStatus newStatus = InstallmentStatus.TO_SYNC;

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa("iupdPagoPa")
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
    DebtPositionDTO inputDTO = buildDebtPositionDTO();
    Boolean massive = false;

    Mockito.when(createDebtPositionService.createDebtPosition(inputDTO, massive, null, null))
      .thenReturn(buildDebtPositionDTO());

    MvcResult result = mockMvc.perform(
        post("/debt-positions")
          .param("massive", String.valueOf(massive))
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(inputDTO)))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenCheckAndUpdateInstallmentExpirationThenOk() throws Exception {
    Long id = 1L;

    Mockito.when(debtPositionHierarchyStatusAlignerService.checkAndUpdateInstallmentExpiration(id)).thenReturn(buildDebtPositionDTO());

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
}
