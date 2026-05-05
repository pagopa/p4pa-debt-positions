package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.mapper.pii.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.*;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionMapperTest {

  @Mock
  private PaymentOptionMapper paymentOptionMapperMock;
  @Mock
  private InstallmentPIIMapper installmentPIIMapperMock;
  @Mock
  private OrganizationService organizationServiceMock;

  private DebtPositionMapper debtPositionMapper;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  private final String accessToken = "accessToken";

  @BeforeEach
  void setUp() {
    debtPositionMapper = new DebtPositionMapper(
      paymentOptionMapperMock,
      installmentPIIMapperMock,
      organizationServiceMock
    );
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      paymentOptionMapperMock,
      installmentPIIMapperMock,
      organizationServiceMock
    );
  }

  @Test
  void givenValidDebtPositionDTO_whenMapToModel_thenReturnDebtPositionAndInstallmentMap() {
    //GIVEN
    DebtPosition debtPositionExpected = buildDebtPosition();
    debtPositionExpected.setStatus(DebtPositionStatus.UNPAID);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    PaymentOption paymentOption = buildPaymentOption();

    Mockito.when(paymentOptionMapperMock.mapToModel(buildPaymentOptionDTO())).thenReturn(paymentOption);
    Mockito.doNothing().when(
      organizationServiceMock).verifyStationId(
        debtPositionDTO.getOrganizationId(),
        debtPositionDTO.getStationId(),
        accessToken
      );
    //WHEN
    DebtPosition result = debtPositionMapper.mapToModel(debtPositionDTO);
    //THEN
    reflectionEqualsByName(debtPositionExpected, result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    checkNotNullFields(result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }

  @Test
  void givenValidDebtPositionDTOWithNoStationID_whenMapToModel_thenReturnDebtPositionAndInstallmentMapWithDefaultStationId() {
    //GIVEN
    DebtPosition debtPositionExpected = buildDebtPosition();
    debtPositionExpected.setStationId("defaultStationId");
    debtPositionExpected.setStatus(DebtPositionStatus.UNPAID);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStationId(null);

    PaymentOption paymentOption = buildPaymentOption();

    Mockito.when(paymentOptionMapperMock.mapToModel(buildPaymentOptionDTO())).thenReturn(paymentOption);
    Mockito.when(
      organizationServiceMock.getDefaultOrganizationStation(
        ArgumentMatchers.eq(debtPositionDTO.getOrganizationId()),
        ArgumentMatchers.eq(accessToken)
      )
    ).thenReturn(OrganizationStation.builder().organizationId(1L).stationId("defaultStationId").build());
    //WHEN
    DebtPosition result = debtPositionMapper.mapToModel(debtPositionDTO);
    //THEN
    reflectionEqualsByName(debtPositionExpected, result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    checkNotNullFields(result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }

  @Test
  void givenMapToDtoThenOk() {
    //GIVEN
    DebtPositionDTO expectedDpDTO = buildDebtPositionDTO();
    expectedDpDTO.setStatus(DebtPositionStatus.TO_SYNC);
    DebtPosition dp = buildDebtPosition();

    configureMapper2DTOMocks(expectedDpDTO, dp);

    //WHEN
    DebtPositionDTO result = debtPositionMapper.mapToDto(dp);
    //THEN
    checkNotNullFields(result);
    reflectionEqualsByName(expectedDpDTO, result);
    // nested lists should be modifiable
    result.getPaymentOptions().forEach(po -> po.getInstallments().clear());
    result.getPaymentOptions().clear();
  }

  private void configureMapper2DTOMocks(DebtPositionDTO expectedDpDTO, DebtPosition dp) {
    List<InstallmentDTO> expectedInstDtos = expectedDpDTO.getPaymentOptions().stream().flatMap(po -> po.getInstallments().stream()).toList();
    Mockito.when(installmentPIIMapperMock.mapAll(dp.getPaymentOptions().stream().flatMap(po -> po.getInstallments().stream()).toList()))
      .thenReturn(expectedInstDtos);
    Mockito.when(paymentOptionMapperMock.mapToDto(dp.getPaymentOptions().getFirst(), new ArrayList<>(expectedInstDtos)))
      .thenReturn(buildPaymentOptionDTO());
  }

  @Test
  void givenPagedDebtPositionsThenOk() {
    Pageable pageable = Pageable.ofSize(5);
    Page<DebtPosition> pageDebtPositionsDTO = new PageImpl<>(List.of(new DebtPosition()), pageable, 1);

    List<DebtPositionDTO> expectedDpDTOs = List.of(new DebtPositionDTO());

    debtPositionMapper = Mockito.spy(debtPositionMapper);
    Mockito
      .doReturn(expectedDpDTOs)
      .when(debtPositionMapper)
      .mapAllToDto(pageDebtPositionsDTO.getContent());

    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder()
      .content(expectedDpDTOs)
      .size(5L)
      .totalElements(1L)
      .number(0L)
      .totalPages(1L)
      .build();

    PagedDebtPositions result = debtPositionMapper.mapToPagedDebtPositions(pageDebtPositionsDTO);

    checkNotNullFields(result);
    reflectionEqualsByName(expectedPagedDebtPositions, result);
    Assertions.assertSame(expectedDpDTOs, result.getContent());
  }

  @Test
  void givenNullPagedDebtPositionsThenOk() {
    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder()
      .content(List.of())
      .build();

    PagedDebtPositions result = debtPositionMapper.mapToPagedDebtPositions(null);

    assertEquals(expectedPagedDebtPositions, result);
  }

  @Test
  void givenEmptyPagedDebtPositionsThenOk() {
    Pageable pageable = Pageable.ofSize(5);
    Page<DebtPosition> pageDebtPositionsDTO = new PageImpl<>(List.of(), pageable, 1);

    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder()
      .content(List.of())
      .size(5L)
      .totalElements(1L)
      .number(0L)
      .totalPages(1L)
      .build();

    PagedDebtPositions result = debtPositionMapper.mapToPagedDebtPositions(pageDebtPositionsDTO);

    assertEquals(expectedPagedDebtPositions, result);
  }

  @Test
  void givenNotPageablePagedDebtPositionsThenOk() {
    Pageable pageable = Pageable.unpaged();
    Page<DebtPosition> pageDebtPositionsDTO = new PageImpl<>(List.of(), pageable, 1);

    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder()
      .content(List.of())
      .build();

    PagedDebtPositions result = debtPositionMapper.mapToPagedDebtPositions(pageDebtPositionsDTO);

    assertEquals(expectedPagedDebtPositions, result);
  }

  @Test
  void whenMapAllThenOk() {
    // Given
    DebtPosition dp1 = podamFactory.manufacturePojo(DebtPosition.class);
    DebtPosition dp2 = podamFactory.manufacturePojo(DebtPosition.class);
    List<DebtPosition> dps = List.of(dp1, dp2);

    List<InstallmentNoPII> installments = new ArrayList<>();
    List<InstallmentDTO> installmentDTOS = new ArrayList<>();
    Map<Long, ArrayList<InstallmentDTO>> poId2InstDTO = new HashMap<>();

    processDpMapAllDto(dp1, installments, installmentDTOS, poId2InstDTO);
    processDpMapAllDto(dp2, installments, installmentDTOS, poId2InstDTO);

    Mockito.when(installmentPIIMapperMock.mapAll(installments))
      .thenReturn(installmentDTOS);

    dps.forEach(dp ->
      dp.getPaymentOptions().forEach(po -> {
        ArrayList<InstallmentDTO> poDtoInst = poId2InstDTO.get(po.getPaymentOptionId());
          Mockito.when(paymentOptionMapperMock.mapToDto(po, poDtoInst))
            .thenReturn(podamFactory.manufacturePojo(PaymentOptionDTO.class)
              .debtPositionId(dp.getDebtPositionId())
              .paymentOptionId(po.getPaymentOptionId())
              .installments(poDtoInst)
            );
        }
      )
    );

    // When
    List<DebtPositionDTO> result = debtPositionMapper.mapAllToDto(dps);

    // Then
    Assertions.assertNotNull(result);

    assertDpMapAllDto(dp1, result.getFirst());
    assertDpMapAllDto(dp2, result.get(1));
  }

  private void processDpMapAllDto(DebtPosition dp, List<InstallmentNoPII> installments, List<InstallmentDTO> installmentDTOS, Map<Long, ArrayList<InstallmentDTO>> poId2InstDTO) {
    dp.getPaymentOptions()
      .forEach(po -> {
          po.setDebtPositionId(dp.getDebtPositionId());
          po.getInstallments()
            .forEach(i -> {
              Long poId = po.getPaymentOptionId();
              i.setPaymentOptionId(poId);

              InstallmentDTO iDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
              iDTO.setInstallmentId(i.getInstallmentId());
              iDTO.setPaymentOptionId(poId);

              installments.add(i);
              installmentDTOS.add(iDTO);

              poId2InstDTO.computeIfAbsent(poId, x -> new ArrayList<>())
                .add(iDTO);
            });
        }
      );
  }

  private void assertDpMapAllDto(DebtPosition dp, DebtPositionDTO dpDTO) {
    Assertions.assertEquals(dp.getDebtPositionId(), dpDTO.getDebtPositionId());
    int i=0;
    for (PaymentOption po : dp.getPaymentOptions()) {
      PaymentOptionDTO poDto = dpDTO.getPaymentOptions().get(i++);
      Assertions.assertEquals(po.getDebtPositionId(), poDto.getDebtPositionId());
      Assertions.assertEquals(po.getPaymentOptionId(), poDto.getPaymentOptionId());

      int j=0;
      for (InstallmentNoPII inst : po.getInstallments()) {
        InstallmentDTO iDto = poDto.getInstallments().get(j++);
        Assertions.assertEquals(inst.getPaymentOptionId(), iDto.getPaymentOptionId());
        Assertions.assertEquals(inst.getInstallmentId(), iDto.getInstallmentId());
      }
    }
  }
}
