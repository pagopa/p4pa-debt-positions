package it.gov.pagopa.pu.debtpositions.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.config.json.JsonConfig;
import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.*;
import it.gov.pagopa.pu.debtpositions.exception.transcoder.handler.ConstraintViolationExceptionMessageTranscoderTest;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.UtilitiesTest;
import jakarta.persistence.RollbackException;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.exception.transcoder.handler.ConstraintViolationExceptionMessageTranscoderTest.EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith({SpringExtension.class})
@WebMvcTest(value = {DebtPositionExceptionHandlerTest.TestController.class, DebtPositionExceptionHandlerTest.TestCrudController.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
  DebtPositionExceptionHandlerTest.TestController.class,
  DebtPositionExceptionHandlerTest.TestCrudController.class,
  DebtPositionExceptionHandler.class,
  JsonConfig.class})
class DebtPositionExceptionHandlerTest {

  public static final String DATA = "data";
  public static final TestRequestBody BODY = new TestRequestBody("bodyData", null, "abc", LocalDateTime.now());

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @MockitoSpyBean
  private TestController testControllerSpy;
  @MockitoSpyBean
  private TestCrudController testCrudControllerSpy;
  @MockitoSpyBean
  private RequestMappingHandlerAdapter requestMappingHandlerAdapterSpy;

  @RestController
  @Slf4j
  static class TestController {
    @PostMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    String testEndpoint(@RequestParam(DATA) String data, @Valid @RequestBody TestRequestBody body) {
      return "OK";
    }
  }

  @RestController
  @Slf4j
  static class TestCrudController {
    @GetMapping(value = "/crud/installments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    String testCrudEndpoint(@PathVariable("id") Long id) {
      return "OK";
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestRequestBody {
    @NotNull
    private String requiredField;
    private String notRequiredField;
    @Pattern(regexp = "[a-z]+")
    private String lowerCaseAlphabeticField;
    private LocalDateTime dateTimeField;
  }

  private final String traceId = "TRACEID";

  @BeforeEach
  void init() {
    TestUtils.clearDefaultTimezone();
    UtilitiesTest.setTraceId(traceId);
  }

  @AfterEach
  void clearTraceId(){
    UtilitiesTest.clearTraceIdContext();
  }

  private ResultActions performRequest(String data, MediaType accept) throws Exception {
    return performRequest(data, accept, objectMapper.writeValueAsString(DebtPositionExceptionHandlerTest.BODY));
  }

  private ResultActions performRequest(String data, MediaType accept, String body) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/test")
      .param(DATA, data)
      .accept(accept);

    if (body != null) {
      requestBuilder
        .contentType(MediaType.APPLICATION_JSON)
        .content(body);
    }

    return mockMvc.perform(requestBuilder);
  }


  @Test
  void handleInvalidValueExceptionError() throws Exception {
    doThrow(new InvalidValueException("ERRORCODE", "Error", List.of(new ErrorFieldDTO().field("fieldName").error("fieldError").message("fieldErrorMessage")))).when(testControllerSpy).testEndpoint(DATA,BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].field").value("fieldName"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].error").value("fieldError"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].message").value("fieldErrorMessage"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleForbiddenErrorExceptionError() throws Exception {
    doThrow(new OperatorNotAuthorizedException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isForbidden())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_FORBIDDEN"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleGenericErrorExceptionError() throws Exception {
    doThrow(new ConflictErrorException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleNotFoundException() throws Exception {
    doThrow(new NotFoundException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleInvalidStatusTransitionExceptionError() throws Exception {
    doThrow(new InvalidStatusTransitionException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInvalidInstallmentStatusExceptionError() throws Exception {
    doThrow(new InvalidInstallmentStatusException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleMissingServletRequestParameterException() throws Exception {

    performRequest(null, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_BAD_REQUEST] Required request parameter 'data' for method parameter type String is not present"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].field").value("data"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].error").value("NotNull"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].message").value("Required request parameter 'data' for method parameter type String is not present"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

  }

  @Test
  void handleRuntimeExceptionError() throws Exception {
    doThrow(new RuntimeException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_GENERIC_ERROR] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleGenericServletException() throws Exception {
    doThrow(new ServletException("Error"))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_GENERIC_ERROR] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handle4xxHttpServletException() throws Exception {
    performRequest(DATA, MediaType.parseMediaType("application/hal+json"))
      .andExpect(MockMvcResultMatchers.status().isNotAcceptable())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_BAD_REQUEST] No acceptable representation"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleUrlNotFound() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/NOTEXISTENTURL"))
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_NOT_FOUND] No static resource NOTEXISTENTURL for request '/NOTEXISTENTURL'."))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleResourceNotFoundException() throws Exception {
    doThrow(new ResourceNotFoundException("Error"))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_NOT_FOUND] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleDataIntegrityViolationException() throws Exception {
    doThrow(new DataIntegrityViolationException("Error"))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_CONFLICT] Conflict."))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleDataIntegrityViolationHibernateException() throws Exception {
    doThrow(new DataIntegrityViolationException("Error", new org.hibernate.exception.ConstraintViolationException("ERROR", new SQLException("SQLEXCEPTION"), "UNIQUE")))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_CONFLICT] Conflict. SQLEXCEPTION"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleNoBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON, null)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_BAD_REQUEST] Required request body is missing"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleMalformedBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON,
      "{\"")
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_BAD_REQUEST] Cannot parse body. Unexpected end-of-input: was expecting closing '\"' for name"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInvalidBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON,
      "{\"notRequiredField\":\"notRequired\",\"lowerCaseAlphabeticField\":\"ABC\"}")
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_BAD_REQUEST] Invalid request content. lowerCaseAlphabeticField: must match \"[a-z]+\"; requiredField: must not be null"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].field").value("lowerCaseAlphabeticField"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].error").value("Pattern"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].message").value("must match \"[a-z]+\""))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[1].field").value("requiredField"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[1].error").value("NotNull"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[1].message").value("must not be null"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleNotParsableBodyException() throws Exception {
    performRequest(DATA, MediaType.APPLICATION_JSON,
      "{\"notRequiredField\":\"notRequired\",\"dateTimeField\":\"2025-02-05\"}")
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_BAD_REQUEST] Cannot parse body. dateTimeField: Text '2025-02-05' could not be parsed at index 10"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].field").value("dateTimeField"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].error").value("DateTimeParse"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].message").value("Text '2025-02-05' could not be parsed at index 10"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handle5xxHttpServletException() throws Exception {
    doThrow(new ServerErrorException("Error", new RuntimeException("Error")))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_GENERIC_ERROR] 500 INTERNAL_SERVER_ERROR \"Error\""))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleHttpClientErrorTooManyRequestsException() throws Exception {
    doThrow(HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS, "TooManyRequests", null, null, null))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isTooManyRequests())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_TOO_MANY_REQUESTS"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_TOO_MANY_REQUESTS"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_TOO_MANY_REQUESTS] 429 TooManyRequests"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  private final ConstraintViolationException constraintViolationException = ConstraintViolationExceptionMessageTranscoderTest.buildConstraintViolationException();
  @Test
  void handleViolationException() throws Exception {
    doThrow(constraintViolationException).when(testControllerSpy).testEndpoint(DATA, BODY);

    assertConstraintViolationException();
  }

  private void assertConstraintViolationException() throws Exception {
    ResultActions resultActions = performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_BAD_REQUEST] " + EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED.getMessage()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));

    for (int i = 0; i < EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED.getFields().size(); i++) {
      ErrorFieldDTO errorFieldDTO = EXPECTED_CONSTRAINT_EXCEPTION_MESSAGE_TRANSCODED.getFields().get(i);
      resultActions
        .andExpect(MockMvcResultMatchers.jsonPath("$.fields[" + i + "].field").value(errorFieldDTO.getField()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.fields[" + i + "].error").value(errorFieldDTO.getError()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.fields[" + i + "].message").value(errorFieldDTO.getMessage()));
    }
  }

  @Test
  void handleHttpHostConnectionExceptionException() throws Exception {
    doThrow(new RuntimeException("connection refused", new HttpHostConnectException("error"))).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_CONNECTION_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_CONNECTION_ERROR] connection refused"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleTransactionException_invalidData() throws Exception {
    doThrow(new TransactionSystemException("TransactionError", new RollbackException("rollbackException", constraintViolationException)))
      .when(testControllerSpy).testEndpoint(DATA, BODY);

    assertConstraintViolationException();
  }

  @Test
  void handleTransactionException_unexpected() throws Exception {
    doThrow(new TransactionSystemException("TransactionError", new RuntimeException()))
      .when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_GENERIC_ERROR] TransactionError"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleTooManyElementsException() throws Exception {
    doThrow(new ExportTooManyRecordsException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
    void handleInvalidDateTimeIntervalException() throws Exception {
    doThrow(new InvalidDateTimeIntervalException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleWorkflowErrorException() throws Exception {
    doThrow(new WorkflowErrorException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInvalidParamException() throws Exception {
    doThrow(new InvalidParamException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInvalidConditionException() throws Exception {
    doThrow(new InvalidConditionException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isPreconditionFailed())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleCrudResourceNotFoundException() throws Exception {
    Long id = -12L;
    doThrow(new ResourceNotFoundException()).when(testCrudControllerSpy).testCrudEndpoint(id);

    mockMvc.perform(MockMvcRequestBuilders.get("/crud/installments/-12"))
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("INSTALLMENT_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[INSTALLMENT_NOT_FOUND] EntityRepresentationModel not found"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleCrudResourceBaseBusinessException() throws Exception {
    Long id = -12L;
    doThrow(new InvalidValueException("ERROR", "message", List.of(new ErrorFieldDTO().field("fieldName").error("resolvedError").message("resolved message")))).when(testCrudControllerSpy).testCrudEndpoint(id);

    mockMvc.perform(MockMvcRequestBuilders.get("/crud/installments/-12"))
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERROR] message"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].field").value("fieldName"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].error").value("resolvedError"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields[0].message").value("resolved message"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleCrudResourceCauseBaseBusinessException() throws Exception {
    Long id = -12L;
    doThrow(new IllegalArgumentException(new InvalidValueException("ERROR", "message"))).when(testCrudControllerSpy).testCrudEndpoint(id);

    mockMvc.perform(MockMvcRequestBuilders.get("/crud/installments/-12"))
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERROR] message"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleInstallmentCloningErrorException() throws Exception {
    doThrow(new InstallmentCloningException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_GENERIC_ERROR"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[ERRORCODE] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleCannotAcquireLockException() throws Exception {
    doThrow(new CannotAcquireLockException("Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isTooManyRequests())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_TOO_MANY_REQUESTS"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_TOO_MANY_REQUESTS"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[DEBT_POSITION_TOO_MANY_REQUESTS] Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }
}
