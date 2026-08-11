package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.PostalIbanVerifyResponse;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.PostalIbanVerifyResponseMapper;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

  @Mock
  private TransferRepository transferRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private PostalIbanVerifyResponseMapper postalIbanVerifyResponseMapperMock;

  private TransferServiceImpl transferService;

  @BeforeEach
  void setUp() {
    transferService = new TransferServiceImpl(
      transferRepositoryMock,
      installmentNoPIIRepositoryMock,
      postalIbanVerifyResponseMapperMock
    );
  }


  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      transferRepositoryMock,
      installmentNoPIIRepositoryMock,
      postalIbanVerifyResponseMapperMock
    );
  }

  @Test
  void givenValidInstallmentIdsWhenVerifyPostalIbanThenReturnMappedResponse() {
    // given
    List<Long> installmentIds = List.of(1L, 2L, 3L);
    Set<Long> idsWithNull = Set.of(2L);

    PostalIbanVerifyResponse expectedResponse = new PostalIbanVerifyResponse();

    when(installmentNoPIIRepositoryMock.findExistingInstallmentIds(installmentIds))
      .thenReturn(Set.of(1L, 2L, 3L));

    when(transferRepositoryMock.findInstallmentIdsWithNullPostalIban(installmentIds))
      .thenReturn(idsWithNull);

    when(postalIbanVerifyResponseMapperMock.map(installmentIds, idsWithNull))
      .thenReturn(expectedResponse);

    // when
    PostalIbanVerifyResponse result = transferService.verifyPostalIban(installmentIds);

    // then
    assertNotNull(result);
    assertEquals(expectedResponse, result);
  }

  @Test
  void givenMissingInstallmentIdsWhenVerifyPostalIbanThenThrowNotFoundException() {
    // given
    List<Long> installmentIds = List.of(1L, 2L, 3L);

    when(installmentNoPIIRepositoryMock.findExistingInstallmentIds(installmentIds))
      .thenReturn(Set.of(1L, 2L));

    // when & then
    NotFoundException ex = assertThrows(NotFoundException.class,
      () -> transferService.verifyPostalIban(installmentIds));

    assertTrue(ex.getMessage().contains("3"));

    Mockito.verifyNoInteractions(transferRepositoryMock, postalIbanVerifyResponseMapperMock);
  }

  @Test
  void givenAllInstallmentsValidWhenVerifyPostalIbanThenMapperCalledWithEmptySet() {
    // given
    List<Long> installmentIds = List.of(1L, 2L);

    PostalIbanVerifyResponse expectedResponse = new PostalIbanVerifyResponse();

    when(installmentNoPIIRepositoryMock.findExistingInstallmentIds(installmentIds))
      .thenReturn(Set.of(1L, 2L));

    when(transferRepositoryMock.findInstallmentIdsWithNullPostalIban(installmentIds))
      .thenReturn(Set.of());

    when(postalIbanVerifyResponseMapperMock.map(installmentIds, Set.of()))
      .thenReturn(expectedResponse);

    // when
    PostalIbanVerifyResponse result = transferService.verifyPostalIban(installmentIds);

    // then
    assertEquals(expectedResponse, result);
  }
}
