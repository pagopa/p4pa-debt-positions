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
import org.springframework.beans.BeanUtils;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgServiceImplTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsServiceMock;

  private DebtPositionTypeOrgService debtPositionTypeOrgService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    debtPositionTypeOrgService = Mockito.spy(new DebtPositionTypeOrgServiceImpl(debtPositionTypeOrgRepositoryMock,debtPositionTypeOrgOperatorsServiceMock));
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

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));

    IONotificationDTO result = debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_CREATED);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
  }

  @Test
  void givenNonExistingDebtPositionTypeOrgWhenGetIONotificationDetailThenNotFoundException() {
    Long debtPositionTypeOrgId = 1L;

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId)).thenReturn(Optional.empty());

    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_CREATED));

    Assertions.assertEquals("DebtPositionTypeOrg having id %d was not found".formatted(debtPositionTypeOrgId), notFoundException.getMessage());
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
  }

  @Test
  void givenFlagNotifyIoFalseWhenGetIONotificationDetailThenNull() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setFlagNotifyIo(false);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));

    IONotificationDTO result = debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_CREATED);

    Assertions.assertNull(result);
  }

  @Test
  void givenContextUpdateWhenGetIONotificationDetailThenNull() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));

    IONotificationDTO result = debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, PaymentEventType.DP_UPDATED);

    Assertions.assertNull(result);
  }

  @Test
  void givenExistingDebtPositionTypeOrgWhenDeleteDebtPositionTypeOrgThenOk(){
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeOrgOperatorsServiceMock.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrgId)).thenReturn(10L);
    Mockito.doNothing().when(debtPositionTypeOrgRepositoryMock).delete(debtPositionTypeOrg);

    debtPositionTypeOrgService.deleteDebtPositionTypeOrg(debtPositionTypeOrgId);

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock,debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenNonExistingDebtPositionTypeOrgWhenDeleteDebtPositionTypeOrgThenNotFoundException(){
    Long debtPositionTypeOrgId = 1L;

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class, () ->debtPositionTypeOrgService.deleteDebtPositionTypeOrg(debtPositionTypeOrgId));

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenOperatorsToHandleAndNonExistingDebtPositionTypeOrgWhenSaveDebtPositionTypeOrgThenOk(){
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = podamFactory.manufacturePojo(SaveDebtPositionTypeOrgDTO.class);
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(true);
    DebtPositionTypeOrg expectedResult = saveDebtPositionTypeOrgDTO.getDebtPositionTypeOrg();
    expectedResult.setDebtPositionTypeOrgId(null);

    Mockito.when(debtPositionTypeOrgRepositoryMock.save(expectedResult))
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
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock,debtPositionTypeOrgOperatorsServiceMock);
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

    Mockito.when(debtPositionTypeOrgRepositoryMock.save(debtPositionTypeOrg))
      .thenReturn(debtPositionTypeOrg);

    DebtPositionTypeOrg result = debtPositionTypeOrgService.saveDebtPositionTypeOrg(
      saveDebtPositionTypeOrgDTO);

    Assertions.assertEquals(debtPositionTypeOrg,result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
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

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeOrgId()))
            .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class,()->debtPositionTypeOrgService.saveDebtPositionTypeOrg(
            saveDebtPositionTypeOrgDTO));

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
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

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeOrgId()))
            .thenReturn(Optional.of(podamFactory.manufacturePojo(DebtPositionTypeOrg.class)));

    Assertions.assertThrows(ValidationException.class,()->debtPositionTypeOrgService.saveDebtPositionTypeOrg(
            saveDebtPositionTypeOrgDTO));

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenExistingDebtPositionTypeOrgAndUnchangedReadOnlyFieldsWhenSaveDebtPositionTypeOrgThenOk(){
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(1L);
    DebtPositionTypeOrg updatedDebtPositionTypeOrg = buildUpdatedDebtPositionTypeOrg(debtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(updatedDebtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setEnabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setDisabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(false);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeOrgId()))
            .thenReturn(Optional.of(debtPositionTypeOrg));

    Mockito.when(debtPositionTypeOrgRepositoryMock.save(updatedDebtPositionTypeOrg))
            .thenReturn(updatedDebtPositionTypeOrg);

    DebtPositionTypeOrg result = debtPositionTypeOrgService.saveDebtPositionTypeOrg(
            saveDebtPositionTypeOrgDTO);

    Assertions.assertEquals(updatedDebtPositionTypeOrg,result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  private static DebtPositionTypeOrg buildUpdatedDebtPositionTypeOrg(DebtPositionTypeOrg debtPositionTypeOrg) {
    DebtPositionTypeOrg dpto = new DebtPositionTypeOrg();
    BeanUtils.copyProperties(debtPositionTypeOrg,dpto);
    return dpto.toBuilder()
              //updatable fields
              .iban(debtPositionTypeOrg.getIban()+1)
              .postalIban(debtPositionTypeOrg.getPostalIban()+1)
              .postalAccountCode(debtPositionTypeOrg.getPostalAccountCode()+1)
              .holderPostalCc(debtPositionTypeOrg.getHolderPostalCc()+1)
              .xsdDefinitionRef(debtPositionTypeOrg.getXsdDefinitionRef()+1)
              .amountCents(debtPositionTypeOrg.getAmountCents()+1)
              .externalPaymentUrl(debtPositionTypeOrg.getExternalPaymentUrl()+1)
              .flagSpontaneous(!debtPositionTypeOrg.isFlagSpontaneous())
              .serviceId(debtPositionTypeOrg.getServiceId()+1)
              .ioTemplateSubject(debtPositionTypeOrg.getIoTemplateSubject()+1)
              .ioTemplateMessage(debtPositionTypeOrg.getIoTemplateMessage()+1)
              .amountActualizationOrgSilServiceId(debtPositionTypeOrg.getAmountActualizationOrgSilServiceId()+1)
              .notifyOutcomePushOrgSilServiceId(debtPositionTypeOrg.getNotifyOutcomePushOrgSilServiceId()+1)
              .flagNotifyIo(!debtPositionTypeOrg.isFlagNotifyIo())
              .build();
  }

  @Test
  void givenValidDebtPositionTypeOrgIdWhenUpdateFlagActiveDebtPositionTypeOrgThenUpdate() {
    //given
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setFlagActive(false);
    Mockito.when(debtPositionTypeOrgRepositoryMock.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true)).thenReturn(1);
    //when
    debtPositionTypeOrgService.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true);
    //then
    Mockito.verify(debtPositionTypeOrgRepositoryMock).updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true);
  }

  @Test
  void givenInvalidDebtPositionTypeOrgIdWhenUpdateFlagActiveDebtPositionTypeOrgThenThrowException() {
    //given
    Long debtPositionTypeOrgId = 1L;
    Mockito.when(debtPositionTypeOrgRepositoryMock.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true)).thenReturn(0);
    //when
    NotFoundException ex = Assertions.assertThrows(NotFoundException.class, () -> debtPositionTypeOrgService.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true));
    //then
    Assertions.assertEquals("DebtPositionTypeOrg having id " + debtPositionTypeOrgId + " not found", ex.getMessage());
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.never()).updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true);
  }

}
