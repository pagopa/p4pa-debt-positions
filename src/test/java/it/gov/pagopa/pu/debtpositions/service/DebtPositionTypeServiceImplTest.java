package it.gov.pagopa.pu.debtpositions.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeDetailDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionTypeMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeServiceImplTest {

  @Mock
  private DebtPositionTypeRepository debtPositionTypeRepositoryMock;
  @Mock
  private TaxonomyService taxonomyServiceMock;
  @Mock
  private DebtPositionTypeMapper debtPositionTypeMapperMock;

  private DebtPositionTypeService debtPositionTypeService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    debtPositionTypeService = Mockito.spy(
      new DebtPositionTypeServiceImpl(debtPositionTypeRepositoryMock,
        taxonomyServiceMock, debtPositionTypeMapperMock));
  }

  @Test
  void whenGetDebtPositionDetailThenOK() {
    Long brokerId = 1L;

    DebtPositionType debtPositionType = podamFactory.manufacturePojo(
      DebtPositionType.class);
    debtPositionType.setBrokerId(brokerId);

    Taxonomy taxonomy = podamFactory.manufacturePojo(Taxonomy.class);

    DebtPositionTypeDetailDTO expected = new DebtPositionTypeDetailDTO();
    Mockito.when(debtPositionTypeRepositoryMock.findById(anyLong()))
      .thenReturn(Optional.of(debtPositionType));
    Mockito.when(taxonomyServiceMock.getTaxonomyByTaxonomyCode(
        Mockito.eq(debtPositionType.getTaxonomyCode()), anyString()))
      .thenReturn(Optional.of(taxonomy));
    Mockito.when(debtPositionTypeMapperMock.mapToDebtPositionTypeDetailDTO(
        debtPositionType, taxonomy))
      .thenReturn(expected);

    DebtPositionTypeDetailDTO result = debtPositionTypeService.getDebtPositionTypeDetail(
      1L, brokerId, "accessToken");

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expected, result);
  }

  @Test
  void givenDebtPositionTypeNotFoundWhenGetDebtPositionDetailThenThrowException() {
    Mockito.when(debtPositionTypeRepositoryMock.findById(anyLong()))
      .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class,
      () -> debtPositionTypeService.getDebtPositionTypeDetail(1L, 1L,
        "accessToken"));
  }

  @Test
  void givenBrokerIdNotMatchingWhenGetDebtPositionDetailThenReturnNull() {
    DebtPositionType debtPositionType = podamFactory.manufacturePojo(
      DebtPositionType.class);
    debtPositionType.setBrokerId(2L);

    Mockito.when(debtPositionTypeRepositoryMock.findById(anyLong()))
      .thenReturn(Optional.of(debtPositionType));

    DebtPositionTypeDetailDTO result = debtPositionTypeService.getDebtPositionTypeDetail(
      1L, 1L, "accessToken");

    Assertions.assertNull(result);
  }

  @Test
  void givenTaxonomyNotFoundWhenGetDebtPositionDetailThenThrowException() {
    DebtPositionType debtPositionType = podamFactory.manufacturePojo(
      DebtPositionType.class);
    debtPositionType.setBrokerId(1L);

    Mockito.when(debtPositionTypeRepositoryMock.findById(anyLong()))
      .thenReturn(Optional.of(debtPositionType));
    Mockito.when(taxonomyServiceMock.getTaxonomyByTaxonomyCode(
        Mockito.eq(debtPositionType.getTaxonomyCode()), anyString()))
      .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class,
      () -> debtPositionTypeService.getDebtPositionTypeDetail(1L, 1L,
        "accessToken"));
  }
}
