package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.PostalIbanVerifyResponse;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.PostalIbanVerifyResponseMapper;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TransferServiceImpl implements TransferService{

  private final TransferRepository transferRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final PostalIbanVerifyResponseMapper postalIbanVerifyResponseMapper;

  public TransferServiceImpl(TransferRepository transferRepository, InstallmentNoPIIRepository installmentNoPIIRepository, PostalIbanVerifyResponseMapper postalIbanVerifyResponseMapper) {
    this.transferRepository = transferRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.postalIbanVerifyResponseMapper = postalIbanVerifyResponseMapper;
  }

  @Override
  public PostalIbanVerifyResponse verifyPostalIban(List<Long> installmentIds) {
    verifyExistingInstallments(installmentIds);

    Set<Long> idsWithNull = transferRepository
      .findInstallmentIdsWithNullPostalIban(installmentIds);

    return postalIbanVerifyResponseMapper.map(installmentIds, idsWithNull);
  }

  private void verifyExistingInstallments(List<Long> installmentIds) {
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
  }

}

