package it.gov.pagopa.pu.debtpositions.exception;

import it.gov.pagopa.pu.debtpositions.dto.generated.CreateErrorDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.WorkflowInvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.WorkflowNotAuthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkflowExceptionHandler {

  @ExceptionHandler({WorkflowInvalidValueException.class})
  public ResponseEntity<CreateErrorDTO> handleNotFoundWorkflowError(RuntimeException ex, HttpServletRequest request){
    return handleWorkflowErrorException(ex, request, HttpStatus.BAD_REQUEST, CreateErrorDTO.CodeEnum.BAD_REQUEST);
  }

  @ExceptionHandler({WorkflowNotAuthorizedException.class})
  public ResponseEntity<CreateErrorDTO> handleInternalError(RuntimeException ex, HttpServletRequest request){
    return handleWorkflowErrorException(ex, request, HttpStatus.FORBIDDEN, CreateErrorDTO.CodeEnum.FORBIDDEN);
  }

  static ResponseEntity<CreateErrorDTO> handleWorkflowErrorException(RuntimeException ex, HttpServletRequest request, HttpStatus httpStatus, CreateErrorDTO.CodeEnum errorEnum) {
    String message = logException(ex, request, httpStatus);

    return ResponseEntity
      .status(httpStatus)
      .body(new CreateErrorDTO(errorEnum, message));
  }

  private static String logException(RuntimeException ex, HttpServletRequest request, HttpStatus httpStatus) {
    String message = ex.getMessage();
    log.info("A {} occurred handling request {}: HttpStatus {} - {}",
      ex.getClass(),
      getRequestDetails(request),
      httpStatus.value(),
      message);
    return message;
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }
}
