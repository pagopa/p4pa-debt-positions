package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.dto.filters.ExportPaidInstallmentsFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.filters.LocalDateTimeIntervalFilter;
import it.gov.pagopa.pu.debtpositions.dto.filters.OffsetDateTimeIntervalFilter;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.json.JsonMapper;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataExportsControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"data-export.installment-paid-view.max-months-interval=6", "data-export.receipt-archiving-view.max-months-interval=6"})
class DataExportsControllerImplTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

  @MockitoBean
  private InstallmentService installmentServiceMock;

  @MockitoBean
  private ReceiptService receiptServiceMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenValidParamsWithPaymentDate_whenGetInstallmentExport_thenOk() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateTimeFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTimeTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);

    Long debtPositionTypeOrgId = 1L;

    OffsetDateTimeIntervalFilter offsetDateTimeIntervalFilter = new OffsetDateTimeIntervalFilter(paymentDateTimeFrom, paymentDateTimeTo);
    ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO = new ExportPaidInstallmentsFiltersDTO(organizationId, operatorExternalUserId, offsetDateTimeIntervalFilter, new LocalDateTimeIntervalFilter(), debtPositionTypeOrgId, null);
    PagedInstallmentsPaidView expectedResponse = podamFactory.manufacturePojo(PagedInstallmentsPaidView.class);

    Mockito.when(installmentServiceMock.getPagedInstallmentPaidView(eq(exportPaidInstallmentsFiltersDTO),
      any(PageRequest.class))).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/export/organization/{organizationId}/installments/paid", organizationId)
          .param("operatorExternalUserId",operatorExternalUserId)
          .param("paymentDateTimeFrom", String.valueOf(paymentDateTimeFrom))
          .param("paymentDateTimeTo", String.valueOf(paymentDateTimeTo))
          .param("debtPositionTypeOrgId", String.valueOf(debtPositionTypeOrgId))
          .param("size", "1"))
      .andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andReturn();

    PagedInstallmentsPaidView response = jsonMapper.readValue(result.getResponse().getContentAsString(), PagedInstallmentsPaidView.class);
    TestUtils.reflectionEqualsByName(expectedResponse.getContent().getFirst(),response.getContent().getFirst(), "paymentDateTime");
    Mockito.verify(installmentServiceMock).getPagedInstallmentPaidView(exportPaidInstallmentsFiltersDTO, Pageable.ofSize(1));
  }

  @Test
  void givenValidParamsWithInstallmentUpdateDateTime_whenGetInstallmentExport_thenOk() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime installmentUpdateDateTimeFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime installmentUpdateDateTimeTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);

    Long debtPositionTypeOrgId = 1L;

    LocalDateTimeIntervalFilter localDateTimeIntervalFilter = new LocalDateTimeIntervalFilter(Utilities.toLocalDateTime(installmentUpdateDateTimeFrom), Utilities.toLocalDateTime(installmentUpdateDateTimeTo));
    ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO = new ExportPaidInstallmentsFiltersDTO(organizationId, operatorExternalUserId, new OffsetDateTimeIntervalFilter(), localDateTimeIntervalFilter, debtPositionTypeOrgId, null);
    PagedInstallmentsPaidView expectedResponse = podamFactory.manufacturePojo(PagedInstallmentsPaidView.class);

    Mockito.when(installmentServiceMock.getPagedInstallmentPaidView(eq(exportPaidInstallmentsFiltersDTO),
      any(PageRequest.class))).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/export/organization/{organizationId}/installments/paid", organizationId)
          .param("operatorExternalUserId",operatorExternalUserId)
          .param("installmentUpdateDateTimeFrom", String.valueOf(installmentUpdateDateTimeFrom))
          .param("installmentUpdateDateTimeTo", String.valueOf(installmentUpdateDateTimeTo))
          .param("debtPositionTypeOrgId", String.valueOf(debtPositionTypeOrgId))
          .param("size", "1"))
      .andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andReturn();

    PagedInstallmentsPaidView response = jsonMapper.readValue(result.getResponse().getContentAsString(), PagedInstallmentsPaidView.class);
    TestUtils.reflectionEqualsByName(expectedResponse.getContent().getFirst(),response.getContent().getFirst(), "installmentUpdateDateTime");
    Mockito.verify(installmentServiceMock).getPagedInstallmentPaidView(exportPaidInstallmentsFiltersDTO, Pageable.ofSize(1));
  }

  @Test
  void givenBothDateTime_whenGetInstallmentExport_thenThrowException() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime installmentUpdateDateTimeFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime installmentUpdateDateTimeTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);

    Long debtPositionTypeOrgId = 1L;

    OffsetDateTimeIntervalFilter paymentDateTimeIntervalFilter = new OffsetDateTimeIntervalFilter(paymentDateFrom, paymentDateTo);
    LocalDateTimeIntervalFilter localDateTimeIntervalFilter = new LocalDateTimeIntervalFilter(Utilities.toLocalDateTime(installmentUpdateDateTimeFrom), Utilities.toLocalDateTime(installmentUpdateDateTimeTo));
    ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO = new ExportPaidInstallmentsFiltersDTO(organizationId, operatorExternalUserId, paymentDateTimeIntervalFilter, localDateTimeIntervalFilter, debtPositionTypeOrgId, null);
    PagedInstallmentsPaidView expectedResponse = podamFactory.manufacturePojo(PagedInstallmentsPaidView.class);

    Mockito.when(installmentServiceMock.getPagedInstallmentPaidView(eq(exportPaidInstallmentsFiltersDTO),
      any(PageRequest.class))).thenReturn(expectedResponse);

    mockMvc.perform(
        MockMvcRequestBuilders.get("/export/organization/{organizationId}/installments/paid", organizationId)
          .param("operatorExternalUserId",operatorExternalUserId)
          .param("paymentDateTimeFrom", String.valueOf(paymentDateFrom))
          .param("paymentDateTimeTo", String.valueOf(paymentDateTo))
          .param("installmentUpdateDateTimeFrom", String.valueOf(installmentUpdateDateTimeFrom))
          .param("installmentUpdateDateTimeTo", String.valueOf(installmentUpdateDateTimeTo))
          .param("debtPositionTypeOrgId", String.valueOf(debtPositionTypeOrgId))
          .param("size", "1"))
      .andExpect(status().isBadRequest())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[INVALID_DATE_FILTER_COMBINATION] You must provide only one of the following date ranges: either the payment date range (paymentDateTimeFrom and paymentDateTimeTo) or the installment update date range (installmentUpdateDateTimeFrom and installmentUpdateDateTimeTo). Providing both or neither is not allowed"))
      .andReturn();

  }

  @Test
  void givenWrongPaymentDateTimeInterval_whenGetInstallmentExport_thenThrowException() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateTimeFrom = OffsetDateTime.parse("2025-03-06T17:05:04.685811Z");
    OffsetDateTime paymentDateTimeTo = OffsetDateTime.parse("2025-11-06T17:05:04.686949700Z");
    Long debtPositionTypeOrgId = 1L;

    OffsetDateTimeIntervalFilter offsetDateTimeIntervalFilter = new OffsetDateTimeIntervalFilter(paymentDateTimeFrom, paymentDateTimeTo);
    ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO = new ExportPaidInstallmentsFiltersDTO(organizationId, operatorExternalUserId, offsetDateTimeIntervalFilter, null, debtPositionTypeOrgId, null);
    PagedInstallmentsPaidView expectedResponse = podamFactory.manufacturePojo(PagedInstallmentsPaidView.class);

    Mockito.when(installmentServiceMock.getPagedInstallmentPaidView(eq(exportPaidInstallmentsFiltersDTO),
      any(PageRequest.class))).thenReturn(expectedResponse);

    mockMvc.perform(
        MockMvcRequestBuilders.get("/export/organization/{organizationId}/installments/paid", organizationId)
          .param("operatorExternalUserId",operatorExternalUserId)
          .param("paymentDateTimeFrom", String.valueOf(paymentDateTimeFrom))
          .param("paymentDateTimeTo", String.valueOf(paymentDateTimeTo))
          .param("debtPositionTypeOrgId", String.valueOf(debtPositionTypeOrgId))
          .param("size", "1"))
      .andExpect(status().isBadRequest())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[INVALID_DATE_FILTER_INTERVAL] The date interval between 2025-03-06T17:05:04.685811Z and 2025-11-06T17:05:04.686949700Z cannot exceed 6 months"))
      .andReturn();

  }

  @Test
  void givenWrongInstallmentUpdateDateTimeInterval_whenGetInstallmentExport_thenThrowException() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime installmentUpdateDateTimeFrom = OffsetDateTime.parse("2025-03-06T17:05:04.685811Z");
    OffsetDateTime installmentUpdateDateTimeTo = OffsetDateTime.parse("2025-11-06T17:05:04.686949700Z");
    Long debtPositionTypeOrgId = 1L;

    LocalDateTimeIntervalFilter localDateTimeIntervalFilter = new LocalDateTimeIntervalFilter(Utilities.toLocalDateTime(installmentUpdateDateTimeFrom), Utilities.toLocalDateTime(installmentUpdateDateTimeTo));
    ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO = new ExportPaidInstallmentsFiltersDTO(organizationId, operatorExternalUserId, new OffsetDateTimeIntervalFilter(), localDateTimeIntervalFilter, debtPositionTypeOrgId, null);
    PagedInstallmentsPaidView expectedResponse = podamFactory.manufacturePojo(PagedInstallmentsPaidView.class);

    Mockito.when(installmentServiceMock.getPagedInstallmentPaidView(eq(exportPaidInstallmentsFiltersDTO),
      any(PageRequest.class))).thenReturn(expectedResponse);

    mockMvc.perform(
        MockMvcRequestBuilders.get("/export/organization/{organizationId}/installments/paid", organizationId)
          .param("operatorExternalUserId",operatorExternalUserId)
          .param("installmentUpdateDateTimeFrom", String.valueOf(installmentUpdateDateTimeFrom))
          .param("installmentUpdateDateTimeTo", String.valueOf(installmentUpdateDateTimeTo))
          .param("debtPositionTypeOrgId", String.valueOf(debtPositionTypeOrgId))
          .param("size", "1"))
      .andExpect(status().isBadRequest())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[INVALID_DATE_FILTER_INTERVAL] The date interval between 2025-03-06T17:05:04.685811Z and 2025-11-06T17:05:04.686949700Z cannot exceed 6 months"))
      .andReturn();

  }

  @Test
  void givenValidParams_whenGetReceiptExport_thenOk() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);

    PagedReceiptsArchivingView expectedResponse = podamFactory.manufacturePojo(PagedReceiptsArchivingView.class);

    Mockito.when(receiptServiceMock.getPagedReceiptArchivingView(eq(organizationId),
      eq(operatorExternalUserId),
      eq(paymentDateFrom),
      eq(paymentDateTo),
      any(PageRequest.class))).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/export/organization/{organizationId}/receipts/archiving", organizationId)
          .param("operatorExternalUserId",operatorExternalUserId)
          .param("paymentDateFrom", String.valueOf(paymentDateFrom))
          .param("paymentDateTo", String.valueOf(paymentDateTo))
          .param("size", "1"))
      .andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andReturn();

    PagedReceiptsArchivingView response = jsonMapper.readValue(result.getResponse().getContentAsString(), PagedReceiptsArchivingView.class);
    TestUtils.reflectionEqualsByName(expectedResponse.getContent().getFirst(),response.getContent().getFirst(), "paymentDateTime");
    Mockito.verify(receiptServiceMock).getPagedReceiptArchivingView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, Pageable.ofSize(1));
  }

  @Test
  void givenWrongDateTimeInterval_whenGetReceiptExport_ThenThrowException() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.parse("2025-03-06T17:05:04.685811Z");
    OffsetDateTime paymentDateTo = OffsetDateTime.parse("2025-11-06T17:05:04.686949700Z");
    Long debtPositionTypeOrgId = 1L;

    PagedReceiptsArchivingView expectedResponse = podamFactory.manufacturePojo(PagedReceiptsArchivingView.class);

    Mockito.when(receiptServiceMock.getPagedReceiptArchivingView(eq(organizationId),
      eq(operatorExternalUserId),
      eq(paymentDateFrom),
      eq(paymentDateTo),
      any(PageRequest.class))).thenReturn(expectedResponse);

    mockMvc.perform(
        MockMvcRequestBuilders.get("/export/organization/{organizationId}/receipts/archiving", organizationId)
          .param("operatorExternalUserId",operatorExternalUserId)
          .param("paymentDateFrom", String.valueOf(paymentDateFrom))
          .param("paymentDateTo", String.valueOf(paymentDateTo))
          .param("debtPositionTypeOrgId", String.valueOf(debtPositionTypeOrgId))
          .param("size", "1"))
      .andExpect(status().isBadRequest())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[INVALID_DATE_FILTER_INTERVAL] The date interval between 2025-03-06T17:05:04.685811Z and 2025-11-06T17:05:04.686949700Z cannot exceed 6 months"))
      .andReturn();

  }
}
