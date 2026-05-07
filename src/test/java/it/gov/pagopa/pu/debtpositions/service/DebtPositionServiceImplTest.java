package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.common.pii.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.filters.LocalDateTimeIntervalFilter;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.DebtorDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.pages.PagedDebtorUnpaidDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceImplTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private DebtPositionSaveService debtPositionSaveServiceMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;
  @Mock
  private DebtPositionDeleteService debtPositionDeleteServiceMock;
  @Mock
  private DataCipherService dataCipherServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private PagedDebtorUnpaidDebtPositionMapper pagedDebtorUnpaidDebtPositionMapperMock;
  @Mock
  private DebtorDebtPositionMapper debtorDebtPositionMapperMock;

  private DebtPositionServiceImpl debtPositionService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    debtPositionService = new DebtPositionServiceImpl(
      debtPositionRepositoryMock,
      debtPositionSaveServiceMock,
      debtPositionMapperMock,
      debtPositionDeleteServiceMock,
      dataCipherServiceMock,
      debtPositionTypeOrgRepositoryMock,
      pagedDebtorUnpaidDebtPositionMapperMock,
      debtorDebtPositionMapperMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      debtPositionRepositoryMock,
      debtPositionSaveServiceMock,
      debtPositionMapperMock,
      debtPositionDeleteServiceMock,
      pagedDebtorUnpaidDebtPositionMapperMock,
      debtorDebtPositionMapperMock);
  }

  @Test
  void whenSaveDebtPositionDTOThenInvokeSaveService() {
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();

    // When
    String accessToken = "accessToken";
    debtPositionService.saveDebtPosition(debtPositionDTO, accessToken);

    // Then
    Mockito.verify(debtPositionSaveServiceMock).saveDebtPositionDTO(Mockito.same(debtPositionDTO), Mockito.same(accessToken));
  }

  @Test
  void whenSaveDebtPositionThenInvokeSaveService(){
    // Given
    DebtPosition debtPosition = new DebtPosition();

    // When
    debtPositionService.saveDebtPosition(debtPosition);

    // Then
    Mockito.verify(debtPositionSaveServiceMock).saveDebtPosition(Mockito.same(debtPosition));
  }

  @Test
  void givenExistingDebtPositionWhenGetDebtPositionThenOk() {
    // Given
    Long debtPositionId = 1L;
    DebtPositionDTO expectedResult = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(expectedResult);

    // When
    DebtPositionDTO result = debtPositionService.getDebtPosition(
      debtPositionId);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertSame(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionRepositoryMock, debtPositionMapperMock);
  }

  @Test
  void givenNonExistingDebtPositionDetailWhenGetDebtPositionThenThrowNotFoundException() {
    // Given
    Long debtPositionId = 1L;

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(null);

    // When
    Assertions.assertThrows(NotFoundException.class, () -> debtPositionService.getDebtPosition(
      debtPositionId));

    // Then
    Mockito.verifyNoMoreInteractions(debtPositionRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionMapperMock);
  }

  @Test
  void givenExistingDebtPositionWhenGetDebtPositionByInstallmentIdThenOk() {
    // Given
    Long installmentId = 1L;
    DebtPositionDTO expectedResult = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(installmentId)).thenReturn(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(expectedResult);

    // When
    DebtPositionDTO result = debtPositionService.getDebtPositionByInstallmentId(
      installmentId);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNonExistingDebtPositionDetailWhenGetDebtPositionByInstallmentIdThenThrowNotFoundException() {
    // Given
    Long installmentId = 1L;

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(installmentId)).thenReturn(null);

    // When, Then
    Assertions.assertThrows(NotFoundException.class, () -> debtPositionService.getDebtPositionByInstallmentId(
      installmentId));
  }

  @Test
  void givenExistingDebtPositionWhenGetDebtPositionsByOrganizationIdAndNavThenOk() {
    // Given
    Long organizationId = 1L;
    String nav = "301000000000000245";
    List<DebtPositionDTO> expectedResult = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));
    List<DebtPosition> debtPositions = List.of(podamFactory.manufacturePojo(DebtPosition.class));

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndNav(organizationId, nav, null)).thenReturn(debtPositions);
    Mockito.when(debtPositionMapperMock.mapAllToDto(debtPositions)).thenReturn(expectedResult);

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByOrganizationIdAndNav(
      organizationId, nav, null);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(expectedResult, result);
  }

  @Test
  void givenExistingDebtPositionWhenGetDebtPositionsByOrganizationIdAndIuvThenOk() {
    // Given
    Long organizationId = 1L;
    String iuv = "12345678901234567";
    List<DebtPositionDTO> expectedResult = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));
    List<DebtPosition> debtPositions = List.of(podamFactory.manufacturePojo(DebtPosition.class));

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndInstallmentIuv(organizationId, iuv, null)).thenReturn(debtPositions);
    Mockito.when(debtPositionMapperMock.mapAllToDto(debtPositions)).thenReturn(expectedResult);

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByOrganizationIdAndIuv(
      organizationId, iuv, null);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(expectedResult, result);
  }

  @Test
  void givenExistingDebtPositionWhenGetDebtPositionsByOrganizationIdAndIudThenOk() {
    // Given
    Long organizationId = 1L;
    String iud = "123456789012345678";
    List<DebtPositionDTO> expectedResult = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));
    List<DebtPosition> debtPositions = List.of(podamFactory.manufacturePojo(DebtPosition.class));

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndInstallmentIud(organizationId, iud, null)).thenReturn(debtPositions);
    Mockito.when(debtPositionMapperMock.mapAllToDto(debtPositions)).thenReturn(expectedResult);

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByOrganizationIdAndIud(
      organizationId, iud, null);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(expectedResult, result);
  }

  @Test
  void givenPagedDebtPositionWhenGetPagedDebtPositionsThenSuccess() {
    // Given
    Long ingestionFlowFileId = 1L;
    Pageable pageable = Pageable.ofSize(5);
    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder()
      .content(List.of(buildDebtPositionDTO()))
      .size(5L).build();

    Page<DebtPosition> pageDebtPosition = new PageImpl<>(List.of(buildDebtPosition()), pageable, 1);

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByIngestionFlowFileIdAndStatusToExclude(ingestionFlowFileId, null, pageable)).thenReturn(pageDebtPosition);
    Mockito.when(debtPositionMapperMock.mapToPagedDebtPositions(pageDebtPosition)).thenReturn(expectedPagedDebtPositions);

    // When
    PagedDebtPositions result = debtPositionService.getPagedDebtPositionsByIngestionFlowFileId(ingestionFlowFileId, null, pageable);

    // Then
    assertEquals(result, expectedPagedDebtPositions);
  }

  @Test
  void givenDebtPositionWhenGetDebtPositionNoPIIThenSuccess(){
    // Given
    Long debtPositionId = 1L;
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(debtPositionId)).thenReturn(debtPosition);

    // When
    DebtPosition result = debtPositionService.getDebtPositionNoPII(debtPositionId);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertSame(debtPosition, result);
  }

  @Test
  void givenDebtPositionWhenDeleteThenSuccess(){
    // Given
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);

    Mockito.doNothing().when(debtPositionDeleteServiceMock).delete(debtPosition);

    // When, Then
    Assertions.assertDoesNotThrow(() -> debtPositionService.delete(debtPosition));
  }

  @Test
  void givenIupdAndOrgIdWhenGetDebtPositionByIupdAndOrganizationIdThenOk() {
    // Given
    String iupd = "IUPD-123";
    Long orgId = 42L;
    DebtPosition entity = podamFactory.manufacturePojo(DebtPosition.class);
    Mockito.when(debtPositionRepositoryMock.findDebtPositionByIupdOrgAndOrganizationId(iupd, orgId))
      .thenReturn(Optional.of(entity));

    // When
    Optional<DebtPosition> result =
      debtPositionService.getDebtPositionByIupdAndOrganizationId(iupd, orgId);

    // Then
    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(entity, result.get());
  }

  @Test
  void givenIupdAndOrgIdWhenGetDebtPositionByIupdAndOrganizationIdThenEmpty() {
    // Given
    String iupd = "IUPD-123";
    Long orgId = 42L;
    Mockito.when(debtPositionRepositoryMock.findDebtPositionByIupdOrgAndOrganizationId(iupd, orgId))
      .thenReturn(Optional.empty());

    // When
    Optional<DebtPosition> result =
      debtPositionService.getDebtPositionByIupdAndOrganizationId(iupd, orgId);

    // Then
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void givenAllParametersWhenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenOk() {
    // Given
    String fiscalCode = "ABCDEF12G34H567I";
    PersonEntityType entityType = PersonEntityType.F;
    List<InstallmentStatus> status = List.of(InstallmentStatus.PAID);
    List<DebtPositionOrigin> origin = List.of(DebtPositionOrigin.ORDINARY);
    List<Long> orgIds = List.of(123L);
    LocalDateTimeIntervalFilter dateTimeIntervalFilter = new LocalDateTimeIntervalFilter(LocalDateTime.of(2023, 1, 1, 0, 0),
      LocalDateTime.of(2023, 12, 31, 23, 59));

    byte[] fiscalCodeHash = new byte[]{1,2,3};
    List<DebtPosition> entities = List.of(podamFactory.manufacturePojo(DebtPosition.class));
    List<DebtPositionDTO> dtos = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));

    Mockito.when(dataCipherServiceMock.hash(fiscalCode)).thenReturn(fiscalCodeHash);
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, Collections.emptyList(), orgIds, dateTimeIntervalFilter)).thenReturn(entities);
    Mockito.when(debtPositionMapperMock.mapAllToDto(entities)).thenReturn(dtos);

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCode, entityType, status, origin, Collections.emptyList(), orgIds, dateTimeIntervalFilter);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(dtos, result);
  }

  @Test
  void givenNullStatusAndOriginWhenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenOk() {
    // Given
    String fiscalCode = "XYZABC12D34E567F";
    PersonEntityType entityType = PersonEntityType.F;
    List<InstallmentStatus> status = null;
    List<DebtPositionOrigin> origin = null;
    List<Long> orgIds = List.of(456L);
    LocalDateTimeIntervalFilter dateTimeIntervalFilter = new LocalDateTimeIntervalFilter(null, null);

    byte[] fiscalCodeHash = new byte[]{4,5,6};
    List<DebtPosition> entities = List.of(podamFactory.manufacturePojo(DebtPosition.class));
    List<DebtPositionDTO> dtos = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));

    Mockito.when(dataCipherServiceMock.hash(fiscalCode)).thenReturn(fiscalCodeHash);
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, Collections.emptyList(), orgIds, dateTimeIntervalFilter)).thenReturn(entities);
    Mockito.when(debtPositionMapperMock.mapAllToDto(entities)).thenReturn(dtos);

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCode, entityType, status, origin, Collections.emptyList(), orgIds, dateTimeIntervalFilter);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(dtos, result);
  }

  @Test
  void givenNullFiscalCodeWhenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenThrowsIAE() {
    // Given
    String fiscalCode = null;
    PersonEntityType entityType = PersonEntityType.F;
    List<InstallmentStatus> status = null;
    List<DebtPositionOrigin> origin = null;
    List<String> debtPositionTypeOrgCodesToExclude = Collections.emptyList();
    List<Long> orgIds = List.of();
    LocalDateTimeIntervalFilter dateTimeIntervalFilter = new LocalDateTimeIntervalFilter(null, null);

    // When, Then
    Assertions.assertThrows(InvalidValueException.class, () -> debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCode, entityType, status, origin, debtPositionTypeOrgCodesToExclude, orgIds, dateTimeIntervalFilter));
  }

  @Test
  void givenEmptyResultWhenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenReturnsEmptyList() {
    // Given
    String fiscalCode = "EMPTYFC12G34H567I";
    PersonEntityType entityType = PersonEntityType.F;
    List<InstallmentStatus> status = List.of();
    List<DebtPositionOrigin> origin = List.of();
    List<Long> orgIds = List.of(789L);
    LocalDateTimeIntervalFilter dateTimeIntervalFilter = new LocalDateTimeIntervalFilter(LocalDateTime.of(2023, 1, 1, 0, 0),
      LocalDateTime.of(2023, 12, 31, 23, 59));

    byte[] fiscalCodeHash = new byte[]{10,11,12};
    List<DebtPosition> entities = List.of();

    Mockito.when(dataCipherServiceMock.hash(fiscalCode)).thenReturn(fiscalCodeHash);
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, Collections.emptyList(), orgIds, dateTimeIntervalFilter)).thenReturn(entities);
    Mockito.when(debtPositionMapperMock.mapAllToDto(entities)).thenReturn(Collections.emptyList());

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCode, entityType, status, origin, Collections.emptyList(), orgIds, dateTimeIntervalFilter);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void givenValidInputWhenGetPagedDebtorUnpaidDebtPositionThenReturnMappedResult() {
    // given
    String debtorFiscalCode = "debtorFiscalCode";
    byte[] hashedDebtorFiscalCode = {1, 2, 3};
    List<Long> organizationIds = List.of(1L);
    Pageable pageable = Pageable.ofSize(10);

    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionId(100L);
    dp.setDebtPositionTypeOrgId(200L);

    DebtPositionTypeOrg typeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Page<DebtPosition> page = new PageImpl<>(List.of(dp));

    PagedDebtorUnpaidDebtPositionDTO expected = podamFactory.manufacturePojo(PagedDebtorUnpaidDebtPositionDTO.class);

    Mockito.when(dataCipherServiceMock.hash(debtorFiscalCode)).thenReturn(hashedDebtorFiscalCode);

    Mockito.when(debtPositionRepositoryMock
        .findEntityGraphUnpaidOrdinaryDebtPositionByDebtorFiscalCode(debtorFiscalCode, organizationIds, pageable))
      .thenReturn(page);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(200L))
      .thenReturn(Optional.of(typeOrg));

    Mockito.when(pagedDebtorUnpaidDebtPositionMapperMock
        .map(page, Map.of(100L, typeOrg), hashedDebtorFiscalCode ))
      .thenReturn(expected);

    // when
    PagedDebtorUnpaidDebtPositionDTO result =
      debtPositionService.getPagedDebtorUnpaidDebtPosition(debtorFiscalCode, organizationIds, pageable);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertSame(expected, result);
  }


  @Test
  void givenEmptyPageWhenGetPagedDebtorUnpaidDebtPositionThenReturnMappedResultWithEmptyMap() {
    // given
    String debtorFiscalCode = "fiscal";
    byte[] hashedDebtorFiscalCode = {1, 2, 3};
    List<Long> organizationIds = List.of(1L);
    Pageable pageable = Pageable.ofSize(5);

    Page<DebtPosition> emptyPage = new PageImpl<>(List.of());

    PagedDebtorUnpaidDebtPositionDTO expected =
      podamFactory.manufacturePojo(PagedDebtorUnpaidDebtPositionDTO.class);

    Mockito.when(dataCipherServiceMock.hash(debtorFiscalCode)).thenReturn(hashedDebtorFiscalCode);

    Mockito.when(debtPositionRepositoryMock
        .findEntityGraphUnpaidOrdinaryDebtPositionByDebtorFiscalCode(debtorFiscalCode, organizationIds, pageable))
      .thenReturn(emptyPage);

    Mockito.when(pagedDebtorUnpaidDebtPositionMapperMock
        .map(emptyPage, Collections.emptyMap(), hashedDebtorFiscalCode))
      .thenReturn(expected);

    // when
    PagedDebtorUnpaidDebtPositionDTO result =
      debtPositionService.getPagedDebtorUnpaidDebtPosition(debtorFiscalCode, organizationIds, pageable);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertSame(expected, result);
  }

  @Test
  void givenMissingTypeOrgWhenGetPagedDebtorUnpaidDebtPositionThenThrowNotFoundException() {
    // Given
    String debtorFiscalCode = "fiscal";
    List<Long> organizationIds = List.of(1L);
    Pageable pageable = Pageable.ofSize(3);

    DebtPosition dp = podamFactory.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionId(50L);
    dp.setDebtPositionTypeOrgId(70L);

    Page<DebtPosition> page = new PageImpl<>(List.of(dp));

    Mockito.when(debtPositionRepositoryMock
        .findEntityGraphUnpaidOrdinaryDebtPositionByDebtorFiscalCode(debtorFiscalCode, organizationIds, pageable))
      .thenReturn(page);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(70L))
      .thenReturn(Optional.empty());

    // When, Then
    Assertions.assertThrows(
      NotFoundException.class,
      () -> debtPositionService.getPagedDebtorUnpaidDebtPosition(debtorFiscalCode, organizationIds, pageable)
    );
  }

  @Test
  void givenParamsWhenGetPagedDebtorUnpaidDebtPositionThenVerifyRepositoryCall() {
    // given
    String debtorFiscalCode = "CODE";
    byte[] hashedDebtorFiscalCode = {1, 2, 3};
    List<Long> organizationIds = List.of(10L, 20L);
    Pageable pageable = Pageable.ofSize(2);

    Page<DebtPosition> page = new PageImpl<>(List.of());

    Mockito.when(dataCipherServiceMock.hash(debtorFiscalCode)).thenReturn(hashedDebtorFiscalCode);

    Mockito.when(debtPositionRepositoryMock
        .findEntityGraphUnpaidOrdinaryDebtPositionByDebtorFiscalCode(debtorFiscalCode, organizationIds, pageable))
      .thenReturn(page);

    PagedDebtorUnpaidDebtPositionDTO expectedResult = new PagedDebtorUnpaidDebtPositionDTO();
    Mockito.when(pagedDebtorUnpaidDebtPositionMapperMock
        .map(page, Collections.emptyMap(), hashedDebtorFiscalCode))
      .thenReturn(expectedResult);

    // when
    PagedDebtorUnpaidDebtPositionDTO result = debtPositionService.getPagedDebtorUnpaidDebtPosition(debtorFiscalCode, organizationIds, pageable);

    // then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenExistingDebtPositionWhenGetDebtorUnpaidDebtPositionOverviewThenOk() {
    // Given
    Long debtPositionId = 1L;
    String fiscalCode = "debtorFiscalCode";
    Long organizationId = 10L;
    byte[] hashedDebtorFiscalCode = {1, 2, 3};

    DebtPosition entity = podamFactory.manufacturePojo(DebtPosition.class);
    entity.setDebtPositionTypeOrgId(99L);

    DebtPositionTypeOrg typeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    DebtorDebtPositionDTO expected = podamFactory.manufacturePojo(DebtorDebtPositionDTO.class);

    Mockito.when(
      debtPositionRepositoryMock.findEntityGraphUnpaidOrPaidDebtPositionsByDebtorFiscalCode(
        debtPositionId, fiscalCode, organizationId)
    ).thenReturn(entity);

    Mockito.when(
      debtPositionTypeOrgRepositoryMock.findById(99L)
    ).thenReturn(Optional.of(typeOrg));

    Mockito.when(dataCipherServiceMock.hash(fiscalCode)).thenReturn(hashedDebtorFiscalCode);

    Mockito.when(
      debtorDebtPositionMapperMock.map(entity, typeOrg, hashedDebtorFiscalCode)
    ).thenReturn(expected);

    // When
    DebtorDebtPositionDTO result = debtPositionService.getDebtorUnpaidDebtPositionOverview(
      debtPositionId, fiscalCode, organizationId
    );

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertSame(expected, result);
  }


  @Test
  void givenMissingDebtPositionWhenGetDebtorUnpaidDebtPositionOverviewThenThrowNotFoundException() {
    // Given
    Long debtPositionId = 1L;
    String fiscalCode = "debtorFiscalCode";
    Long organizationId = 10L;

    Mockito.when(
      debtPositionRepositoryMock.findEntityGraphUnpaidOrPaidDebtPositionsByDebtorFiscalCode(
        debtPositionId, fiscalCode, organizationId)
    ).thenReturn(null);

    // When / Then
    Assertions.assertThrows(
      NotFoundException.class,
      () -> debtPositionService.getDebtorUnpaidDebtPositionOverview(
        debtPositionId, fiscalCode, organizationId
      )
    );
  }

  @Test
  void givenMissingTypeOrgWhenGetDebtorUnpaidDebtPositionOverviewThenThrowNotFoundException() {
    // Given
    Long debtPositionId = 1L;
    String fiscalCode = "debtorFiscalCode";
    Long organizationId = 10L;

    DebtPosition entity = podamFactory.manufacturePojo(DebtPosition.class);
    entity.setDebtPositionTypeOrgId(123L);

    Mockito.when(
      debtPositionRepositoryMock.findEntityGraphUnpaidOrPaidDebtPositionsByDebtorFiscalCode(
        debtPositionId, fiscalCode, organizationId)
    ).thenReturn(entity);

    Mockito.when(
      debtPositionTypeOrgRepositoryMock.findById(123L)
    ).thenReturn(Optional.empty());

    //Then
    Assertions.assertThrows(
      NotFoundException.class,
      () -> debtPositionService.getDebtorUnpaidDebtPositionOverview(
        debtPositionId, fiscalCode, organizationId)
    );
  }

}
