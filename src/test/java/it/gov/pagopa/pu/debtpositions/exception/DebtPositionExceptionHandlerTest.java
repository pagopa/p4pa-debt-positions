package it.gov.pagopa.pu.debtpositions.exception;

import it.gov.pagopa.pu.debtpositions.exception.common.CommonExceptionHandlerTest;
import it.gov.pagopa.pu.debtpositions.exception.custom.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.doThrow;

class DebtPositionExceptionHandlerTest extends CommonExceptionHandlerTest {

  @Test
  void handleOperatorNotAuthorizedException() throws Exception {
    doThrow(new OperatorNotAuthorizedException("ERRORCODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isForbidden())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("DEBT_POSITION_FORBIDDEN"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ERRORCODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }
}
