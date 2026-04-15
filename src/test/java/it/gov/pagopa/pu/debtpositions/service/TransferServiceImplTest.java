package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.PostalIbanVerifyResponse;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

  @Mock
  private TransferRepository transferRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;

  private TransferServiceImpl transferService;

  @BeforeEach
  void setUp() {
    transferService = new TransferServiceImpl(
      transferRepositoryMock,
      installmentNoPIIRepositoryMock
    );
  }


  @Test
  void givenValidInstallmentIdsWhenVerifyPostalIbanThenReturnCorrectMap() {
    // given
    List<Long> installmentIds = List.of(1L, 2L, 3L);

    Mockito.when(installmentNoPIIRepositoryMock.findExistingInstallmentIds(installmentIds))
      .thenReturn(Set.of(1L, 2L, 3L));

    Mockito.when(transferRepositoryMock.findInstallmentIdsWithNullPostalIban(installmentIds))
      .thenReturn(Set.of(2L));

    // when
    PostalIbanVerifyResponse result = transferService.verifyPostalIban(installmentIds);

    // then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.get(1L));
    assertFalse(result.get(2L));
    assertTrue(result.get(3L));
  }

  @Test
  void givenMissingInstallmentIdsWhenVerifyPostalIbanThenThrowNotFoundException() {
    // given
    List<Long> installmentIds = List.of(1L, 2L, 3L);

    Mockito.when(installmentNoPIIRepositoryMock.findExistingInstallmentIds(installmentIds))
      .thenReturn(Set.of(1L, 2L));

    // when & then
    NotFoundException ex = assertThrows(NotFoundException.class,
      () -> transferService.verifyPostalIban(installmentIds));

    assertTrue(ex.getMessage().contains("3"));
  }

}
