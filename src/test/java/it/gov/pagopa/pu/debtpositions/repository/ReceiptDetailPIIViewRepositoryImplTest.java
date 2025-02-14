package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptDetailPIIViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailNoPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailPIIViewRepositoryImpl;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

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
    Long receiptId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    ReceiptDetailNoPIIView receiptDetailNoPIIView = podamFactory.manufacturePojo(
      ReceiptDetailNoPIIView.class);
    ReceiptDetailDTO receiptDetail = podamFactory.manufacturePojo(ReceiptDetailDTO.class);

    Mockito.when(receiptDetailNoPIIViewRepositoryMock.findReceiptDetailView(receiptId, operatorExternalUserId)).thenReturn(
      Optional.of(receiptDetailNoPIIView));
    Mockito.when(receiptDetailPIIViewMapperMock.mapToReceiptDetailDTO(receiptDetailNoPIIView)).thenReturn(receiptDetail);

    // When
    ReceiptDetailDTO result = receiptDetailPIIViewRepository.getReceiptDetail(receiptId,operatorExternalUserId);

    // Then
    Assertions.assertEquals(receiptDetail, result);
    Mockito.verify(receiptDetailNoPIIViewRepositoryMock).findReceiptDetailView(receiptId,operatorExternalUserId);
    Mockito.verify(receiptDetailPIIViewMapperMock).mapToReceiptDetailDTO(receiptDetailNoPIIView);
  }

  @Test
  void givenNonExistingReceiptWhenFindReceiptThenNotFoundException() {
    // Given
    Long receiptId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";

    Mockito.when(receiptDetailNoPIIViewRepositoryMock.findReceiptDetailView(receiptId, operatorExternalUserId)).thenReturn(
      Optional.empty());

    // When
    Assertions.assertThrows(NotFoundException.class,()->receiptDetailPIIViewRepository.getReceiptDetail(receiptId,operatorExternalUserId));

    Mockito.verify(receiptDetailNoPIIViewRepositoryMock).findReceiptDetailView(receiptId,operatorExternalUserId);
    Mockito.verifyNoInteractions(receiptDetailPIIViewMapperMock);
  }
}
