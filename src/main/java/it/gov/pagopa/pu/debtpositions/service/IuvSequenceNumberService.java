package it.gov.pagopa.pu.debtpositions.service;

public interface IuvSequenceNumberService {
  long getNextIuvSequenceNumber(Long organizationId);
}
