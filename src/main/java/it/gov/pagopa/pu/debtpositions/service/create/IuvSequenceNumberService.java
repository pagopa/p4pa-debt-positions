package it.gov.pagopa.pu.debtpositions.service.create;

public interface IuvSequenceNumberService {
  long getNextIuvSequenceNumber(Long organizationId);
}
