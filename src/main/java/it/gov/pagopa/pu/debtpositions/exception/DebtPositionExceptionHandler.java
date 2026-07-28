package it.gov.pagopa.pu.debtpositions.exception;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionErrorDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.*;
import it.gov.pagopa.pu.debtpositions.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.debtpositions.exception.transcoder.ExceptionMessageTranscoderService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import jakarta.persistence.RollbackException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DebtPositionExceptionHandler {

  private static final String ERROR_MESSAGE_FORMAT = "[%s] %s";

  private static final ExceptionMessageTranscoderService exceptionMessageTranscoderService = new ExceptionMessageTranscoderService();

  @ExceptionHandler({InvalidValueException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInternalError(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST);
  }

  @ExceptionHandler({OperatorNotAuthorizedException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleForbiddenError(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.FORBIDDEN, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_FORBIDDEN);
  }

  @ExceptionHandler({NotFoundException.class, ResourceNotFoundException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleNotFoundError(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.NOT_FOUND, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_NOT_FOUND);
  }

  @ExceptionHandler({ConflictErrorException.class, DataIntegrityViolationException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleConflictError(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.CONFLICT, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_CONFLICT);
  }

  @ExceptionHandler({InvalidStatusTransitionException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInvalidStatusTransitionException(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST);
  }

  @ExceptionHandler({InvalidInstallmentStatusException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInvalidInstallmentStatusException(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST);
  }

  @ExceptionHandler({ExportTooManyRecordsException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleExportTooManyRecordsException(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST);
  }

  @ExceptionHandler({InvalidDateTimeIntervalException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInvalidDateTimeIntervalException(RuntimeException ex, HttpServletRequest request){
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST);
  }

  @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, ConversionFailedException.class, ConversionFailedException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleViolationException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST);
  }

  @ExceptionHandler({WorkflowErrorException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleWorkflowErrorException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_GENERIC_ERROR);
  }

  @ExceptionHandler({InvalidParamException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInvalidParamException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.BAD_REQUEST, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST);
  }

  @ExceptionHandler({InvalidConditionException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInvalidConditionException(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.PRECONDITION_FAILED, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST);
  }

  @ExceptionHandler({HttpClientErrorException.TooManyRequests.class, CannotAcquireLockException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInvokedHttpClientTooManyRequestsError(Exception ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.TOO_MANY_REQUESTS, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_TOO_MANY_REQUESTS);
  }

  @ExceptionHandler({ServletException.class, ErrorResponseException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleServletException(Exception ex, HttpServletRequest request) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    DebtPositionErrorDTO.CategoryEnum errorCode = DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_GENERIC_ERROR;
    if (ex instanceof ErrorResponse errorResponse) {
      httpStatus = HttpStatus.valueOf((errorResponse.getStatusCode().value()));
      if(httpStatus.isSameCodeAs(HttpStatus.NOT_FOUND)) {
        errorCode = DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_NOT_FOUND;
      } else if (httpStatus.is4xxClientError()) {
        errorCode = DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST;
      }
    }
    return handleException(ex, request, httpStatus, errorCode);
  }


  @ExceptionHandler({TransactionException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleTransactionException(TransactionException ex, HttpServletRequest request) {
    if (ex.getCause() instanceof RollbackException rollbackException && rollbackException.getCause() instanceof ValidationException validationException) {
      return handleViolationException(validationException, request);
    } else {
      return handleRuntimeException(ex, request);
    }
  }

  @ExceptionHandler({RuntimeException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_GENERIC_ERROR);
  }

  @ExceptionHandler({InstallmentCloningException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInstallmentCloningException(InstallmentCloningException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_GENERIC_ERROR);
  }

  static ResponseEntity<DebtPositionErrorDTO> handleException(Exception ex, HttpServletRequest request, HttpStatus httpStatus, DebtPositionErrorDTO.CategoryEnum errorEnum) {
    logException(ex, request, httpStatus);

    ExceptionMessageTranscoded code2message = Optional.of(request.getRequestURI())
      .filter(path -> path.contains("/crud/"))
      .map(path -> buildCrudErrorMessage(path, httpStatus, ex))
      .orElseGet(() -> buildReturnedMessage(ex));

    String code = Objects.requireNonNullElse(code2message.code(), errorEnum.getValue());
    String message = code2message.message();
    List<ErrorFieldDTO> fields = code2message.fields();

    return ResponseEntity
      .status(httpStatus)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new DebtPositionErrorDTO(errorEnum, code, String.format(ERROR_MESSAGE_FORMAT, code, message), fields, Utilities.getTraceId()));
  }

  private static void logException(Exception ex, HttpServletRequest request, HttpStatusCode httpStatus) {
    boolean printStackTrace = httpStatus.is5xxServerError();
    Level logLevel = printStackTrace ? Level.ERROR : Level.INFO;
    log.makeLoggingEventBuilder(logLevel)
      .log("A {} occurred handling request {}: HttpStatus {} - {}",
        ex.getClass(),
        getRequestDetails(request),
        httpStatus.value(),
        ex.getMessage(),
        printStackTrace ? ex : null
      );
    if (!printStackTrace && log.isDebugEnabled() && ex.getCause() != null) {
      log.debug("CausedBy: ", ex.getCause());
    }
  }

  private static ExceptionMessageTranscoded buildReturnedMessage(Exception ex) {
    return exceptionMessageTranscoderService.transcode(ex);
  }

  private static ExceptionMessageTranscoded buildCrudErrorMessage(String requestPath, HttpStatus httpStatus, Exception ex) {
    if(ex instanceof BaseBusinessException) {
      return buildReturnedMessage(ex);
    } else if (ex.getCause() instanceof BaseBusinessException causeBusinessException) {
      return buildReturnedMessage(causeBusinessException);
    }
    String entity = requestPath.split("/crud/")[1].split("/")[0].replaceAll("s$", "");
    String entityCode = entity.replace("-", "_").toUpperCase();
    ExceptionMessageTranscoded error = buildReturnedMessage(ex);
    return new ExceptionMessageTranscoded(entityCode + "_" + httpStatus.name(), error.message(), error.fields());
  }

  static String getRequestDetails(HttpServletRequest request) {
    return "%s %s".formatted(request.getMethod(), request.getRequestURI());
  }

}
