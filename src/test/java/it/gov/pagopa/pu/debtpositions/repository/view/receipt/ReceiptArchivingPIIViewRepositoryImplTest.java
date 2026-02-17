package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.exception.custom.ExportTooManyRecordsException;
import it.gov.pagopa.pu.debtpositions.mapper.pages.PagedReceiptsArchivingViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@TestPropertySource(properties = {"data-export.receipt-archiving-view.max-total-elements=10"})
class ReceiptArchivingPIIViewRepositoryImplTest {

  @Value("${data-export.receipt-archiving-view.max-total-elements}")
  private Integer maxElements;

  @Mock
  private ReceiptArchivingNoPIIViewRepository receiptArchivingNoPIIViewRepositoryMock;
  @Mock
  private PagedReceiptsArchivingViewMapper pagedReceiptsArchivingViewMapperMock;

  ReceiptArchivingPIIViewRepository receiptArchivingPIIViewRepository;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  @BeforeEach
  void setUp() {
    receiptArchivingPIIViewRepository = new ReceiptArchivingPIIViewRepositoryImpl(maxElements,receiptArchivingNoPIIViewRepositoryMock, pagedReceiptsArchivingViewMapperMock);
  }

  @Test
  void givenValidParam_WhenGetPagedReceiptArchivingView_ThenReturnPagedReceiptArchivingView() {
    //given
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);

    List<ReceiptArchivingNoPIIView> content = podamFactory.manufacturePojo(List.class, ReceiptArchivingNoPIIView.class);

    Pageable pageable = PageRequest.of(0, 10);
    Page<ReceiptArchivingNoPIIView> receiptArchivingNoPIIViews = new PageImpl<>(content, pageable, 10);

    PagedReceiptsArchivingView expectedResponse = podamFactory.manufacturePojo(PagedReceiptsArchivingView.class);
    Mockito.when(receiptArchivingNoPIIViewRepositoryMock.findReceiptArchivingViewNoPIIDTO(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo,  pageable)).thenReturn(receiptArchivingNoPIIViews);
    Mockito.when(pagedReceiptsArchivingViewMapperMock.mapToPagedReceiptsArchivingView(receiptArchivingNoPIIViews)).thenReturn(expectedResponse);
    //when
    PagedReceiptsArchivingView result = receiptArchivingPIIViewRepository.getPagedReceiptsArchivingView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, Pageable.ofSize(10));
    //then
    assertNotNull(result);
    assertEquals(expectedResponse, result);
  }

  @Test
  void givenTooManyElements_WhenGetPagedReceiptArchivingView_ThenReturnException() {
    //given
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);

    List<ReceiptArchivingNoPIIView> content = new ArrayList<>();
    Pageable pageable = PageRequest.of(0, 10);

    for (int i=0; i < 12; i++){
      ReceiptArchivingNoPIIView receiptArchivingNoPIIView = podamFactory.manufacturePojo(ReceiptArchivingNoPIIView.class);
      content.add(receiptArchivingNoPIIView);
    }

    Page<ReceiptArchivingNoPIIView> receiptArchivingNoPIIViews= new PageImpl<>(content, pageable, 12);

    Mockito.when(receiptArchivingNoPIIViewRepositoryMock.findReceiptArchivingViewNoPIIDTO(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, Pageable.ofSize(1))).thenReturn(receiptArchivingNoPIIViews);
    //when
    ExportTooManyRecordsException ex = assertThrows(
      ExportTooManyRecordsException.class,
      () -> receiptArchivingPIIViewRepository.getPagedReceiptsArchivingView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, Pageable.ofSize(1))
    );
    //then
    assertEquals("[TOO_MANY_EXPORTED_RECORDS] The number of ReceiptArchivingViewNoPII records returned: 12 exceeds the maximum allowed: 10", ex.getMessage());

  }
}
