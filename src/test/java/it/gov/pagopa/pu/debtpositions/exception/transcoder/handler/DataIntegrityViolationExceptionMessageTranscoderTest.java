package it.gov.pagopa.pu.debtpositions.exception.transcoder.handler;

import it.gov.pagopa.pu.debtpositions.exception.transcoder.ExceptionMessageTranscoded;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

class DataIntegrityViolationExceptionMessageTranscoderTest {

  private final DataIntegrityViolationExceptionMessageTranscoder transcoder = new DataIntegrityViolationExceptionMessageTranscoder();

  @Test
  void testTranscode() {
    // Given
    DataIntegrityViolationException exception = new DataIntegrityViolationException("message");

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "DEBT_POSITION_BAD_REQUEST",
        "Data integrity violation.",
        null),
      result);
  }

  @Test
  void givenSQLConstraintViolationExceptionCauseWhenTranscodeThenBadRequest() {
    // Given
    SQLException sqlException = new SQLException("sqlErrorMessage", "22001");
    DataIntegrityViolationException exception = new DataIntegrityViolationException("message", new ConstraintViolationException("message", sqlException, "constraintName"));

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "DEBT_POSITION_BAD_REQUEST",
        "Data integrity violation. "+sqlException.getMessage(),
        null),
      result);
  }

  @Test
  void givenUniqueConstraintViolationExceptionCauseWhenTranscodeThenConflict() {
    // Given
    SQLException sqlException = new SQLException("uniqueViolationMessage", "23505");
    DataIntegrityViolationException exception = new DataIntegrityViolationException("message", new ConstraintViolationException("message", sqlException, "constraintName"));

    // When
    ExceptionMessageTranscoded result = transcoder.transcode(exception);

    // Then
    Assertions.assertEquals(
      new ExceptionMessageTranscoded(
        "DEBT_POSITION_CONFLICT",
        "Conflict. " + sqlException.getMessage(),
        null),
      result);
  }
}
