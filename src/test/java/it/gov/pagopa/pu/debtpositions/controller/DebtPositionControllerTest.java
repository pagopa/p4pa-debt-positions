package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.LocalDateTimeIntervalFilter;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import it.gov.pagopa.pu.debtpositions.service.TaxonomyValidatorService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.mixed.MixedDebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.delete.DebtPositionDeletionService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.InstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.PublishDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionManageInstallmentsService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.NullNode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildMixedDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.ManageDebtPositionFaker.buildManageDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebtPositionControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class DebtPositionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

  @MockitoBean
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  @MockitoBean
  private DebtPositionCreationService createDebtPositionService;

  @MockitoBean
  private DebtPositionService debtPositionService;

  @MockitoBean
  private InstallmentSynchronizeService installmentSynchronizerService;

  @MockitoBean
  private InstallmentService installmentService;

  @MockitoBean
  private DebtPositionManageInstallmentsService debtPositionManageInstallmentsService;

  @MockitoBean
  private DebtPositionDeletionService debtPositionDeletionService;

  @MockitoBean
  private PublishDebtPositionService publishDebtPositionService;

  @MockitoBean
  private MixedDebtPositionCreationService mixedDebtPositionCreationService;

  @MockitoBean
  private TaxonomyValidatorService taxonomyValidatorService;

  private static final LocalDate DATE = LocalDate.of(2099, 1, 1);
  private static final OffsetDateTime DATETIME = OffsetDateTime.of(DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);

  private final String accessToken = "ACCESSTOKEN";
  private final String userId = "USERID";

  @BeforeEach
  void init(){
    SecurityUtilsTest.configureSecurityContext(accessToken, userId);
  }

  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @Test
  void whenFinalizeSyncStatusThenOk() throws Exception {
    Long id = 1L;
    InstallmentStatus newStatus = InstallmentStatus.TO_SYNC;

    Map<String, SyncCompleteDTO> iupd2finalize = Map.of("iud", SyncCompleteDTO.builder()
      .newStatus(newStatus)
      .build());
    Map<String, SyncErrorDTO> iupdSyncError = Map.of("iud2", new SyncErrorDTO("SYNCERROR"));

    SyncStatusUpdateRequestDTO requestDTO = new SyncStatusUpdateRequestDTO(iupd2finalize, iupdSyncError);

    Mockito.when(debtPositionHierarchyStatusAlignerService.finalizeSyncStatus(id, requestDTO)).thenReturn(buildDebtPositionDTO());

    MvcResult result = mockMvc.perform(
        put("/debt-positions/1/finalize-sync-status")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(requestDTO)))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenCreateDebtPositionThenOk() throws Exception {
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    boolean massive = true;
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .build();
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(createDebtPositionService.createDebtPosition(debtPosition, wfExecutionParameters, accessToken, userId))
      .thenReturn(workflow);

    MvcResult result = mockMvc.perform(
        post("/debt-positions")
          .param("massive", String.valueOf(massive))
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(debtPosition)))
      .andExpect(status().isOk())
      .andExpect(header().string("x-workflow-id", workflow.getWorkflowId()))
      .andExpect(header().string("x-run-id", workflow.getRunId()))
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenCreateDraftDebtPositionThenOk() throws Exception {
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    debtPosition.setStatus(DebtPositionStatus.DRAFT);
    boolean massive = true;
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .build();

    Mockito.when(createDebtPositionService.createDebtPosition(debtPosition, wfExecutionParameters, accessToken, userId))
      .thenReturn(null);

    MvcResult result = mockMvc.perform(
        post("/debt-positions")
          .param("massive", String.valueOf(massive))
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(debtPosition)))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO().status(DebtPositionStatus.DRAFT), resultResponse);
  }

  @Test
  void whenCreateMixedDebtPositionThenOk() throws Exception {
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();

    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    Mockito.when(mixedDebtPositionCreationService.createMixedDebtPosition(mixedDebtPositionDTO, accessToken, userId))
      .thenReturn(Pair.of(workflow, debtPositionDTO));

    MvcResult result = mockMvc.perform(
        post("/debt-positions/mixed")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(mixedDebtPositionDTO)))
      .andExpect(status().isOk())
      .andExpect(header().string("x-workflow-id", workflow.getWorkflowId()))
      .andExpect(header().string("x-run-id", workflow.getRunId()))
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(debtPositionDTO, resultResponse);
  }

  @Test
  void givenNullWorkflowWhenCreateMixedDebtPositionThenOk() throws Exception {
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();

    Mockito.when(mixedDebtPositionCreationService.createMixedDebtPosition(mixedDebtPositionDTO, accessToken, userId))
      .thenReturn(Pair.of(null, buildDebtPositionDTO()));

    MvcResult result = mockMvc.perform(
        post("/debt-positions/mixed")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(mixedDebtPositionDTO)))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenCheckAndUpdateInstallmentExpirationThenOk() throws Exception {
    Long id = 1L;
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(debtPositionHierarchyStatusAlignerService.checkAndUpdateInstallmentExpiration(id, accessToken))
      .thenReturn(Pair.of(buildDebtPositionDTO(), workflow));

    MvcResult result = mockMvc.perform(
        put("/debt-positions/1/check-installment-expiration")
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andExpect(header().string("x-workflow-id", workflow.getWorkflowId()))
      .andExpect(header().string("x-run-id", workflow.getRunId()))
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenGetDebtPositionThenOk() throws Exception {
    Long debtPositionId = 1L;

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    Mockito.when(debtPositionService.getDebtPosition(debtPositionId)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/" + debtPositionId)
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void whenGetDebtPositionByInstallmentIdThenOk() throws Exception {
    Long installmentId = 1L;

    DebtPositionDTO expectedResult = new DebtPositionDTO();
    Mockito.when(debtPositionService.getDebtPositionByInstallmentId(installmentId)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/by-installmentId/" + installmentId)
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void whenGetDebtPositionsByOrganizationIdAndIuvThenOk() throws Exception {
    Long organizationId = 1L;
    String iuv = "12345678901234567";

    List<DebtPositionDTO> expectedResult = List.of(new DebtPositionDTO());
    Mockito.when(debtPositionService.getDebtPositionsByOrganizationIdAndIuv(organizationId, iuv, null)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/by-iuv/" + organizationId + "/" + iuv)
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    List<DebtPositionDTO> resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ArrayList<DebtPositionDTO>>(){});
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void whenGetDebtPositionsByOrganizationIdAndIudThenOk() throws Exception {
    Long organizationId = 1L;
    String iud = "123456789012345678";

    List<DebtPositionDTO> expectedResult = List.of(new DebtPositionDTO());
    Mockito.when(debtPositionService.getDebtPositionsByOrganizationIdAndIud(organizationId, iud, null)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/by-iud/" + organizationId + "/" + iud)
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    List<DebtPositionDTO> resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ArrayList<DebtPositionDTO>>(){});
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void whenInstallmentSynchronizeThenOk() throws Exception {
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    Boolean massive = true;
    Boolean partialChange = false;
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .partialChange(partialChange)
      .executionConfig(NullNode.instance)
      .build();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(installmentSynchronizerService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, userId))
      .thenReturn(workflow);

    mockMvc.perform(
        put("/debt-positions/installment-synchronize")
          .param("massive", String.valueOf(massive))
          .param("partialChange", String.valueOf(partialChange))
          .param("origin", String.valueOf(debtPositionOrigin))
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(installmentSynchronizeDTO)))
      .andExpect(status().isOk())
      .andExpect(header().string("x-workflow-id", workflow.getWorkflowId()))
      .andExpect(header().string("x-run-id", workflow.getRunId()))
      .andReturn();
  }

  @Test
  void whenDraftInstallmentSynchronizeThenOk() throws Exception {
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    Boolean massive = true;
    Boolean partialChange = false;
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .partialChange(partialChange)
      .executionConfig(NullNode.instance)
      .build();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;

    Mockito.when(installmentSynchronizerService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, userId))
      .thenReturn(null);

    mockMvc.perform(
        put("/debt-positions/installment-synchronize")
          .param("massive", String.valueOf(massive))
          .param("partialChange", String.valueOf(partialChange))
          .param("origin", String.valueOf(debtPositionOrigin))
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(installmentSynchronizeDTO)))
      .andExpect(status().isOk())
      .andReturn();
  }

  @Test
  void whenGetDebtPositionsByIngestionFlowFileIdThenOk() throws Exception {
    Long ingestionFlowFileId = 1L;

    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder().size(1L).build();
    Mockito.when(debtPositionService.getPagedDebtPositionsByIngestionFlowFileId(ingestionFlowFileId, null, Pageable.ofSize(1))).thenReturn(expectedPagedDebtPositions);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/ingestion-flow-file/" + ingestionFlowFileId)
          .param("size", "1")
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    PagedDebtPositions resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), PagedDebtPositions.class);
    assertEquals(expectedPagedDebtPositions, resultResponse);
  }

  @Test
  void whenUpdateInstallmentNotificationDateThenOk() throws Exception {
    UpdateInstallmentNotificationDateRequest request = UpdateInstallmentNotificationDateRequest.builder()
      .debtPositionId(1L)
      .nav(Collections.singletonList("123456789123"))
      .notificationDate(DATETIME)
      .build();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(installmentService.updateInstallmentNotificationDate(request, wfExecutionParameters, userId, accessToken))
      .thenReturn(workflow);

    mockMvc.perform(
        put("/debt-positions/update-installment-notification-date")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(header().string("x-workflow-id", workflow.getWorkflowId()))
      .andExpect(header().string("x-run-id", workflow.getRunId()))
      .andReturn();
  }

  @Test
  void whenManageDebtPositionInstallmentsThenOk() throws Exception {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO request = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, request, wfExecutionParameters, accessToken, userId))
      .thenReturn(Pair.of(buildDebtPositionDTO(), workflow));

    MvcResult result = mockMvc.perform(
        put("/debt-positions/" + debtPositionId + "/manage-installments")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(header().string("x-workflow-id", workflow.getWorkflowId()))
      .andExpect(header().string("x-run-id", workflow.getRunId()))
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenManageDraftDebtPositionInstallmentsThenOk() throws Exception {
    Long debtPositionId = 1L;
    ManageDebtPositionDTO request = buildManageDebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();

    Mockito.when(debtPositionManageInstallmentsService.manageDebtPositionInstallments(debtPositionId, request, wfExecutionParameters, accessToken, userId))
      .thenReturn(Pair.of(buildDebtPositionDTO().status(DebtPositionStatus.DRAFT), null));

    MvcResult result = mockMvc.perform(
        put("/debt-positions/" + debtPositionId + "/manage-installments")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO().status(DebtPositionStatus.DRAFT), resultResponse);
  }

  @Test
  void whenUpdateInstallmentNotificationFeeThenOk()
    throws Exception {
    ActualizeAmountRequestDTO request = ActualizeAmountRequestDTO.builder()
      .organizationId(0L)
      .nav("NAV")
      .newFeeCents(1L)
      .actualizedFromPuSil(true)
      .notificationDate(OffsetDateTime.now())
      .iun("IUN")
      .balance("BALANCE")
      .build();

    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();

    InstallmentDTO installmentDTO = InstallmentDTO.builder().build();

    Mockito.when(installmentService.updateInstallmentNotificationFee(request,
      wfExecutionParameters, accessToken, userId)).thenReturn(installmentDTO);

    mockMvc.perform(
        put("/debt-positions/update-notification-fee")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn();
  }

  @Test
  void whenDeleteDebtPositionThenOk() throws Exception {
    Long debtPositionId = 1L;
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(debtPositionDeletionService.deleteDebtPosition(debtPositionId, accessToken, userId))
      .thenReturn(workflow);

    mockMvc.perform(
        delete("/debt-positions/" + debtPositionId)
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andExpect(header().string("x-workflow-id", workflow.getWorkflowId()))
      .andExpect(header().string("x-run-id", workflow.getRunId()))
      .andReturn();
  }

  @Test
  void whenDeleteDraftDebtPositionThenOk() throws Exception {
    Long debtPositionId = 1L;

    Mockito.when(debtPositionDeletionService.deleteDebtPosition(debtPositionId, accessToken, userId))
      .thenReturn(null);

    mockMvc.perform(
        delete("/debt-positions/" + debtPositionId)
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isNoContent())
      .andReturn();
  }

  @Test
  void whenPublishDebtPositionThenOk() throws Exception {
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    Long debtPositionId = 1L;
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .build();
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.when(publishDebtPositionService.publishDebtPosition(debtPositionId, wfExecutionParameters, accessToken, userId))
      .thenReturn(Pair.of(debtPosition, workflow));

    MvcResult result = mockMvc.perform(
        put("/debt-positions/1/publish")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(jsonMapper.writeValueAsString(debtPosition)))
      .andExpect(status().isOk())
      .andExpect(header().string("x-workflow-id", workflow.getWorkflowId()))
      .andExpect(header().string("x-run-id", workflow.getRunId()))
      .andReturn();

    DebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), DebtPositionDTO.class);
    assertEquals(buildDebtPositionDTO(), resultResponse);
  }

  @Test
  void whenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenOk() throws Exception {
    PersonEntityType debtorEntityType = PersonEntityType.F;
    String debtorFiscalCode = "fiscalcode";
    List<Long> organizationIds = List.of(1L);
    LocalDateTimeIntervalFilter dateTimeIntervalFilter = new LocalDateTimeIntervalFilter(null, null);

    List<DebtPositionDTO> expectedResult = List.of(new DebtPositionDTO());
    Mockito.when(debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(debtorFiscalCode, debtorEntityType, null, null, null, organizationIds, dateTimeIntervalFilter)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/by-debtor/" + debtorFiscalCode + "/" + debtorEntityType.getValue())
          .queryParam("organizationIds", organizationIds.stream().map(String::valueOf).toArray(String[]::new))
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    List<DebtPositionDTO> resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ArrayList<DebtPositionDTO>>(){});
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void whenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeAndDatesThenOk() throws Exception {
    PersonEntityType debtorEntityType = PersonEntityType.F;
    String debtorFiscalCode = "fiscalcode";
    List<Long> organizationIds = List.of(1L);
    OffsetDateTime fromDate = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    OffsetDateTime toDate = OffsetDateTime.of(2025, 12, 31, 23, 59, 59, 999000000, ZoneOffset.UTC);
    LocalDateTimeIntervalFilter dateTimeIntervalFilter = new LocalDateTimeIntervalFilter(
      Utilities.offsetDateTimeToLocalDateTime(fromDate),
      Utilities.offsetDateTimeToLocalDateTime(toDate));

    List<DebtPositionDTO> expectedResult = List.of(new DebtPositionDTO());
    Mockito.when(debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(debtorFiscalCode, debtorEntityType, null, null, null, organizationIds, dateTimeIntervalFilter)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/by-debtor/" + debtorFiscalCode + "/" + debtorEntityType.getValue())
          .queryParam("organizationIds", organizationIds.stream().map(String::valueOf).toArray(String[]::new))
          .queryParam("dateFrom", fromDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
          .queryParam("dateTo", toDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    List<DebtPositionDTO> resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ArrayList<DebtPositionDTO>>() {
    });
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void givenOrgIdsWhenGetPagedDebtorUnpaidDebtPositionsThenOk() throws Exception {
    List<Long> organizationIds = List.of(1L);
    String debtorFiscalCode = "debtorFiscalCode";
    PagedDebtorUnpaidDebtPositionDTO expectedResult = new PagedDebtorUnpaidDebtPositionDTO();

    Mockito.when(debtPositionService.getPagedDebtorUnpaidDebtPosition(debtorFiscalCode, organizationIds, Pageable.ofSize(1))).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
      get("/debt-positions/debtor/unpaid")
        .header("X-fiscal-code", debtorFiscalCode)
        .queryParam("organizationIds", organizationIds.stream().map(String::valueOf).toArray(String[]::new))
        .param("size", "1")
        .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    PagedDebtorUnpaidDebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(),
      PagedDebtorUnpaidDebtPositionDTO.class);

    assertNotNull(resultResponse);
    assertEquals(expectedResult, resultResponse);
  }

  @Test
  void givenOrgIdWhenGetDebtorUnpaidDebtPositionOverviewThenOk() throws Exception {
    Long debtPositionId = 1L;
    Long organizationId = 1L;
    String debtorFiscalCode = "debtorFiscalCode";
    DebtorDebtPositionDTO expectedResult = new DebtorDebtPositionDTO();

    Mockito.when(debtPositionService.getDebtorUnpaidDebtPositionOverview(debtPositionId, debtorFiscalCode, organizationId)).thenReturn(expectedResult);

    MvcResult result = mockMvc.perform(
        get("/debt-positions/debtor/unpaid/{debtPositionId}/overview", debtPositionId)
          .header("X-fiscal-code", debtorFiscalCode)
          .queryParam("organizationId", organizationId.toString())
          .contentType(MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andReturn();

    DebtorDebtPositionDTO resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(),
      DebtorDebtPositionDTO.class);

    assertNotNull(resultResponse);
    assertEquals(expectedResult, resultResponse);
  }

}
