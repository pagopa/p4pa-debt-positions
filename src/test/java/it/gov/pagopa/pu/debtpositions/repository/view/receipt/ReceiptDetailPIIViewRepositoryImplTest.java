package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.pii.view.ReceiptDetailPIIViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReceiptDetailPIIViewRepositoryImplTest {

  @Mock
  private ReceiptDetailNoPIIViewRepository receiptDetailNoPIIViewRepositoryMock;
  @Mock
  private ReceiptDetailPIIViewMapper receiptDetailPIIViewMapperMock;

  private ReceiptDetailPIIViewRepository receiptDetailPIIViewRepository;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    receiptDetailPIIViewRepository = new ReceiptDetailPIIViewRepositoryImpl(
      receiptDetailNoPIIViewRepositoryMock, receiptDetailPIIViewMapperMock);
  }

  @Test
  void givenExistingReceiptWhenGetReceiptDetailThenOk() {
    // Given
    Long organizationId = 1L;
    Long receiptId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    String iud = "iud";
    ReceiptDetailNoPIIView receiptDetailNoPIIView = podamFactory.manufacturePojo(
      ReceiptDetailNoPIIView.class);
    ReceiptDetailDTO receiptDetail = podamFactory.manufacturePojo(ReceiptDetailDTO.class);

    Mockito.when(receiptDetailNoPIIViewRepositoryMock.findReceiptDetailView(receiptId, operatorExternalUserId, organizationId, iud)).thenReturn(
      Optional.of(receiptDetailNoPIIView));
    Mockito.when(receiptDetailPIIViewMapperMock.mapToReceiptDetailDTO(receiptDetailNoPIIView)).thenReturn(receiptDetail);

    // When
    ReceiptDetailDTO result = receiptDetailPIIViewRepository.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud);

    // Then
    Assertions.assertEquals(receiptDetail, result);
    Mockito.verify(receiptDetailNoPIIViewRepositoryMock).findReceiptDetailView(receiptId, operatorExternalUserId, organizationId, iud);
    Mockito.verify(receiptDetailPIIViewMapperMock).mapToReceiptDetailDTO(receiptDetailNoPIIView);
  }

  @Test
  void givenNonExistingReceiptWhenFindReceiptThenNotFoundException() {
    // Given
    Long organizationId = 1L;
    Long receiptId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    String iud = "iud";

    Mockito.when(receiptDetailNoPIIViewRepositoryMock.findReceiptDetailView(receiptId, operatorExternalUserId, organizationId, iud)).thenReturn(
      Optional.empty());

    // When
    Assertions.assertThrows(NotFoundException.class,()->receiptDetailPIIViewRepository.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud));

    Mockito.verify(receiptDetailNoPIIViewRepositoryMock).findReceiptDetailView(receiptId, operatorExternalUserId, organizationId, iud);
    Mockito.verifyNoInteractions(receiptDetailPIIViewMapperMock);
  }

  @Test
  void givenExistingReceiptAndNoOperatorExternalUserIdWhenGetReceiptDetailThenOk() {
    // Given
    Long organizationId = 1L;
    Long receiptId = 1L;
    String iud = "iud";
    ReceiptDetailNoPIIView receiptDetailNoPIIView = podamFactory.manufacturePojo(
      ReceiptDetailNoPIIView.class);
    ReceiptDetailDTO receiptDetail = podamFactory.manufacturePojo(ReceiptDetailDTO.class);

    Mockito.when(receiptDetailNoPIIViewRepositoryMock.findReceiptDetailView(receiptId, organizationId, iud)).thenReturn(
      Optional.of(receiptDetailNoPIIView));
    Mockito.when(receiptDetailPIIViewMapperMock.mapToReceiptDetailDTO(receiptDetailNoPIIView)).thenReturn(receiptDetail);

    // When
    ReceiptDetailDTO result = receiptDetailPIIViewRepository.getReceiptDetail(receiptId, organizationId, iud);

    // Then
    Assertions.assertEquals(receiptDetail, result);
    Mockito.verify(receiptDetailNoPIIViewRepositoryMock).findReceiptDetailView(receiptId, organizationId, iud);
    Mockito.verify(receiptDetailPIIViewMapperMock).mapToReceiptDetailDTO(receiptDetailNoPIIView);
  }

  @Test
  void givenNonExistingReceiptAndNoOperatorExternalUserIdWhenFindReceiptThenNotFoundException() {
    // Given
    Long organizationId = 1L;
    Long receiptId = 1L;
    String iud = "iud";

    Mockito.when(receiptDetailNoPIIViewRepositoryMock.findReceiptDetailView(receiptId, organizationId, iud)).thenReturn(
      Optional.empty());

    // When
    Assertions.assertThrows(NotFoundException.class,()->receiptDetailPIIViewRepository.getReceiptDetail(receiptId, organizationId, iud));

    Mockito.verify(receiptDetailNoPIIViewRepositoryMock).findReceiptDetailView(receiptId, organizationId, iud);
    Mockito.verifyNoInteractions(receiptDetailPIIViewMapperMock);
  }
}
