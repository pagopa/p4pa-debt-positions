package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.model.IuvSequenceNumber;
import it.gov.pagopa.pu.debtpositions.repository.IuvSequenceNumberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IuvSequenceNumberServiceImplTest {

  @Mock
  private IuvSequenceNumberRepository iuvSequenceNumberRepositoryMock;

  private IuvSequenceNumberServiceImpl iuvSequenceNumberService;

  @BeforeEach
  void setUp() {
    iuvSequenceNumberService = new IuvSequenceNumberServiceImpl(iuvSequenceNumberRepositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(iuvSequenceNumberRepositoryMock);
  }

  @Test
  void whenGetNextIuvSequenceNumberThenOk(){
    // GIven
    IuvSequenceNumber iuvSequenceNumber = new IuvSequenceNumber();
    iuvSequenceNumber.setId(1L);
    iuvSequenceNumber.setSequenceNumber(2L);
    iuvSequenceNumber.setOrganizationId(3L);

    Mockito.when(iuvSequenceNumberRepositoryMock.getNextIuvSequenceNumber(1L)).thenReturn(iuvSequenceNumber);

    // When
    long result = iuvSequenceNumberService.getNextIuvSequenceNumber(1L);

    // Then
    Assertions.assertEquals(iuvSequenceNumber.getSequenceNumber(), result);
  }

}
