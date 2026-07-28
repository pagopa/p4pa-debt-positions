package it.gov.pagopa.pu.debtpositions.exception.transcoder.handler;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionErrorDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.debtpositions.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.debtpositions.exception.transcoder.ExceptionMessageTranscoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MethodArgumentNotValidExceptionMessageTranscoder implements ExceptionMessageTranscoder<MethodArgumentNotValidException> {

  @Override
  public ExceptionMessageTranscoded transcode(MethodArgumentNotValidException methodArgumentNotValidException) {
    List<ErrorFieldDTO> errorFields = methodArgumentNotValidException.getBindingResult()
      .getAllErrors().stream()
      .map(e -> (ErrorFieldDTO)ErrorFieldDTO.builder()
        .field(e instanceof FieldError fieldError ? fieldError.getField() : e.getObjectName())
        .error(e.getCode())
        .message(e.getDefaultMessage())
        .build()
      )
      .sorted(Comparator.comparing(ErrorFieldDTO::getField))
      .toList();

    String errorDescription = errorFields.stream()
      .map(e -> " " + e.getField() + ": " + e.getMessage())
      .collect(Collectors.joining(";"));

    return new ExceptionMessageTranscoded(
      DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_BAD_REQUEST.name(),
      "Invalid request content." + errorDescription,
      errorFields
    );
  }
}
