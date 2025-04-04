package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentDetailPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentPaidViewPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionUpdateInstallmentService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InstallmentServiceImplTest {

  @Mock
  private InstallmentPIIRepository installmentPIIRepositoryMock;
  @Mock
  private InstallmentMapper installmentMapperMock;
  @Mock
  private InstallmentDetailPIIViewRepository installmentDetailPIIViewRepositoryMock;
  @Mock
  private InstallmentPaidViewPIIViewRepository installmentPaidViewPIIViewRepositoryMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentServiceMock;

  private InstallmentServiceImpl installmentService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    installmentService = new InstallmentServiceImpl(
      installmentPIIRepositoryMock,
      installmentMapperMock,
      installmentDetailPIIViewRepositoryMock,
      installmentPaidViewPIIViewRepositoryMock,
      debtPositionServiceMock,
      debtPositionUpdateInstallmentServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      installmentPIIRepositoryMock,
      installmentMapperMock,
      installmentDetailPIIViewRepositoryMock,
      installmentPaidViewPIIViewRepositoryMock,
      debtPositionServiceMock,
      debtPositionUpdateInstallmentServiceMock);
  }

  @ParameterizedTest
  @ValueSource(strings = {"ORDINARY", "ORDINARY_SIL"})
  @NullSource
  void givenValidOrganizationAndNavWhGetInstallmentsByOrganizationIdAndNavThenOk(String debtPositionOrigin) {
    //given
    List<Installment> installmentList = podamFactory.manufacturePojo(List.class, Installment.class);
    List<InstallmentDTO> installmentDTOList = new ArrayList<>();
    List<DebtPositionOrigin> originList = debtPositionOrigin == null ? null : List.of(DebtPositionOrigin.valueOf(debtPositionOrigin));

    Mockito.when(installmentPIIRepositoryMock.getByOrganizationIdAndNav(1L, "NAV", originList)).thenReturn(installmentList);
    installmentList.forEach(installment -> {
      InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
      installmentDTOList.add(installmentDTO);
      Mockito.when(installmentMapperMock.mapToDto(installment)).thenReturn(installmentDTO);
    });

    //when
    List<InstallmentDTO> response = installmentService.getInstallmentsByOrganizationIdAndNav(1L, "NAV", originList);

    //verify
    assertNotNull(response);
    Assertions.assertIterableEquals(installmentDTOList, response);
  }

  @Test
  void whenGetInstallmentDetailThenOk() {
    Long installmentId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentDetailDTO installment = podamFactory.manufacturePojo(InstallmentDetailDTO.class);

    Mockito.when(installmentDetailPIIViewRepositoryMock.getInstallmentDetail(installmentId, operatorExternalUserId)).thenReturn(installment);

    InstallmentDetailDTO response = installmentService.getInstallmentDetail(installmentId, operatorExternalUserId);

    assertNotNull(response);
    Assertions.assertEquals(installment, response);
  }

  @Test
  void whenGetPagedInstallmentPaidViewThenOk() {
    //given
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);
    Long debtPositionTypeOrgId = 1L;

    PagedInstallmentsPaidView pagedInstallmentsPaidView = podamFactory.manufacturePojo(PagedInstallmentsPaidView.class);

    Mockito.when(installmentPaidViewPIIViewRepositoryMock.getPagedInstallmentPaidView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, Pageable.ofSize(1))).thenReturn(pagedInstallmentsPaidView);
    //when
    PagedInstallmentsPaidView result = installmentService.getPagedInstallmentPaidView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, Pageable.ofSize(1));
    //then
    assertNotNull(result);
    assertEquals(pagedInstallmentsPaidView, result);
  }

  @Test
  void whenUpdateInstallmentNotificationDateThenSuccess() {
    // Given
    String accessToken = "ACCESS_TOKEN";
    String operatorExternalUserId = "OPERATOR_EXTERNAL_USER_ID";
    OffsetDateTime dateTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setNotificationDate(dateTime);
    InstallmentDTO installmentDTO = buildInstallmentDTO();
    installmentDTO.setNotificationDate(dateTime);
    UpdateInstallmentNotificationDateRequest request = UpdateInstallmentNotificationDateRequest.builder()
      .debtPositionId(1L)
      .nav(Collections.singletonList("nav"))
      .notificationDate(dateTime)
      .build();

    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();

    Mockito.when(debtPositionServiceMock.getDebtPosition(request.getDebtPositionId())).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionUpdateInstallmentServiceMock.updateInstallment(debtPositionDTO, List.of(installmentDTO), wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(org.apache.commons.lang3.tuple.Pair.of(debtPositionDTO, "workflowId"));

    // When
    String workflowId = installmentService.updateInstallmentNotificationDate(request, wfExecutionParameters, operatorExternalUserId, accessToken);

    // Then
    assertEquals("workflowId", workflowId);
  }

  @Test
  void whenInstallmentIsCancelledThenNoUpdate() {
    // Given
    String accessToken = "ACCESS_TOKEN";
    String operatorExternalUserId = "OPERATOR_EXTERNAL_USER_ID";
    OffsetDateTime dateTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.CANCELLED);

    UpdateInstallmentNotificationDateRequest request = UpdateInstallmentNotificationDateRequest.builder()
      .debtPositionId(1L)
      .nav(Collections.singletonList("nav"))
      .notificationDate(dateTime)
      .build();

    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();

    Mockito.when(debtPositionServiceMock.getDebtPosition(request.getDebtPositionId())).thenReturn(debtPositionDTO);

    // When
    String workflowId = installmentService.updateInstallmentNotificationDate(request, wfExecutionParameters, operatorExternalUserId, accessToken);

    // Then
    assertNull(workflowId);
  }

  @Test
  void whenNavDoesNotMatchThenNoUpdate() {
    // Given
    String accessToken = "ACCESS_TOKEN";
    String operatorExternalUserId = "OPERATOR_EXTERNAL_USER_ID";
    OffsetDateTime dateTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    UpdateInstallmentNotificationDateRequest request = UpdateInstallmentNotificationDateRequest.builder()
      .debtPositionId(1L)
      .nav(Collections.singletonList("1234"))
      .notificationDate(dateTime)
      .build();

    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();

    Mockito.when(debtPositionServiceMock.getDebtPosition(request.getDebtPositionId())).thenReturn(debtPositionDTO);

    // When
    String workflowId = installmentService.updateInstallmentNotificationDate(request, wfExecutionParameters, operatorExternalUserId, accessToken);

    // Then
    assertNull(workflowId);
  }

}
