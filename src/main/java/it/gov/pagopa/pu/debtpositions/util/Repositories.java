package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;

public record Repositories(DebtPositionRepository debtPositionRepository,
                           PaymentOptionRepository paymentOptionRepository,
                           InstallmentPIIRepository installmentRepository,
                           TransferRepository transferRepository) {
}
