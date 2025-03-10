package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataExportsControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"data-export.installment-paid-view.max-months-interval=6"})
class DataExportsControllerImplTest {


  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private InstallmentService installmentServiceMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Value("${data-export.installment-paid-view.max-months-interval}")
  private Integer maxMonthsInterval;


  @Test
  void whenGetInstallmentExportThenOk() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);
    Long debtPositionTypeOrgId = 1L;

    PagedInstallmentsPaidView expectedResponse = podamFactory.manufacturePojo(PagedInstallmentsPaidView.class);

    Mockito.when(installmentServiceMock.getPagedInstallmentPaidView(eq(organizationId),
      eq(operatorExternalUserId),
      eq(paymentDateFrom),
      eq(paymentDateTo),
      eq(debtPositionTypeOrgId),
      any(PageRequest.class))).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/export/organization/{organizationId}/installments/paid", organizationId)
          .param("operatorExternalUserId",operatorExternalUserId)
          .param("paymentDateFrom", String.valueOf(paymentDateFrom))
          .param("paymentDateTo", String.valueOf(paymentDateTo))
          .param("debtPositionTypeOrgId", String.valueOf(debtPositionTypeOrgId))
          .param("size", "1"))
      .andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andReturn();

    PagedInstallmentsPaidView response = objectMapper.readValue(result.getResponse().getContentAsString(), PagedInstallmentsPaidView.class);
    TestUtils.reflectionEqualsByName(expectedResponse.getContent().getFirst(),response.getContent().getFirst(), "paymentDateTime");
    Mockito.verify(installmentServiceMock).getPagedInstallmentPaidView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, Pageable.ofSize(1));
  }

  @Test
  void whenGetInstallmentExportThenThrowException() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.parse("2025-03-06T17:05:04.685811Z");
    OffsetDateTime paymentDateTo = OffsetDateTime.parse("2025-11-06T17:05:04.686949700Z");
    Long debtPositionTypeOrgId = 1L;

    PagedInstallmentsPaidView expectedResponse = podamFactory.manufacturePojo(PagedInstallmentsPaidView.class);

    Mockito.when(installmentServiceMock.getPagedInstallmentPaidView(eq(organizationId),
      eq(operatorExternalUserId),
      eq(paymentDateFrom),
      eq(paymentDateTo),
      eq(debtPositionTypeOrgId),
      any(PageRequest.class))).thenReturn(expectedResponse);

    mockMvc.perform(
        MockMvcRequestBuilders.get("/export/organization/{organizationId}/installments/paid", organizationId)
          .param("operatorExternalUserId",operatorExternalUserId)
          .param("paymentDateFrom", String.valueOf(paymentDateFrom))
          .param("paymentDateTo", String.valueOf(paymentDateTo))
          .param("debtPositionTypeOrgId", String.valueOf(debtPositionTypeOrgId))
          .param("size", "1"))
      .andExpect(status().isBadRequest())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("The date interval between 2025-03-06T17:05:04.685811Z and 2025-11-06T17:05:04.686949700Z cannot exceed 6 months"))
      .andReturn();

  }
}
