package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgServiceImplTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  @Mock
  private DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsServiceMock;

  private DebtPositionTypeOrgService debtPositionTypeOrgService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    debtPositionTypeOrgService = Mockito.spy(new DebtPositionTypeOrgServiceImpl(debtPositionTypeOrgRepository,debtPositionTypeOrgOperatorsServiceMock));
  }

  @Test
  void givenExistingDebtPositionTypeOrgWhenGetIONotificationDetailThenOk() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    IONotificationDTO expectedResult = IONotificationDTO.builder()
      .serviceId(debtPositionTypeOrg.getServiceId())
      .ioTemplateMessage(debtPositionTypeOrg.getIoTemplateMessage())
      .ioTemplateSubject(debtPositionTypeOrg.getIoTemplateSubject())
      .build();

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));

    IONotificationDTO result = debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_CREATED);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository);
  }

  @Test
  void givenNonExistingDebtPositionTypeOrgWhenGetIONotificationDetailThenNotFoundException() {
    Long debtPositionTypeOrgId = 1L;

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)).thenReturn(Optional.empty());

    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_CREATED));

    Assertions.assertEquals("DebtPositionTypeOrg having id %d was not found".formatted(debtPositionTypeOrgId), notFoundException.getMessage());
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository);
  }

  @Test
  void givenFlagNotifyIoFalseWhenGetIONotificationDetailThenNull() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setFlagNotifyIo(false);

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));

    IONotificationDTO result = debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_CREATED);

    Assertions.assertNull(result);
  }

  @Test
  void givenContextUpdateWhenGetIONotificationDetailThenNull() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));

    IONotificationDTO result = debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_UPDATED);

    Assertions.assertNull(result);
  }

  @Test
  void givenExistingDebtPositionTypeOrgWhenDeleteDebtPositionTypeOrgThenOk(){
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeOrgOperatorsServiceMock.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrgId)).thenReturn(10L);
    Mockito.doNothing().when(debtPositionTypeOrgRepository).delete(debtPositionTypeOrg);

    debtPositionTypeOrgService.deleteDebtPositionTypeOrg(debtPositionTypeOrgId);

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository,debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenNonExistingDebtPositionTypeOrgWhenDeleteDebtPositionTypeOrgThenNotFoundException(){
    Long debtPositionTypeOrgId = 1L;

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class, () ->debtPositionTypeOrgService.deleteDebtPositionTypeOrg(debtPositionTypeOrgId));

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenOperatorsToHandleAndNonExistingDebtPositionTypeOrgWhenSaveDebtPositionTypeOrgThenOk(){
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = podamFactory.manufacturePojo(SaveDebtPositionTypeOrgDTO.class);
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(true);
    DebtPositionTypeOrg expectedResult = saveDebtPositionTypeOrgDTO.getDebtPositionTypeOrg();
    expectedResult.setDebtPositionTypeOrgId(null);

    Mockito.when(debtPositionTypeOrgRepository.save(expectedResult))
      .thenReturn(expectedResult);
    Mockito.when(debtPositionTypeOrgOperatorsServiceMock.deleteOperatorsByDebtPositionTypeOrgId(
      expectedResult.getDebtPositionTypeOrgId())).thenReturn(1L);
    Mockito.when(debtPositionTypeOrgOperatorsServiceMock.saveOperators(
      expectedResult.getDebtPositionTypeOrgId(), saveDebtPositionTypeOrgDTO.getEnabledOperators())).thenReturn(null);
    Mockito.when(debtPositionTypeOrgOperatorsServiceMock.deleteOperators(
      expectedResult.getDebtPositionTypeOrgId(), saveDebtPositionTypeOrgDTO.getDisabledOperators())).thenReturn(2);

    DebtPositionTypeOrg result = debtPositionTypeOrgService.saveDebtPositionTypeOrg(
      saveDebtPositionTypeOrgDTO);

    Assertions.assertEquals(expectedResult,result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository,debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenNoOperatorsToHandleAndNonExistingDebtPositionTypeOrgWhenSaveDebtPositionTypeOrgThenOk(){
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(null);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setEnabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setDisabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(false);

    Mockito.when(debtPositionTypeOrgRepository.save(debtPositionTypeOrg))
      .thenReturn(debtPositionTypeOrg);

    DebtPositionTypeOrg result = debtPositionTypeOrgService.saveDebtPositionTypeOrg(
      saveDebtPositionTypeOrgDTO);

    Assertions.assertEquals(debtPositionTypeOrg,result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenNonExistingDebtPositionTypeOrgAndDebtPositionTypeOrgIdPopulatedWhenSaveDebtPositionTypeOrgThenNotFoundException(){
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(1L);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setEnabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setDisabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(false);

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrg.getDebtPositionTypeOrgId()))
            .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class,()->debtPositionTypeOrgService.saveDebtPositionTypeOrg(
            saveDebtPositionTypeOrgDTO));

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenExistingDebtPositionTypeOrgAndUpdatedReadOnlyFieldWhenSaveDebtPositionTypeOrgThenValidationException(){
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(1L);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setEnabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setDisabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(false);

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrg.getDebtPositionTypeOrgId()))
            .thenReturn(Optional.of(podamFactory.manufacturePojo(DebtPositionTypeOrg.class)));

    Assertions.assertThrows(ValidationException.class,()->debtPositionTypeOrgService.saveDebtPositionTypeOrg(
            saveDebtPositionTypeOrgDTO));

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }
}
