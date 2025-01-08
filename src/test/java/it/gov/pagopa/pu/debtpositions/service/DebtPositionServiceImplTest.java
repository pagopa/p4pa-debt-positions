package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceImplTest {

  @Mock
  private DebtPositionRepository debtPositionRepository;

  @Mock
  private DebtPositionMapper debtPositionMapper;

  @InjectMocks
  private DebtPositionServiceImpl debtPositionService;

  @Test
  void givenValidDebtPositionDTOWhenSaveDebtPositionThenReturnSavedDebtPositionDTO() {
    DebtPositionDTO inputDto = new DebtPositionDTO();
    inputDto.setDebtPositionId(1L);

    DebtPosition mappedEntity = new DebtPosition();
    mappedEntity.setDebtPositionId(1L);

    DebtPosition savedEntity = new DebtPosition();
    savedEntity.setDebtPositionId(1L);

    DebtPositionDTO outputDto = new DebtPositionDTO();
    outputDto.setDebtPositionId(1L);

    Mockito.when(debtPositionMapper.mapToModel(inputDto)).thenReturn(mappedEntity);
    Mockito.when(debtPositionRepository.save(mappedEntity)).thenReturn(savedEntity);
    Mockito.when(debtPositionMapper.mapToDto(savedEntity)).thenReturn(outputDto);

    DebtPositionDTO result = debtPositionService.saveDebtPosition(inputDto);

    assertNotNull(result);
    assertEquals(1L, result.getDebtPositionId());
    Mockito.verify(debtPositionMapper).mapToModel(inputDto);
    Mockito.verify(debtPositionRepository).save(mappedEntity);
    Mockito.verify(debtPositionMapper).mapToDto(savedEntity);
  }

  @Test
  void givenRepositoryFailsWhenSaveDebtPositionThenThrowRuntimeException() {
    DebtPositionDTO inputDto = new DebtPositionDTO();
    DebtPosition mappedEntity = new DebtPosition();
    Mockito.when(debtPositionMapper.mapToModel(inputDto)).thenReturn(mappedEntity);
    Mockito.when(debtPositionRepository.save(mappedEntity)).thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class, () -> debtPositionService.saveDebtPosition(inputDto));
    Mockito.verify(debtPositionMapper).mapToModel(inputDto);
    Mockito.verify(debtPositionRepository).save(mappedEntity);
  }

  @Test
  void givenMapperFailsWhenConvertEntityToDTOThenThrowRuntimeException() {
    DebtPositionDTO inputDto = new DebtPositionDTO();
    DebtPosition mappedEntity = new DebtPosition();
    DebtPosition savedEntity = new DebtPosition();

    Mockito.when(debtPositionMapper.mapToModel(inputDto)).thenReturn(mappedEntity);
    Mockito.when(debtPositionRepository.save(mappedEntity)).thenReturn(savedEntity);
    Mockito.when(debtPositionMapper.mapToDto(savedEntity)).thenThrow(new RuntimeException("Mapping error"));

    assertThrows(RuntimeException.class, () -> debtPositionService.saveDebtPosition(inputDto));
    Mockito.verify(debtPositionMapper).mapToModel(inputDto);
    Mockito.verify(debtPositionRepository).save(mappedEntity);
    Mockito.verify(debtPositionMapper).mapToDto(savedEntity);
  }
}

