package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
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

  @Test
  void givenNewNotificationFeeWhenUpdateInstallmentNotificationFeeThenSuccess() {
    // Given
    String nav = "NAV";
    Long orgId = 1L;
    long newNotificationFee = 200L;

    InstallmentDTO installmentDTO = getInstallmentDTO();

    Installment installment = new Installment();
    installment.setStatus(InstallmentStatus.UNPAID);
    List<DebtPositionOrigin> debtPositionOrigin = List.of(DebtPositionOrigin.valueOf( "ORDINARY_SIL"));

    Mockito.when(installmentPIIRepositoryMock.getByOrganizationIdAndNav(orgId, nav, debtPositionOrigin))
      .thenReturn(List.of(installment));
    Mockito.when(installmentMapperMock.mapToDto(installment))
      .thenReturn(installmentDTO);
    Mockito.when(installmentMapperMock.mapToModel(Mockito.any(InstallmentDTO.class)))
      .thenReturn(installment);

    // When
    InstallmentDTO result = installmentService.updateInstallmentNotificationFee(
      orgId, nav, debtPositionOrigin, newNotificationFee);

    // Then
    assertEquals(100L, result.getNotificationFeeCents());
    assertEquals(1100L, result.getAmountCents());
    assertEquals(900L, result.getTransfers().getFirst().getAmountCents());
    Mockito.verify(installmentPIIRepositoryMock).save(Mockito.any(Installment.class));
  }

  @Test
  void givenNewNotificationFeeWhenUpdateInstallmentNotificationFeeThenNotFoundException() {
    // Given
    String nav = "NAV";
    Long orgId = 1L;
    long newNotificationFee = 200L;
    List<DebtPositionOrigin> debtPositionOrigin = List.of(DebtPositionOrigin.valueOf( "ORDINARY_SIL"));

    Mockito.when(installmentPIIRepositoryMock.getByOrganizationIdAndNav(orgId, nav, debtPositionOrigin))
      .thenReturn(Collections.emptyList());

    // When Then
    assertThrows(NotFoundException.class, () ->
      installmentService.updateInstallmentNotificationFee(
        orgId, nav, debtPositionOrigin, newNotificationFee));
  }

  @Test
  void givenNewNotificationFeeWhenUpdateInstallmentNotificationFeeThenConflictErrorException() {
    // Given
    String nav = "NAV";
    Long orgId = 1L;
    long newNotificationFee = 200L;
    InstallmentDTO installmentDTO = getInstallmentDTO();

    Installment installment = new Installment();
    installment.setStatus(InstallmentStatus.UNPAID);
    List<DebtPositionOrigin> debtPositionOrigin = List.of(DebtPositionOrigin.valueOf( "ORDINARY_SIL"));

    Mockito.when(installmentPIIRepositoryMock.getByOrganizationIdAndNav(orgId, nav, debtPositionOrigin))
      .thenReturn(List.of(installment, installment));
    Mockito.when(installmentMapperMock.mapToDto(installment))
      .thenReturn(installmentDTO);

    // When Then
    assertThrows(ConflictErrorException.class, () ->
      installmentService.updateInstallmentNotificationFee(
        orgId, nav, debtPositionOrigin, newNotificationFee));
  }

  @Test
  void givenNewNotificationFeeWhenUpdateInstallmentNotificationFeeThenFiltersCorrectStatus() {
    // Given
    String nav = "NAV";
    Long orgId = 1L;
    long newNotificationFee = 200L;

    Installment paidInstallment = new Installment();
    paidInstallment.setStatus(InstallmentStatus.PAID);
    List<DebtPositionOrigin> debtPositionOrigin = List.of(DebtPositionOrigin.valueOf( "ORDINARY_SIL"));

    Mockito.when(installmentPIIRepositoryMock.getByOrganizationIdAndNav(orgId, nav, debtPositionOrigin))
      .thenReturn(List.of(paidInstallment));

    // When Then
    assertThrows(NotFoundException.class, () ->
      installmentService.updateInstallmentNotificationFee(
        orgId, nav, debtPositionOrigin, newNotificationFee));
  }

  @Test
  void givenNewNotificationFeeWhenUpdateInstallmentNotificationFeeThenNoEligibleTransfer() {
    // Given
    String nav = "NAV";
    Long orgId = 1L;
    long newNotificationFee = 200L;
    List<DebtPositionOrigin> debtPositionOrigin = List.of(DebtPositionOrigin.valueOf( "ORDINARY_SIL"));

    InstallmentDTO installmentDTO = getInstallmentDTO();
    installmentDTO.getTransfers().clear();
    TransferDTO taxTransfer = new TransferDTO();
    taxTransfer.setStampType("MDB");
    taxTransfer.setAmountCents(100L);
    installmentDTO.getTransfers().add(taxTransfer);

    Installment installment = new Installment();
    installment.setStatus(InstallmentStatus.EXPIRED);

    Mockito.when(installmentPIIRepositoryMock.getByOrganizationIdAndNav(orgId, nav, debtPositionOrigin))
      .thenReturn(List.of(installment));
    Mockito.when(installmentMapperMock.mapToDto(installment))
      .thenReturn(installmentDTO);

    // When Then
    assertThrows(IllegalStateException.class, () ->
      installmentService.updateInstallmentNotificationFee(orgId, nav, debtPositionOrigin, newNotificationFee));
  }


  private static InstallmentDTO getInstallmentDTO() {
    InstallmentDTO installmentDTO = new InstallmentDTO();
    installmentDTO.setStatus(InstallmentStatus.UNPAID);
    installmentDTO.setNotificationFeeCents(100L);
    installmentDTO.setAmountCents(1000L);

    List<TransferDTO> transfers = new ArrayList<>();
    TransferDTO transfer1 = new TransferDTO();
    transfer1.setAmountCents(900L);
    transfer1.setStampType("TAX");
    TransferDTO transfer2 = new TransferDTO();
    transfer2.setAmountCents(100L);
    transfers.add(transfer1);
    transfers.add(transfer2);
    installmentDTO.setTransfers(transfers);
    return installmentDTO;
  }
}
