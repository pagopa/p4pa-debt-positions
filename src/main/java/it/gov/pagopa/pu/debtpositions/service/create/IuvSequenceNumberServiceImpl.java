package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.repository.IuvSequenceNumberRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class IuvSequenceNumberServiceImpl implements IuvSequenceNumberService {

  private final IuvSequenceNumberRepository iuvSequenceNumberRepository;

  public IuvSequenceNumberServiceImpl(IuvSequenceNumberRepository iuvSequenceNumberRepository) {
    this.iuvSequenceNumberRepository = iuvSequenceNumberRepository;
  }

  @Transactional
  public long getNextIuvSequenceNumber(Long organizationId) {
    return iuvSequenceNumberRepository.getNextIuvSequenceNumber(organizationId)
      .getSequenceNumber();
  }
}
