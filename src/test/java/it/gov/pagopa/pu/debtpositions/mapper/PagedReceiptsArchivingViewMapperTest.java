package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptArchivingView;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PagedReceiptsArchivingViewMapperTest {

  @Mock
  private ReceiptArchivingPIIMapper receiptArchivingPIIMapperMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  PagedReceiptsArchivingViewMapper pagedReceiptsArchivingViewMapper;

  @BeforeEach
  void setUp() {
    pagedReceiptsArchivingViewMapper = new PagedReceiptsArchivingViewMapper(receiptArchivingPIIMapperMock);
  }

  @Test
  void givenValidPagedReceiptArchivingViewNoPIIDTO_whenMapToPagedReceiptsArchivingView_thenReturnPagedReceiptsArchivingView(){
    //given
    int pageSize = 10;
    long totalElements = 1;

    ReceiptArchivingNoPIIView receiptArchivingNoPIIView = podamFactory.manufacturePojo(ReceiptArchivingNoPIIView.class);

    List<ReceiptArchivingNoPIIView> content = List.of(receiptArchivingNoPIIView);

    Pageable pageable = PageRequest.of(0, pageSize);

    Page<ReceiptArchivingNoPIIView> pagedReceiptArchivingNoPII = new PageImpl<>(content, pageable, totalElements);

    ReceiptArchivingView receiptArchivingView = podamFactory.manufacturePojo(ReceiptArchivingView.class);

    Mockito.when(receiptArchivingPIIMapperMock.map(receiptArchivingNoPIIView)).thenReturn(receiptArchivingView);
    //when

    PagedReceiptsArchivingView result = pagedReceiptsArchivingViewMapper.mapToPagedReceiptsArchivingView(pagedReceiptArchivingNoPII);
    //then
    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getTotalPages());
    assertEquals(10, result.getSize());
    assertEquals(0, result.getNumber());

    TestUtils.checkNotNullFields(result);
  }

  @Test
  void givenEmptyPagedReceiptArchivingViewNoPIIDTO_whenMapToPagedReceiptsArchivingView_thenReturnEmptyCollection(){
    //given
    int pageSize = 10;
    long totalElements = 0;

    Pageable pageable = PageRequest.of(0, pageSize);
    Page<ReceiptArchivingNoPIIView> pagedReceiptArchivingNoPII = new PageImpl<>(Collections.emptyList(), pageable, totalElements);

    //when
    PagedReceiptsArchivingView result = pagedReceiptsArchivingViewMapper.mapToPagedReceiptsArchivingView(pagedReceiptArchivingNoPII);
    //then
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    assertEquals(0, result.getTotalElements());
    assertEquals(0, result.getTotalPages());
    assertEquals(10, result.getSize());
    assertEquals(0, result.getNumber());

    TestUtils.checkNotNullFields(result);
  }

  @Test
  void givenNullPagedReceiptArchivingViewNoPIIDTO_whenMapToPagedReceiptArchivingView_thenReturnNewPagedReceiptArchivingView(){
    //when
    PagedReceiptsArchivingView result = pagedReceiptsArchivingViewMapper.mapToPagedReceiptsArchivingView(null);
    //then
    assertNotNull(result);
  }

  @Test
  void givenUnpagedPagedReceiptArchivingViewNoPIIDTO_whenMapToPagedReceiptArchivingView_thenReturnPagedReceiptArchivingView(){
    //given
    ReceiptArchivingNoPIIView receiptArchivingNoPIIView = podamFactory.manufacturePojo(ReceiptArchivingNoPIIView.class);

    List<ReceiptArchivingNoPIIView> content = List.of(receiptArchivingNoPIIView);

    Page<ReceiptArchivingNoPIIView> unpagedReceiptArchivingNoPIIView = new PageImpl<>(content);

    ReceiptArchivingView receiptArchivingView = podamFactory.manufacturePojo(ReceiptArchivingView.class);

    Mockito.when(receiptArchivingPIIMapperMock.map(receiptArchivingNoPIIView)).thenReturn(receiptArchivingView);

    //when
    PagedReceiptsArchivingView result = pagedReceiptsArchivingViewMapper.mapToPagedReceiptsArchivingView(unpagedReceiptArchivingNoPIIView);

    //then
    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());

    TestUtils.checkNotNullFields(result, "size","totalPages", "totalElements", "number");
  }
}
