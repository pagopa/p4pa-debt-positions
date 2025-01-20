package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.IuvSequenceNumber;
import it.gov.pagopa.pu.debtpositions.repository.IuvSequenceNumberRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IuvSequenceNumberServiceImpl implements IuvSequenceNumberService {

  private final IuvSequenceNumberRepository iuvSequenceNumberRepository;

  public IuvSequenceNumberServiceImpl(IuvSequenceNumberRepository iuvSequenceNumberRepository) {
    this.iuvSequenceNumberRepository = iuvSequenceNumberRepository;
  }

  @Transactional
  public long getNextIuvSequenceNumber(Long organizationId) {
    long nextSequenceNumber;

    Optional<IuvSequenceNumber> currentEntity = iuvSequenceNumberRepository.findIuvSequenceNumberByOrganizationId(organizationId);

    if (currentEntity.isEmpty()) {
      nextSequenceNumber = 1;
      IuvSequenceNumber newEntity = new IuvSequenceNumber();
      newEntity.setOrganizationId(organizationId);
      newEntity.setSequenceNumber(nextSequenceNumber);
      iuvSequenceNumberRepository.save(newEntity);
    } else {
      IuvSequenceNumber existingISN = currentEntity.get();
      nextSequenceNumber = existingISN.getSequenceNumber() + 1;
      existingISN.setSequenceNumber(nextSequenceNumber);
      iuvSequenceNumberRepository.save(existingISN);
    }

    return nextSequenceNumber;
  }
}
