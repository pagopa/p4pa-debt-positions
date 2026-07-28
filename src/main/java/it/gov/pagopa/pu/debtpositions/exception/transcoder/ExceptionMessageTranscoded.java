package it.gov.pagopa.pu.debtpositions.exception.transcoder;

import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;

import java.util.List;

public record ExceptionMessageTranscoded(
  String code,
  String message,
  List<ErrorFieldDTO> fields
) {}
