package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentsSearchFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsView;
import it.gov.pagopa.pu.debtpositions.mapper.PagedInstallmentsViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InstallmentPIIViewRepositoryImplTest {

  @Mock
  private InstallmentNoPIIViewRepository installmentNoPIIViewRepositoryMock;
  @Mock
  private PagedInstallmentsViewMapper pagedInstallmentsViewMapperMock;

  private InstallmentPIIViewRepository installmentPIIViewRepository;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    installmentPIIViewRepository = new InstallmentPIIViewRepositoryImpl(installmentNoPIIViewRepositoryMock, pagedInstallmentsViewMapperMock);
  }

  @Test
  void givenValidFiltersWhenGetPagedInstallmentsByFiltersThenReturnPagedView() {
    //given
    InstallmentsSearchFiltersDTO filters = podamFactory.manufacturePojo(InstallmentsSearchFiltersDTO.class);
    Pageable pageable = PageRequest.of(0, 10);

    List<InstallmentViewNoPII> content = podamFactory.manufacturePojo(List.class, InstallmentViewNoPII.class);
    Page<InstallmentViewNoPII> installmentPaidViewNoPIIS = new PageImpl<>(content, pageable, 10);

    PagedInstallmentsView expectedPagedView = podamFactory.manufacturePojo(PagedInstallmentsView.class);

    Mockito.when(installmentNoPIIViewRepositoryMock.findInstallmentsByFilters(filters, pageable)).thenReturn(installmentPaidViewNoPIIS);
    Mockito.when(pagedInstallmentsViewMapperMock.mapToPagedInstallmentsView(installmentPaidViewNoPIIS)).thenReturn(expectedPagedView);

    //when
    PagedInstallmentsView result = installmentPIIViewRepository.getPagedInstallmentsByFilters(filters, pageable);

    //then
    assertNotNull(result);
    assertEquals(expectedPagedView, result);
  }
}
