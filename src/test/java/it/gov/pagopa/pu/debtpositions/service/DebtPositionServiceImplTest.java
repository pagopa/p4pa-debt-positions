package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

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

  private DebtPositionServiceImpl debtPositionService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    debtPositionService = new DebtPositionServiceImpl(
      debtPositionRepositoryMock,
      debtPositionSaveServiceMock,
      debtPositionMapperMock,
      debtPositionDeleteServiceMock,
      dataCipherServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      debtPositionRepositoryMock,
      debtPositionSaveServiceMock,
      debtPositionMapperMock,
      debtPositionDeleteServiceMock);
  }

  @Test
  void whenSaveDebtPositionDTOThenInvokeSaveService() {
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();

    // When
    debtPositionService.saveDebtPosition(debtPositionDTO);

    // Then
    Mockito.verify(debtPositionSaveServiceMock).saveDebtPositionDTO(Mockito.same(debtPositionDTO));
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
    Mockito.verifyNoMoreInteractions(debtPositionRepositoryMock, debtPositionMapperMock);
  }

  @Test
  void givenNonExistingDebtPositionDetailWhenGetDebtPositionByInstallmentIdThenThrowNotFoundException() {
    // Given
    Long installmentId = 1L;

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByInstallmentId(installmentId)).thenReturn(null);

    // When
    Assertions.assertThrows(NotFoundException.class, () -> debtPositionService.getDebtPositionByInstallmentId(
      installmentId));

    // Then
    Mockito.verifyNoMoreInteractions(debtPositionRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionMapperMock);
  }

  @Test
  void givenExistingDebtPositionWhenGetDebtPositionsByOrganizationIdAndIuvThenOk() {
    // Given
    Long organizationId = 1L;
    String iuv = "12345678901234567";
    List<DebtPositionDTO> expectedResult = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));
    List<DebtPosition> debtPositions = List.of(podamFactory.manufacturePojo(DebtPosition.class));

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndInstallmentIuv(organizationId, iuv, null)).thenReturn(debtPositions);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPositions.getFirst())).thenReturn(expectedResult.getFirst());

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByOrganizationIdAndIuv(
      organizationId, iuv, null);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionRepositoryMock, debtPositionMapperMock);
  }

  @Test
  void givenExistingDebtPositionWhenGetDebtPositionsByOrganizationIdAndIudThenOk() {
    // Given
    Long organizationId = 1L;
    String iud = "123456789012345678";
    List<DebtPositionDTO> expectedResult = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));
    List<DebtPosition> debtPositions = List.of(podamFactory.manufacturePojo(DebtPosition.class));

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndInstallmentIud(organizationId, iud, null)).thenReturn(debtPositions);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPositions.getFirst())).thenReturn(expectedResult.getFirst());

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByOrganizationIdAndIud(
      organizationId, iud, null);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionRepositoryMock, debtPositionMapperMock);
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
    Mockito.verifyNoMoreInteractions(debtPositionRepositoryMock);
  }

  @Test
  void givenDebtPositionWhenDeleteThenSuccess(){
    // Given
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);

    Mockito.doNothing().when(debtPositionDeleteServiceMock).delete(debtPosition);

    // When
    Assertions.assertDoesNotThrow(() -> debtPositionService.delete(debtPosition));

    // Then
    Mockito.verifyNoMoreInteractions(debtPositionDeleteServiceMock);
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
    Mockito.verify(debtPositionRepositoryMock)
      .findDebtPositionByIupdOrgAndOrganizationId(iupd, orgId);
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
    Mockito.verify(debtPositionRepositoryMock)
      .findDebtPositionByIupdOrgAndOrganizationId(iupd, orgId);
  }

  @Test
  void givenExistingDebtPositionWhenUpdateDebtPositionThenPropagatesIdsAndSaves() {
    // Given
    Long dpId = 10L;
    Long poId = 20L;
    Integer poIndex = 1;
    String iud = "IUD-001";
    Long instId = 30L;
    Integer trIndex = 0;
    Long trId = 40L;

    Transfer trEntity = new Transfer();
    trEntity.setTransferIndex(trIndex);
    trEntity.setTransferId(trId);

    InstallmentNoPII instEntity = new InstallmentNoPII();
    instEntity.setIud(iud);
    instEntity.setInstallmentId(instId);
    instEntity.setTransfers(new TreeSet<>(List.of(trEntity)));

    PaymentOption poEntity = new PaymentOption();
    poEntity.setPaymentOptionId(poId);
    poEntity.setPaymentOptionIndex(poIndex);
    poEntity.setInstallments(new TreeSet<>(List.of(instEntity)));

    DebtPosition entity = new DebtPosition();
    entity.setDebtPositionId(dpId);
    entity.setPaymentOptions(new TreeSet<>(List.of(poEntity)));

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(dpId))
      .thenReturn(entity);

    TransferDTO trDTO = new TransferDTO();
    trDTO.setTransferIndex(trIndex);

    InstallmentDTO instDTO = new InstallmentDTO();
    instDTO.setIud(iud);
    instDTO.setTransfers(List.of(trDTO));

    PaymentOptionDTO poDTO = new PaymentOptionDTO();
    poDTO.setPaymentOptionIndex(poIndex);
    poDTO.setInstallments(List.of(instDTO));

    DebtPositionDTO dpDTO = new DebtPositionDTO();
    dpDTO.setPaymentOptions(List.of(poDTO));

    // When
    debtPositionService.updateDebtPosition(dpId, dpDTO);

    // Then: capture what was sent to save service and assert propagation
    ArgumentCaptor<DebtPositionDTO> captor = ArgumentCaptor.forClass(DebtPositionDTO.class);
    Mockito.verify(debtPositionSaveServiceMock).saveDebtPositionDTO(captor.capture());
    DebtPositionDTO saved = captor.getValue();

    Assertions.assertEquals(dpId, saved.getDebtPositionId());

    PaymentOptionDTO savedPo = saved.getPaymentOptions().getFirst();
    Assertions.assertEquals(poId, savedPo.getPaymentOptionId());
    Assertions.assertEquals(poIndex, savedPo.getPaymentOptionIndex());

    InstallmentDTO savedInst = savedPo.getInstallments().getFirst();
    Assertions.assertEquals(instId, savedInst.getInstallmentId());
    Assertions.assertEquals(iud, savedInst.getIud());

    TransferDTO savedTr = savedInst.getTransfers().getFirst();
    Assertions.assertEquals(trId, savedTr.getTransferId());
    Assertions.assertEquals(trIndex, savedTr.getTransferIndex());

    Mockito.verify(debtPositionRepositoryMock).findEntityGraphByDebtPositionId(dpId);
  }

  @Test
  void givenNonExistingDebtPositionWhenUpdateDebtPositionThenThrowEntityNotFound() {
    // Given
    Long dpId = 999L;
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(dpId))
      .thenReturn(null);

    // Then
    Assertions.assertThrows(EntityNotFoundException.class,
      () -> debtPositionService.updateDebtPosition(dpId, new DebtPositionDTO()));

    Mockito.verify(debtPositionRepositoryMock).findEntityGraphByDebtPositionId(dpId);
    Mockito.verifyNoInteractions(debtPositionSaveServiceMock);
  }

  @Test
  void givenDtoAlreadyHasIdsWhenUpdateDebtPositionThenDoesNotOverwrite() {
    // Given
    Long dpId = 1L;
    Long existingDpIdInDto = 111L;
    Long poIdEntity = 2L;
    Long poIdDto = 222L;
    String iud = "IUD-X";
    Long instIdEntity = 3L;
    Long instIdDto = 333L;
    Integer trIndex = 7;
    Long trIdEntity = 4L;
    Long trIdDto = 444L;

    Transfer trEntity = new Transfer();
    trEntity.setTransferIndex(trIndex);
    trEntity.setTransferId(trIdEntity);

    InstallmentNoPII instEntity = new InstallmentNoPII();
    instEntity.setIud(iud);
    instEntity.setInstallmentId(instIdEntity);
    instEntity.setTransfers(new TreeSet<>(List.of(trEntity)));

    PaymentOption poEntity = new PaymentOption();
    poEntity.setPaymentOptionIndex(5);
    poEntity.setPaymentOptionId(poIdEntity);
    poEntity.setInstallments(new TreeSet<>(List.of(instEntity)));

    DebtPosition entity = new DebtPosition();
    entity.setDebtPositionId(dpId);
    entity.setPaymentOptions(new TreeSet<>(List.of(poEntity)));

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(dpId))
      .thenReturn(entity);

    TransferDTO trDTO = new TransferDTO();
    trDTO.setTransferIndex(trIndex);
    trDTO.setTransferId(trIdDto);

    InstallmentDTO instDTO = new InstallmentDTO();
    instDTO.setIud(iud);
    instDTO.setInstallmentId(instIdDto);
    instDTO.setTransfers(List.of(trDTO));

    PaymentOptionDTO poDTO = new PaymentOptionDTO();
    poDTO.setPaymentOptionIndex(5);
    poDTO.setPaymentOptionId(poIdDto);
    poDTO.setInstallments(List.of(instDTO));

    DebtPositionDTO dpDTO = new DebtPositionDTO();
    dpDTO.setDebtPositionId(existingDpIdInDto);
    dpDTO.setPaymentOptions(List.of(poDTO));

    // When
    debtPositionService.updateDebtPosition(dpId, dpDTO);

    // Then
    ArgumentCaptor<DebtPositionDTO> captor = ArgumentCaptor.forClass(DebtPositionDTO.class);
    Mockito.verify(debtPositionSaveServiceMock).saveDebtPositionDTO(captor.capture());
    DebtPositionDTO saved = captor.getValue();

    Assertions.assertEquals(dpId, saved.getDebtPositionId());
    Assertions.assertEquals(poIdEntity, saved.getPaymentOptions().getFirst().getPaymentOptionId());
    Assertions.assertEquals(instIdEntity, saved.getPaymentOptions().getFirst().getInstallments().getFirst().getInstallmentId());
    Assertions.assertEquals(trIdEntity, saved.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().getTransferId());

    Mockito.verify(debtPositionRepositoryMock).findEntityGraphByDebtPositionId(dpId);
  }

  @Test
  void givenNullOrEmptyCollectionsWhenUpdateDebtPositionThenNoNpeAndSaves() {
    // Given
    Long dpId = 5L;
    DebtPosition entity = new DebtPosition();
    entity.setDebtPositionId(dpId);
    entity.setPaymentOptions(null);
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtPositionId(dpId))
      .thenReturn(entity);

    DebtPositionDTO dto = new DebtPositionDTO();
    dto.setPaymentOptions(null);

    // When / Then
    Assertions.assertDoesNotThrow(() -> debtPositionService.updateDebtPosition(dpId, dto));

    ArgumentCaptor<DebtPositionDTO> captor = ArgumentCaptor.forClass(DebtPositionDTO.class);
    Mockito.verify(debtPositionSaveServiceMock).saveDebtPositionDTO(captor.capture());
    DebtPositionDTO saved = captor.getValue();
    Assertions.assertEquals(dpId, saved.getDebtPositionId());

    Mockito.verify(debtPositionRepositoryMock).findEntityGraphByDebtPositionId(dpId);
  }

  @Test
  void givenAllParametersWhenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenOk() {
    // Given
    String fiscalCode = "ABCDEF12G34H567I";
    PersonEntityType entityType = PersonEntityType.F;
    List<InstallmentStatus> status = List.of(InstallmentStatus.PAID);
    List<DebtPositionOrigin> origin = List.of(DebtPositionOrigin.ORDINARY);
    Long orgId = 123L;
    LocalDate dateFrom = LocalDate.of(2023, 1, 1);
    LocalDate dateTo = LocalDate.of(2023, 12, 31);

    byte[] fiscalCodeHash = new byte[]{1,2,3};
    List<DebtPosition> entities = List.of(podamFactory.manufacturePojo(DebtPosition.class));
    List<DebtPositionDTO> dtos = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));

    Mockito.when(dataCipherServiceMock.hash(fiscalCode)).thenReturn(fiscalCodeHash);
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, orgId, dateFrom, dateTo)).thenReturn(entities);
    Mockito.when(debtPositionMapperMock.mapToDto(entities.getFirst())).thenReturn(dtos.getFirst());

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCode, entityType, status, origin, orgId, dateFrom, dateTo);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(dtos, result);
    Mockito.verify(dataCipherServiceMock).hash(fiscalCode);
    Mockito.verify(debtPositionRepositoryMock).findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, orgId, dateFrom, dateTo);
    Mockito.verify(debtPositionMapperMock).mapToDto(entities.getFirst());
  }

  @Test
  void givenNullStatusAndOriginWhenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenOk() {
    // Given
    String fiscalCode = "XYZABC12D34E567F";
    PersonEntityType entityType = PersonEntityType.F;
    List<InstallmentStatus> status = null;
    List<DebtPositionOrigin> origin = null;
    Long orgId = 456L;
    LocalDate dateFrom = null;
    LocalDate dateTo = null;

    byte[] fiscalCodeHash = new byte[]{4,5,6};
    List<DebtPosition> entities = List.of(podamFactory.manufacturePojo(DebtPosition.class));
    List<DebtPositionDTO> dtos = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));

    Mockito.when(dataCipherServiceMock.hash(fiscalCode)).thenReturn(fiscalCodeHash);
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, orgId, dateFrom, dateTo)).thenReturn(entities);
    Mockito.when(debtPositionMapperMock.mapToDto(entities.getFirst())).thenReturn(dtos.getFirst());

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCode, entityType, status, origin, orgId, dateFrom, dateTo);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(dtos, result);
    Mockito.verify(dataCipherServiceMock).hash(fiscalCode);
    Mockito.verify(debtPositionRepositoryMock).findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, orgId, dateFrom, dateTo);
    Mockito.verify(debtPositionMapperMock).mapToDto(entities.getFirst());
  }

  @Test
  void givenNullOrganizationIdAndDatesWhenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenOk() {
    // Given
    String fiscalCode = "NOPQRS12T34U567V";
    PersonEntityType entityType = PersonEntityType.F;
    List<InstallmentStatus> status = List.of();
    List<DebtPositionOrigin> origin = List.of();
    Long orgId = null;
    LocalDate dateFrom = null;
    LocalDate dateTo = null;

    byte[] fiscalCodeHash = new byte[]{7,8,9};
    List<DebtPosition> entities = List.of(podamFactory.manufacturePojo(DebtPosition.class));
    List<DebtPositionDTO> dtos = List.of(podamFactory.manufacturePojo(DebtPositionDTO.class));

    Mockito.when(dataCipherServiceMock.hash(fiscalCode)).thenReturn(fiscalCodeHash);
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, orgId, dateFrom, dateTo)).thenReturn(entities);
    Mockito.when(debtPositionMapperMock.mapToDto(entities.getFirst())).thenReturn(dtos.getFirst());

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCode, entityType, status, origin, orgId, dateFrom, dateTo);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertIterableEquals(dtos, result);
    Mockito.verify(dataCipherServiceMock).hash(fiscalCode);
    Mockito.verify(debtPositionRepositoryMock).findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, orgId, dateFrom, dateTo);
    Mockito.verify(debtPositionMapperMock).mapToDto(entities.getFirst());
  }

  @Test
  void givenNullFiscalCodeWhenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenThrowsIAE() {
    // Given
    String fiscalCode = null;
    PersonEntityType entityType = PersonEntityType.F;
    List<InstallmentStatus> status = null;
    List<DebtPositionOrigin> origin = null;
    Long orgId = null;
    LocalDate dateFrom = null;
    LocalDate dateTo = null;

    // When / Then
    Assertions.assertThrows(IllegalArgumentException.class, () -> debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCode, entityType, status, origin, orgId, dateFrom, dateTo));
    Mockito.verifyNoInteractions(dataCipherServiceMock, debtPositionRepositoryMock, debtPositionMapperMock);
  }

  @Test
  void givenEmptyResultWhenGetDebtPositionsByDebtorFiscalCodeAndDebtorEntityTypeThenReturnsEmptyList() {
    // Given
    String fiscalCode = "EMPTYFC12G34H567I";
    PersonEntityType entityType = PersonEntityType.F;
    List<InstallmentStatus> status = List.of();
    List<DebtPositionOrigin> origin = List.of();
    Long orgId = 789L;
    LocalDate dateFrom = LocalDate.of(2022, 1, 1);
    LocalDate dateTo = LocalDate.of(2022, 12, 31);

    byte[] fiscalCodeHash = new byte[]{10,11,12};
    List<DebtPosition> entities = List.of();

    Mockito.when(dataCipherServiceMock.hash(fiscalCode)).thenReturn(fiscalCodeHash);
    Mockito.when(debtPositionRepositoryMock.findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, orgId, dateFrom, dateTo)).thenReturn(entities);

    // When
    List<DebtPositionDTO> result = debtPositionService.getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCode, entityType, status, origin, orgId, dateFrom, dateTo);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    Mockito.verify(dataCipherServiceMock).hash(fiscalCode);
    Mockito.verify(debtPositionRepositoryMock).findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
      fiscalCodeHash, entityType, status, origin, orgId, dateFrom, dateTo);
    Mockito.verifyNoInteractions(debtPositionMapperMock);
  }
}
