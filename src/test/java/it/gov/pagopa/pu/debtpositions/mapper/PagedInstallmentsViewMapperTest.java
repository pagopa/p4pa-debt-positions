package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentViewDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsView;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagedInstallmentsViewMapperTest {
  @Mock
  private InstallmentViewDTOMapper installmentViewDTOMapperMock;

  @InjectMocks
  private PagedInstallmentsViewMapper mapper;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenFullContentWhenMapThenReturnPagedInstallmentsView() {
    //given
    int pageSize = 10;
    long totalElements = 1;

    InstallmentViewNoPII installmentViewNoPII = podamFactory.manufacturePojo(InstallmentViewNoPII.class);
    List<InstallmentViewNoPII> content = List.of(installmentViewNoPII);
    Pageable pageable = PageRequest.of(0, pageSize);
    Page<InstallmentViewNoPII> page = new PageImpl<>(content, pageable, totalElements);
    InstallmentViewDTO installmentViewDTO = podamFactory.manufacturePojo(InstallmentViewDTO.class);

    when(installmentViewDTOMapperMock.map(installmentViewNoPII)).thenReturn(installmentViewDTO);

    //when
    PagedInstallmentsView result = mapper.mapToPagedInstallmentsView(page);
    //then
    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());
    assertEquals(0, result.getTotalElements());
    assertEquals(0, result.getTotalPages());
    assertEquals(10, result.getSize());
    assertEquals(0, result.getNumber());

    TestUtils.checkNotNullFields(result);
  }

  @Test
  void givenEmptyPagedWhenMapThenReturnEmptyCollection() {
    //given
    int pageSize = 10;
    long totalElements = 0;

    Pageable pageable = PageRequest.of(0, pageSize);

    Page<InstallmentViewNoPII> pagedInstallmentViewNoPIIs = new PageImpl<>(List.of(), pageable, totalElements);

    //when
    PagedInstallmentsView result =  mapper.mapToPagedInstallmentsView(pagedInstallmentViewNoPIIs);
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
  void givenEmptyContentWhenMapThenReturnPagedInstallmentsView() {
    //given
    InstallmentViewNoPII installmentViewNoPII = podamFactory.manufacturePojo(InstallmentViewNoPII.class);
    List<InstallmentViewNoPII> content = List.of(installmentViewNoPII);

    Page<InstallmentViewNoPII> pagedInstallmentViewNoPIIs = new PageImpl<>(content);
    InstallmentViewDTO installmentViewDTO = podamFactory.manufacturePojo(InstallmentViewDTO.class);

    when(installmentViewDTOMapperMock.map(installmentViewNoPII)).thenReturn(installmentViewDTO);
    //when
    PagedInstallmentsView result = mapper.mapToPagedInstallmentsView(pagedInstallmentViewNoPIIs);

    //then
    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());

    TestUtils.checkNotNullFields(result, "size","totalPages", "totalElements", "number");
  }
}
