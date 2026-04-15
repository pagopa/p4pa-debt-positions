package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.PostalIbanVerifyResponse;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TransferServiceImpl implements TransferService{

  private final TransferRepository transferRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;

  public TransferServiceImpl(TransferRepository transferRepository, InstallmentNoPIIRepository installmentNoPIIRepository) {
    this.transferRepository = transferRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
  }

  @Override
  public PostalIbanVerifyResponse verifyPostalIban(List<Long> installmentIds) {

    Set<Long> existingIds = installmentNoPIIRepository.findExistingInstallmentIds(installmentIds);

    List<Long> missingIds = installmentIds.stream()
      .filter(id -> !existingIds.contains(id))
      .toList();

    if (!missingIds.isEmpty()) {
      throw new NotFoundException(
        "[INSTALLMENT_NOT_FOUND]",
        "The following installmentIds were not found: " + missingIds
      );
    }

    Set<Long> idsWithNull = transferRepository
      .findInstallmentIdsWithNullPostalIban(installmentIds);

    PostalIbanVerifyResponse response = new PostalIbanVerifyResponse();

    installmentIds.forEach(id ->
      response.put(
        id,
        !idsWithNull.contains(id)
      )
    );

    return response;
  }

}

