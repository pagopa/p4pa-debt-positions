package it.gov.pagopa.pu.debtpositions.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstallmentControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class InstallmentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

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

    InstallmentDetailDTO response = objectMapper.readValue(result.getResponse().getContentAsString(), InstallmentDetailDTO.class);
    TestUtils.reflectionEqualsByName(expectedResponse,response);

    Mockito.verify(installmentServiceMock).getInstallmentDetail(installmentId, operatorExternalUserId);
  }

}
