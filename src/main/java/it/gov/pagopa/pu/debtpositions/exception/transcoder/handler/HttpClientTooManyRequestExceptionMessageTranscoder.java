package it.gov.pagopa.pu.debtpositions.exception.transcoder.handler;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionErrorDTO;
import it.gov.pagopa.pu.debtpositions.exception.transcoder.ExceptionMessageTranscoded;
import it.gov.pagopa.pu.debtpositions.exception.transcoder.ExceptionMessageTranscoder;
import org.springframework.web.client.HttpClientErrorException;

public class HttpClientTooManyRequestExceptionMessageTranscoder implements ExceptionMessageTranscoder<HttpClientErrorException.TooManyRequests> {
  @Override
  public ExceptionMessageTranscoded transcode(HttpClientErrorException.TooManyRequests tooManyRequestsException) {
    return new ExceptionMessageTranscoded(
      DebtPositionErrorDTO.CategoryEnum.DEBT_POSITION_TOO_MANY_REQUESTS.name(),
      tooManyRequestsException.getMessage(),
      null);
  }
}
