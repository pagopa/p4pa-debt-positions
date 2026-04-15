package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.PostalIbanVerifyResponse;

import java.util.List;

public interface TransferService {
  PostalIbanVerifyResponse verifyPostalIban(List<Long> installmentIds);
}
