package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.PostalIbanVerifyResponse;

import java.util.List;

public interface TransferService {
  PostalIbanVerifyResponse checkAllTransfersHavePostalIban(List<Long> installmentIds);
}
