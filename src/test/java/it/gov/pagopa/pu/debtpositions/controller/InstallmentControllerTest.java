package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstallmentControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"data-export.installment-paid-view.max-months-interval=6"})
class InstallmentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private InstallmentService installmentServiceMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Value("${data-export.installment-paid-view.max-months-interval}")
  private Integer maxMonthsInterval;

  @ParameterizedTest
  @ValueSource(strings = {"ORDINARY", "ORDINARY_SIL"})
  @NullSource
  void whenGetInstallmentsByOrganizationIdAndNavThenOk(String debtPositionOrigin) throws Exception {
    //given
    List<InstallmentDTO> installmentDTOList = podamFactory.manufacturePojo(List.class, InstallmentDTO.class);
    List<DebtPositionOrigin> originList = debtPositionOrigin==null?null:List.of(DebtPositionOrigin.valueOf(debtPositionOrigin));

    Mockito.when(installmentServiceMock.getInstallmentsByOrganizationIdAndNav(1L, "NAV", originList)).thenReturn(installmentDTOList);

    var builder = MockMvcRequestBuilders.get("/installments/{organizationId}/{nav}",1L,"NAV")
      .contentType(MediaType.APPLICATION_JSON_VALUE);
    if(debtPositionOrigin!=null)
      builder = builder.queryParam("debtPositionOrigin", originList.stream().map(Enum::name).toArray(String[]::new));
    MvcResult result = mockMvc.perform(builder)
      .andExpect(status().isOk())
      .andReturn();

    List<InstallmentDTO> resultResponse = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
    });
    for(int idx = 0; idx < installmentDTOList.size(); idx++) {
      InstallmentDTO resultElem = resultResponse.get(idx);
      InstallmentDTO expectedElem = installmentDTOList.get(idx);
      Assertions.assertTrue(EqualsBuilder.reflectionEquals(expectedElem, resultElem, false, null, true,
        "transfers", "notificationDate","dueDate", "creationDate", "updateDate"), "Error on element " + idx);
      Assertions.assertEquals(expectedElem.getDueDate(), resultElem.getDueDate());
      Assertions.assertEquals(expectedElem.getNotificationDate().toInstant(), resultElem.getNotificationDate().toInstant());
      Assertions.assertEquals(expectedElem.getCreationDate().toInstant(), resultElem.getCreationDate().toInstant());
      Assertions.assertEquals(expectedElem.getUpdateDate().toInstant(), resultElem.getUpdateDate().toInstant());
      Assertions.assertIterableEquals(expectedElem.getTransfers(), resultElem.getTransfers());
    }
    Mockito.verify(installmentServiceMock, Mockito.times(1)).getInstallmentsByOrganizationIdAndNav(1L, "NAV", originList);
  }

  @Test
  void whenGetInstallmentDetailThenOk() throws Exception {
    Long installmentId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentDetailDTO expectedResponse = podamFactory.manufacturePojo(InstallmentDetailDTO.class);

    Mockito.when(installmentServiceMock.getInstallmentDetail(installmentId, operatorExternalUserId)).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/installments/"+installmentId)
          .param("operatorExternalUserId",operatorExternalUserId))
      .andExpect(status().isOk())
      .andReturn();

    InstallmentDetailDTO response = objectMapper.readValue(result.getResponse().getContentAsString(), InstallmentDetailDTO.class);
    TestUtils.reflectionEqualsByName(expectedResponse,response);

    Mockito.verify(installmentServiceMock).getInstallmentDetail(installmentId, operatorExternalUserId);
  }

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
