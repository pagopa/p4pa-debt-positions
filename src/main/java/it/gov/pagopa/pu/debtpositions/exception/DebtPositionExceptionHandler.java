package it.gov.pagopa.pu.debtpositions.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionErrorDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;


@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DebtPositionExceptionHandler {

  @ExceptionHandler({InvalidValueException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInternalError(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CodeEnum.BAD_REQUEST);
  }

  @ExceptionHandler({OperatorNotAuthorizedException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleForbiddenError(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.FORBIDDEN, DebtPositionErrorDTO.CodeEnum.FORBIDDEN);
  }

  @ExceptionHandler({NotFoundException.class, ResourceNotFoundException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleNotFoundError(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.NOT_FOUND, DebtPositionErrorDTO.CodeEnum.NOT_FOUND);
  }

  @ExceptionHandler({ConflictErrorException.class, DataIntegrityViolationException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleConflictError(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.CONFLICT, DebtPositionErrorDTO.CodeEnum.CONFLICT);
  }

  @ExceptionHandler({InvalidStatusTransitionException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInvalidStatusTransitionException(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CodeEnum.BAD_REQUEST);
  }


  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CodeEnum.BAD_REQUEST);
  }

  @ExceptionHandler({ServletException.class, ErrorResponseException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleServletException(Exception ex, HttpServletRequest request) {
    HttpStatusCode httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    DebtPositionErrorDTO.CodeEnum errorCode = DebtPositionErrorDTO.CodeEnum.GENERIC_ERROR;
    if (ex instanceof ErrorResponse errorResponse) {
      httpStatus = errorResponse.getStatusCode();
      if(httpStatus.isSameCodeAs(HttpStatus.NOT_FOUND)) {
        errorCode = DebtPositionErrorDTO.CodeEnum.NOT_FOUND;
      } else if (httpStatus.is4xxClientError()) {
        errorCode = DebtPositionErrorDTO.CodeEnum.BAD_REQUEST;
      }
    }
    return handleException(ex, request, httpStatus, errorCode);
  }

  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, DebtPositionErrorDTO.CodeEnum.GENERIC_ERROR);
  }

  static ResponseEntity<DebtPositionErrorDTO> handleException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus, DebtPositionErrorDTO.CodeEnum errorEnum) {
    logException(ex, request, httpStatus);

    String message = buildReturnedMessage(ex);

    return ResponseEntity
      .status(httpStatus)
      .body(new DebtPositionErrorDTO(errorEnum, message));
  }

  private static void logException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus) {
    log.info("A {} occurred handling request {}: HttpStatus {} - {}",
      ex.getClass(),
      getRequestDetails(request),
      httpStatus.value(),
      ex.getMessage());
    if(log.isDebugEnabled() && ex.getCause()!=null){
      log.debug("CausedBy: ", ex.getCause());
    }
  }

  private static String buildReturnedMessage(Exception ex) {
    if (ex instanceof HttpMessageNotReadableException) {
      if(ex.getCause() instanceof JsonMappingException jsonMappingException){
        return "Cannot parse body: " +
          jsonMappingException.getPath().stream()
            .map(JsonMappingException.Reference::getFieldName)
            .collect(Collectors.joining(".")) +
          ": " + jsonMappingException.getOriginalMessage();
      }
      return "Required request body is missing";
    } else if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
      return "Invalid request content:" +
        methodArgumentNotValidException.getBindingResult()
          .getAllErrors().stream()
          .map(e -> " " +
            (e instanceof FieldError fieldError? fieldError.getField(): e.getObjectName()) +
            ": " + e.getDefaultMessage())
          .sorted()
          .collect(Collectors.joining(";"));
    } else {
      return ex.getMessage();
    }
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }
}
