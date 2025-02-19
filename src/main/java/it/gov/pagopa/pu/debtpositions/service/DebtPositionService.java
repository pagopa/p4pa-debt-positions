package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;

public interface DebtPositionService {

  DebtPositionDTO saveDebtPosition(DebtPositionDTO debtPositionDTO, Organization org);

  InstallmentDTO saveNewInstallment(InstallmentDTO installmentDTO);

  PaymentOptionDTO saveNewPaymentOption(PaymentOptionDTO paymentOptionDTO);
}
