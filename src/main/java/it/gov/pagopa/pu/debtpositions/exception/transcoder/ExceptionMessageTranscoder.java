package it.gov.pagopa.pu.debtpositions.exception.transcoder;

public interface ExceptionMessageTranscoder<T extends Exception> {
  ExceptionMessageTranscoded transcode(T exception);
}
