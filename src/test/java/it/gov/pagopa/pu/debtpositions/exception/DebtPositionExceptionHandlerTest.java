package it.gov.pagopa.pu.debtpositions.exception;

import it.gov.pagopa.pu.debtpositions.exception.custom.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.doThrow;

@ExtendWith({SpringExtension.class})
@WebMvcTest(value = {DebtPositionExceptionHandlerTest.TestController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
  DebtPositionExceptionHandlerTest.TestController.class,
  DebtPositionExceptionHandler.class})
public class DebtPositionExceptionHandlerTest {

  public static final String DATA = "data";
  @Autowired
  private MockMvc mockMvc;

  @MockitoSpyBean
  private TestController testControllerSpy;

  @RestController
  @Slf4j
  static class TestController {

    @GetMapping("/test")
    String testEndpoint(@RequestParam(DATA) String data) {
      return "OK";
    }
  }

  @Test
  void handleInvalidValueExceptionError() throws Exception {
    doThrow(new InvalidValueException("Error")).when(testControllerSpy).testEndpoint(DATA);

    mockMvc.perform(MockMvcRequestBuilders.get("/test")
        .param(DATA, DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"));

  }

  @Test
  void handleForbiddenErrorExceptionError() throws Exception {
    doThrow(new OperatorNotAuthorizedException("Error")).when(testControllerSpy).testEndpoint(DATA);

    mockMvc.perform(MockMvcRequestBuilders.get("/test")
        .param(DATA, DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isForbidden())
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_FORBIDDEN"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"));

  }

  @Test
  void handleGenericErrorExceptionError() throws Exception {
    doThrow(new ConflictErrorException("Error")).when(testControllerSpy).testEndpoint(DATA);

    mockMvc.perform(MockMvcRequestBuilders.get("/test")
        .param(DATA, DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"));

  }

  @Test
  void handleInvalidStatusTransitionExceptionError() throws Exception {
    doThrow(new InvalidStatusTransitionException("Error")).when(testControllerSpy).testEndpoint(DATA);

    mockMvc.perform(MockMvcRequestBuilders.get("/test")
        .param(DATA, DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_BAD_REQUEST"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"));
  }

  @Test
  void handleDebtPositionNotFoundExceptionError() throws Exception {
    doThrow(new DebtPositionNotFoundException("Error")).when(testControllerSpy).testEndpoint(DATA);

    mockMvc.perform(MockMvcRequestBuilders.get("/test")
        .param(DATA, DATA)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("DEBT_POSITION_NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"));
  }
}
