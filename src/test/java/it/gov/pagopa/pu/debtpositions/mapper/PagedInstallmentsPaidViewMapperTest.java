package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentPaidViewFaker;
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

  @Test
  void givenValidPagedInstallmentPaidViewNoPIIDTO_whenMapToPagedInstallmentsPaidView_thenReturnPagedInstallmentPaidView(){
    //given
    int pageSize = 10;
    long totalElements = 1;

    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();

    List<InstallmentPaidViewNoPII> content = List.of(installmentPaidViewNoPII);

    Pageable pageable = PageRequest.of(0, pageSize);

    Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPII = new PageImpl<>(content, pageable, totalElements);

    InstallmentPaidViewDTO installmentPaidViewDTO = podamFactory.manufacturePojo(InstallmentPaidViewDTO.class);

    Mockito.when(installmentPaidPIIMapperMock.map(installmentPaidViewNoPII)).thenReturn(installmentPaidViewDTO);
    //when

    PagedInstallmentsPaidView result = pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView(pagedInstallmentPaidViewNoPII);
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
  void givenEmptyPagedInstallmentPaidViewNoPIIDTO_whenMapToPagedInstallmentsPaidView_thenReturnEmptyCollection(){
    //given
    int pageSize = 10;
    long totalElements = 0;

    Pageable pageable = PageRequest.of(0, pageSize);

    Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPII = new PageImpl<>(Collections.emptyList(), pageable, totalElements);
    //when

    PagedInstallmentsPaidView result = pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView(pagedInstallmentPaidViewNoPII);
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
  void givenNullPagedInstallmentPaidViewNoPIIDTO_whenMapToPagedInstallmentsPaidView_thenReturnNewPagedInstallmentPaidView(){
    //when
    PagedInstallmentsPaidView result = pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView( null);
    //then
    assertNotNull(result);
  }

  @Test
  void givenUnpagedInstallmentPaidViewNoPIIDTO_whenMapToPagedInstallmentsPaidView_thenReturnPagedInstallmentPaidView(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();

    List<InstallmentPaidViewNoPII> content = List.of(installmentPaidViewNoPII);

    Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPII = new PageImpl<>(content);

    InstallmentPaidViewDTO installmentPaidViewDTO = podamFactory.manufacturePojo(InstallmentPaidViewDTO.class);

    Mockito.when(installmentPaidPIIMapperMock.map(installmentPaidViewNoPII)).thenReturn(installmentPaidViewDTO);

    //when
    PagedInstallmentsPaidView result = pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView(pagedInstallmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());

    TestUtils.checkNotNullFields(result, "size","totalPages", "totalElements", "number");
  }
}
