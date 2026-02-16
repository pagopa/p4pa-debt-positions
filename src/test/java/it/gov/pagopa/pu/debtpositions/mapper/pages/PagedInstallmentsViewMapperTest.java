package it.gov.pagopa.pu.debtpositions.mapper.pages;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsView;
import it.gov.pagopa.pu.debtpositions.dto.view.InstallmentViewDTO;
import it.gov.pagopa.pu.debtpositions.mapper.pii.view.InstallmentViewDTOMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagedInstallmentsViewMapperTest {
  @Mock
  private InstallmentViewDTOMapper installmentViewDTOMapperMock;

  @InjectMocks
  private PagedInstallmentsViewMapper mapper;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(installmentViewDTOMapperMock);
  }

  @Test
  void givenFullContentWhenMapThenReturnPagedInstallmentsView() {
    // Given
    int pageSize = 10;
    long totalElements = 1;

    Pageable pageable = PageRequest.of(0, pageSize);
    List<InstallmentViewNoPII> content = List.of(podamFactory.manufacturePojo(InstallmentViewNoPII.class));
    Page<InstallmentViewNoPII> page = new PageImpl<>(content, pageable, totalElements);
    List<InstallmentViewDTO> expectedContent = List.of(podamFactory.manufacturePojo(InstallmentViewDTO.class));

    when(installmentViewDTOMapperMock.mapAll(content))
      .thenReturn(expectedContent);

    // When
    PagedInstallmentsView result = mapper.mapToPagedInstallmentsView(page);

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
  void givenEmptyPagedWhenMapThenReturnEmptyCollection() {
    // Given
    int pageSize = 10;
    long totalElements = 0;

    Pageable pageable = PageRequest.of(0, pageSize);

    Page<InstallmentViewNoPII> pagedInstallmentViewNoPIIs = new PageImpl<>(List.of(), pageable, totalElements);

    // When
    PagedInstallmentsView result =  mapper.mapToPagedInstallmentsView(pagedInstallmentViewNoPIIs);

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
  void givenEmptyContentWhenMapThenReturnPagedInstallmentsView() {
    // Given
    List<InstallmentViewNoPII> content = List.of(podamFactory.manufacturePojo(InstallmentViewNoPII.class));
    Page<InstallmentViewNoPII> pagedInstallmentViewNoPIIs = new PageImpl<>(content);
    List<InstallmentViewDTO> expectedContent = List.of(podamFactory.manufacturePojo(InstallmentViewDTO.class));

    when(installmentViewDTOMapperMock.mapAll(content))
      .thenReturn(expectedContent);

    // When
    PagedInstallmentsView result = mapper.mapToPagedInstallmentsView(pagedInstallmentViewNoPIIs);

    // Then
    assertNotNull(result);
    assertSame(expectedContent, result.getContent());

    TestUtils.checkNotNullFields(result, "size","totalPages", "totalElements", "number");
  }
}
