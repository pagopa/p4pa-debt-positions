package it.gov.pagopa.pu.debtpositions.exception.custom;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InvalidInstallmentStatusException extends RuntimeException{
  private final String message;
}
