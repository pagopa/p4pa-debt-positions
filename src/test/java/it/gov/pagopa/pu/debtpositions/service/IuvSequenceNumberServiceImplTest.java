package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.IuvSequenceNumber;
import it.gov.pagopa.pu.debtpositions.repository.IuvSequenceNumberRepository;
import it.gov.pagopa.pu.debtpositions.service.create.IuvSequenceNumberServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class IuvSequenceNumberServiceImplTest {

  @Mock
  private IuvSequenceNumberRepository iuvSequenceNumberRepository;

  private IuvSequenceNumberServiceImpl iuvSequenceNumberService;

  @BeforeEach
  void setUp() {
    iuvSequenceNumberService = new IuvSequenceNumberServiceImpl(iuvSequenceNumberRepository);
  }

  @Test
  void givenValidOrgWhenGetNextIuvSequenceNumberThenOk(){
    IuvSequenceNumber iuvSequenceNumber = new IuvSequenceNumber();
    iuvSequenceNumber.setId(1L);
    iuvSequenceNumber.setSequenceNumber(2L);
    iuvSequenceNumber.setOrganizationId(3L);

    Mockito.when(iuvSequenceNumberRepository.findByOrganizationId(1L)).thenReturn(Optional.of(iuvSequenceNumber));

    long result = iuvSequenceNumberService.getNextIuvSequenceNumber(1L);

    Assertions.assertEquals(3L, result);
    Mockito.verify(iuvSequenceNumberRepository, Mockito.times(1))
      .findByOrganizationId(1L);
  }

  @Test
  void givenNonExistentOrgWhenGetNextIuvSequenceNumberThenOk(){
    Mockito.when(iuvSequenceNumberRepository.findByOrganizationId(1L)).thenReturn(Optional.empty());

    long result = iuvSequenceNumberService.getNextIuvSequenceNumber(1L);

    Assertions.assertEquals(1L, result);
    Mockito.verify(iuvSequenceNumberRepository, Mockito.times(1))
      .findByOrganizationId(1L);
  }
}
