package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.exception.custom.TooManyElementsException;
import it.gov.pagopa.pu.debtpositions.mapper.PagedInstallmentsPaidViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
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
@TestPropertySource(properties = {"installment-paid-view.max-total-elements=10"})
class InstallmentPaidViewPIIViewRepositoryImplTest {

  @Value("${installment-paid-view.max-total-elements}")
  private Integer maxElements;

  @Mock
  private InstallmentPaidViewNoPIIDTORepository installmentPaidViewNoPIIDTORepositoryMock;

  @Mock
  private PagedInstallmentsPaidViewMapper pagedInstallmentsPaidViewMapperMock;

  InstallmentPaidViewPIIViewRepository installmentPaidViewPIIViewRepository;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    installmentPaidViewPIIViewRepository = new InstallmentPaidViewPIIViewRepositoryImpl(maxElements, installmentPaidViewNoPIIDTORepositoryMock, pagedInstallmentsPaidViewMapperMock);
  }

  @Test
  void givenValidParam_WhenGetPagedInstallmentPaidView_ThenReturnPagedInstallmentsPaidView() {
    //given
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);
    Long debtPositionTypeOrgId = 1L;

    List<InstallmentPaidViewNoPII> content = podamFactory.manufacturePojo(List.class, InstallmentPaidViewNoPII.class);
    Pageable pageable = PageRequest.of(0, 10);
    Page<InstallmentPaidViewNoPII> installmentPaidViewNoPIIS = new PageImpl<>(content, pageable, 10);

    PagedInstallmentsPaidView pagedInstallmentsPaidView = podamFactory.manufacturePojo(PagedInstallmentsPaidView.class);

    Mockito.when(installmentPaidViewNoPIIDTORepositoryMock.findInstallmentPaidViewNoPIIDTO(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, Pageable.ofSize(1))).thenReturn(installmentPaidViewNoPIIS);
    Mockito.when(pagedInstallmentsPaidViewMapperMock.mapToPagedInstallmentsPaidView(installmentPaidViewNoPIIS)).thenReturn(pagedInstallmentsPaidView);
    //when
    PagedInstallmentsPaidView result = installmentPaidViewPIIViewRepository.getPagedInstallmentPaidView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, Pageable.ofSize(1));
    //then
    assertNotNull(result);
    assertEquals(pagedInstallmentsPaidView, result);
  }

  @Test
  void givenWhenGetPagedInstallmentPaidViewThen() {
    //given
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);
    Long debtPositionTypeOrgId = 1L;

    List<InstallmentPaidViewNoPII> content = new ArrayList<>();
    Pageable pageable = PageRequest.of(0, 10);

    for (int i=0; i < 12; i++){
      InstallmentPaidViewNoPII installmentPaidViewNoPII = podamFactory.manufacturePojo(InstallmentPaidViewNoPII.class);
      content.add(installmentPaidViewNoPII);
    }

    Page<InstallmentPaidViewNoPII> installmentPaidViewNoPIIS = new PageImpl<>(content, pageable, 12);

    Mockito.when(installmentPaidViewNoPIIDTORepositoryMock.findInstallmentPaidViewNoPIIDTO(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, Pageable.ofSize(1))).thenReturn(installmentPaidViewNoPIIS);
    //when
    TooManyElementsException ex = assertThrows(TooManyElementsException.class, () ->
            installmentPaidViewPIIViewRepository.getPagedInstallmentPaidView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, Pageable.ofSize(1))
    );
    //then
    assertEquals("The number of InstallmentPaidViewNoPII records returned: 12 exceeds the maximum allowed: 10", ex.getMessage());

  }
}
