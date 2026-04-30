package it.gov.pagopa.pu.debtpositions.mapper.pages;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.dto.view.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.mapper.pii.view.InstallmentPaidPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentPaidViewFaker;
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
class PagedInstallmentsPaidViewMapperTest {

  @Mock
  private InstallmentPaidPIIMapper installmentPaidPIIMapperMock;

  private PagedInstallmentsPaidViewMapper pagedInstallmentsPaidViewMapper;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    pagedInstallmentsPaidViewMapper = new PagedInstallmentsPaidViewMapper(installmentPaidPIIMapperMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(installmentPaidPIIMapperMock);
  }

  @Test
  void givenValidPagedInstallmentPaidViewNoPIIDTO_whenMapToPagedInstallmentsPaidView_thenReturnPagedInstallmentPaidView(){
    // Given
    int pageSize = 10;
    long totalElements = 1;

    Pageable pageable = PageRequest.of(0, pageSize);
    List<InstallmentPaidViewNoPII> content = List.of(InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII());
    Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPII = new PageImpl<>(content, pageable, totalElements);

    List<InstallmentPaidViewDTO> expectedContent = List.of(podamFactory.manufacturePojo(InstallmentPaidViewDTO.class));

    Mockito.when(installmentPaidPIIMapperMock.mapAll(content))
      .thenReturn(expectedContent);

    // When
    PagedInstallmentsPaidView result = pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView(pagedInstallmentPaidViewNoPII);

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
  void givenEmptyPagedInstallmentPaidViewNoPIIDTO_whenMapToPagedInstallmentsPaidView_thenReturnEmptyCollection(){
    // Given
    int pageSize = 10;
    long totalElements = 0;

    Pageable pageable = PageRequest.of(0, pageSize);

    Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPII = new PageImpl<>(Collections.emptyList(), pageable, totalElements);

    // When
    PagedInstallmentsPaidView result = pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView(pagedInstallmentPaidViewNoPII);

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
  void givenNullPagedInstallmentPaidViewNoPIIDTO_whenMapToPagedInstallmentsPaidView_thenReturnNewPagedInstallmentPaidView(){
    // When
    PagedInstallmentsPaidView result = pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView( null);

    // Then
    assertNotNull(result);
  }

  @Test
  void givenUnpagedInstallmentPaidViewNoPIIDTO_whenMapToPagedInstallmentsPaidView_thenReturnPagedInstallmentPaidView(){
    // Given
    List<InstallmentPaidViewNoPII> content = List.of(InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII());
    Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPII = new PageImpl<>(content);
    List<InstallmentPaidViewDTO> expectedResult = List.of(podamFactory.manufacturePojo(InstallmentPaidViewDTO.class));

    Mockito.when(installmentPaidPIIMapperMock.mapAll(content)).thenReturn(expectedResult);

    // When
    PagedInstallmentsPaidView result = pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView(pagedInstallmentPaidViewNoPII);

    // Then
    assertNotNull(result);
    assertSame(expectedResult, result.getContent());

    TestUtils.checkNotNullFields(result, "size","totalPages", "totalElements", "number");
  }
}
