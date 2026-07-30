package it.gov.pagopa.pu.debtpositions.exception.transcoder.handler;

import it.gov.pagopa.pu.debtpositions.exception.transcoder.ExceptionMessageTranscoded;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultExceptionMessageTranscoderTest {

  private final DefaultExceptionMessageTranscoder transcoder = new DefaultExceptionMessageTranscoder();

  @Test
  void testTranscode() {
    // Given
    Exception exception = new Exception("test");

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(null, exception.getMessage(), null),
      result);
  }

  @Test
  void givenHttpHostConnectExceptionCauseWhenTranscodeThenOk() {
    // Given
    Exception exception = new Exception("test", new HttpHostConnectException(""));

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded("DEBT_POSITION_CONNECTION_ERROR", exception.getMessage(), null),
      result);
  }
}
