package it.gov.pagopa.pu.debtpositions.exception;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionErrorDTO;
import it.gov.pagopa.pu.debtpositions.exception.common.CommonExceptionHandler;
import it.gov.pagopa.pu.debtpositions.exception.custom.*;
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
public class DebtPositionExceptionHandler extends CommonExceptionHandler {

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

  @ExceptionHandler({InstallmentCloningException.class})
  public ResponseEntity<DebtPositionErrorDTO> handleInstallmentCloningException(InstallmentCloningException ex, HttpServletRequest request) {
    return handleException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_GENERIC_ERROR);
  }

}
