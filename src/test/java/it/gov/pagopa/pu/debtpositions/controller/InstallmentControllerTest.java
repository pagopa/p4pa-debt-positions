package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentsSearchFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstallmentControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class InstallmentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

  @MockitoBean
  private InstallmentService installmentServiceMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @ParameterizedTest
  @ValueSource(strings = {"ORDINARY", "ORDINARY_SIL"})
  void whenGetInstallmentsByOrganizationIdAndNavThenOk(String debtPositionOrigin) throws Exception {
    //given
    List<InstallmentDTO> installmentDTOList = List.of(InstallmentFaker.buildInstallmentDTO());
    List<DebtPositionOrigin> originList = List.of(DebtPositionOrigin.valueOf(debtPositionOrigin));

    Mockito.when(installmentServiceMock.getInstallmentsByOrganizationIdAndNav(1L, "NAV", originList)).thenReturn(installmentDTOList);

    var builder = MockMvcRequestBuilders.get("/installments/{organizationId}/{nav}",1L,"NAV")
      .contentType(MediaType.APPLICATION_JSON_VALUE);
    if(debtPositionOrigin!=null)
      builder = builder.queryParam("debtPositionOrigin", originList.stream().map(Enum::name).toArray(String[]::new));
    MvcResult result = mockMvc.perform(builder)
      .andExpect(status().isOk())
      .andReturn();

    List<InstallmentDTO> resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
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

  @ParameterizedTest
  @ValueSource(strings = {"ORDINARY", "ORDINARY_SIL"})
  void whenGetInstallmentsByOrganizationIdAndReceiptIdThenOk(String debtPositionOrigin) throws Exception {
    //given
    List<InstallmentDTO> installmentDTOList = List.of(InstallmentFaker.buildInstallmentDTO());
    List<DebtPositionOrigin> originList = List.of(DebtPositionOrigin.valueOf(debtPositionOrigin));

    Mockito.when(installmentServiceMock.getInstallmentsByOrganizationIdAndReceiptId(1L, 999L, originList)).thenReturn(installmentDTOList);

    var builder = MockMvcRequestBuilders.get("/installments/by-organizationId-and-receiptId")
      .contentType(MediaType.APPLICATION_JSON_VALUE);
    builder.queryParam("organizationId", String.valueOf(1L));
    builder.queryParam("receiptId", String.valueOf(999L));
    if(debtPositionOrigin!=null)
      builder = builder.queryParam("debtPositionOrigin", originList.stream().map(Enum::name).toArray(String[]::new));
    MvcResult result = mockMvc.perform(builder)
      .andExpect(status().isOk())
      .andReturn();

    List<InstallmentDTO> resultResponse = jsonMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
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
    Mockito.verify(installmentServiceMock, Mockito.times(1)).getInstallmentsByOrganizationIdAndReceiptId(1L, 999L, originList);
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

    InstallmentDetailDTO response = jsonMapper.readValue(result.getResponse().getContentAsString(), InstallmentDetailDTO.class);
    TestUtils.reflectionEqualsByName(expectedResponse,response);

    Mockito.verify(installmentServiceMock).getInstallmentDetail(installmentId, operatorExternalUserId);
  }

  @Test
  void whenGetInstallmentsByIuvOrNavThenOk() throws Exception {
    String iuvOrNav = "iuvOrNav";
    String debtorFiscalCode = "debtorFiscalCode";
    Long organizationId = 1L;
    List<InstallmentDebtorDTO> expectedResponse = podamFactory.manufacturePojo(List.class,InstallmentDebtorDTO.class);
    List<InstallmentStatus> statuses = List.of(InstallmentStatus.PAID);

    Mockito.when(installmentServiceMock.getInstallmentsByIuvOrNav(iuvOrNav,debtorFiscalCode,organizationId, statuses)).thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/installments/debtor")
          .param("iuvOrNav",iuvOrNav)
          .param("organizationId",organizationId.toString())
          .param("statuses", "PAID")
          .header("X-fiscal-code",debtorFiscalCode))
      .andExpect(status().isOk())
      .andReturn();

    List<InstallmentDebtorDTO> response = jsonMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

    Assertions.assertEquals(expectedResponse,response);
  }

  @Test
  void whenGetInstallmentsByFiltersThenOk() throws Exception {
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    String iuv = "iuv";
    String iud = "iud";
    String fiscalCode = "fiscalCode";
    Long debtPositionTypeOrgId = 2L;
    List<DebtPositionOrigin> debtPositionOrigins = List.of(DebtPositionOrigin.ORDINARY);
    InstallmentStatus status = InstallmentStatus.UNPAID;
    LocalDate dueDateTimeFrom = LocalDate.now();
    LocalDate dueDateTimeTo = LocalDate.now().plusDays(10);
    Pageable pageable = PageRequest.of(0, 10);

    PagedInstallmentsView expectedResponse = podamFactory.manufacturePojo(PagedInstallmentsView.class);
    InstallmentsSearchFiltersDTO filtersDTO = new InstallmentsSearchFiltersDTO(
      organizationId,
      operatorExternalUserId,
      dueDateTimeFrom,
      dueDateTimeTo,
      iuv,
      iud,
      fiscalCode,
      debtPositionOrigins,
      debtPositionTypeOrgId,
      status
    );
    Mockito.when(installmentServiceMock
        .getPagedInstallmentsByFilters(filtersDTO, pageable))
      .thenReturn(expectedResponse);

    MvcResult result = mockMvc.perform(
        MockMvcRequestBuilders.get("/installments")
          .param("organizationId", String.valueOf(organizationId))
          .param("operatorExternalUserId", operatorExternalUserId)
          .param("dueDateTimeFrom", String.valueOf(dueDateTimeFrom))
          .param("dueDateTimeTo", String.valueOf(dueDateTimeTo))
          .param("iuv", iuv)
          .param("iud", iud)
          .param("fiscalCode", fiscalCode)
          .param("debtPositionOrigins", debtPositionOrigins.getFirst().name())
          .param("debtPositionTypeOrgId", String.valueOf(debtPositionTypeOrgId))
          .param("status", status.name())
          .param("size", String.valueOf(pageable.getPageSize())))
      .andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
      .andReturn();

    PagedInstallmentsView response = jsonMapper.readValue(result.getResponse().getContentAsString(), PagedInstallmentsView.class);

    Assertions.assertEquals(expectedResponse, response);
  }
}
