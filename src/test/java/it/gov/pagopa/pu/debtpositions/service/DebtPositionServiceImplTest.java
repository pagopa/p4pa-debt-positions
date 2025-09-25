package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
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

import java.util.List;

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

  private DebtPositionServiceImpl debtPositionService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    debtPositionService = new DebtPositionServiceImpl(
      debtPositionRepositoryMock,
      debtPositionSaveServiceMock,
      debtPositionMapperMock,
      debtPositionDeleteServiceMock
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
}

