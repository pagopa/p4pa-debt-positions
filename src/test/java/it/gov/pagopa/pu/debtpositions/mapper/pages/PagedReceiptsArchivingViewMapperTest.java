package it.gov.pagopa.pu.debtpositions.mapper.pages;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptArchivingView;
import it.gov.pagopa.pu.debtpositions.mapper.pii.view.ReceiptArchivingPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
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

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(receiptArchivingPIIMapperMock);
  }

  @Test
  void givenValidPagedReceiptArchivingViewNoPIIDTO_whenMapToPagedReceiptsArchivingView_thenReturnPagedReceiptsArchivingView(){
    // Given
    int pageSize = 10;
    long totalElements = 1;

    Pageable pageable = PageRequest.of(0, pageSize);
    List<ReceiptArchivingNoPIIView> content = List.of(podamFactory.manufacturePojo(ReceiptArchivingNoPIIView.class));
    Page<ReceiptArchivingNoPIIView> pagedReceiptArchivingNoPII = new PageImpl<>(content, pageable, totalElements);
    List<ReceiptArchivingView> expectedContent = List.of(podamFactory.manufacturePojo(ReceiptArchivingView.class));

    Mockito.when(receiptArchivingPIIMapperMock.mapAll(content))
      .thenReturn(expectedContent);

    // When
    PagedReceiptsArchivingView result = pagedReceiptsArchivingViewMapper.mapToPagedReceiptsArchivingView(pagedReceiptArchivingNoPII);

    // Then
    assertNotNull(result);
    assertSame(expectedContent, result.getContent());
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getTotalPages());
    assertEquals(10, result.getSize());
    assertEquals(0, result.getNumber());

    TestUtils.checkNotNullFields(result);
  }

  @Test
  void givenEmptyPagedReceiptArchivingViewNoPIIDTO_whenMapToPagedReceiptsArchivingView_thenReturnEmptyCollection(){
    // Given
    int pageSize = 10;
    long totalElements = 0;

    Pageable pageable = PageRequest.of(0, pageSize);
    Page<ReceiptArchivingNoPIIView> pagedReceiptArchivingNoPII = new PageImpl<>(Collections.emptyList(), pageable, totalElements);

    // When
    PagedReceiptsArchivingView result = pagedReceiptsArchivingViewMapper.mapToPagedReceiptsArchivingView(pagedReceiptArchivingNoPII);

    // Then
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
    // When
    PagedReceiptsArchivingView result = pagedReceiptsArchivingViewMapper.mapToPagedReceiptsArchivingView(null);

    // Then
    assertNotNull(result);
  }

  @Test
  void givenUnpagedPagedReceiptArchivingViewNoPIIDTO_whenMapToPagedReceiptArchivingView_thenReturnPagedReceiptArchivingView(){
    // Given
    List<ReceiptArchivingNoPIIView> content = List.of(podamFactory.manufacturePojo(ReceiptArchivingNoPIIView.class));

    Page<ReceiptArchivingNoPIIView> unpagedReceiptArchivingNoPIIView = new PageImpl<>(content);


    List<ReceiptArchivingView> expectedContent = List.of(podamFactory.manufacturePojo(ReceiptArchivingView.class));
    Mockito.when(receiptArchivingPIIMapperMock.mapAll(content))
      .thenReturn(expectedContent);

    // When
    PagedReceiptsArchivingView result = pagedReceiptsArchivingViewMapper.mapToPagedReceiptsArchivingView(unpagedReceiptArchivingNoPIIView);

    // Then
    assertNotNull(result);
    assertSame(expectedContent, result.getContent());

    TestUtils.checkNotNullFields(result, "size","totalPages", "totalElements", "number");
  }
}
