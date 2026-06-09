package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowDebtPositionService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrgBalanceCostRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgBalanceCost;
import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgBalanceCostRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.SpontaneousFormRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import it.gov.pagopa.pu.workflowhub.dto.generated.MassiveDebtPositionIbanUpdateRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgServiceImplTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsServiceMock;
  @Mock
  private SpontaneousFormRepository spontaneousFormRepositoryMock;
  @Mock
  private WorkflowDebtPositionService workflowDebtPositionServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private DebtPositionTypeOrgBalanceCostRepository debtPositionTypeOrgBalanceCostRepositoryMock;

  private DebtPositionTypeOrgService debtPositionTypeOrgService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    debtPositionTypeOrgService = Mockito.spy(new DebtPositionTypeOrgServiceImpl(debtPositionTypeOrgRepositoryMock, debtPositionTypeOrgOperatorsServiceMock, spontaneousFormRepositoryMock, workflowDebtPositionServiceMock, organizationServiceMock, debtPositionTypeOrgBalanceCostRepositoryMock));
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

    Assertions.assertEquals("DEBT_POSITION_TYPE_ORG_NOT_FOUND", notFoundException.getCode());
    Assertions.assertEquals("DebtPositionTypeOrg with id %d not found".formatted(debtPositionTypeOrgId), notFoundException.getMessage());
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
  void givenExistingDebtPositionTypeOrgWhenDeleteDebtPositionTypeOrgThenOk() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeOrgOperatorsServiceMock.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrgId)).thenReturn(10);
    Mockito.doNothing().when(debtPositionTypeOrgBalanceCostRepositoryMock).deleteByDebtPositionTypeOrgId(debtPositionTypeOrgId);
    Mockito.doNothing().when(debtPositionTypeOrgRepositoryMock).delete(debtPositionTypeOrg);

    debtPositionTypeOrgService.deleteDebtPositionTypeOrg(debtPositionTypeOrgId);

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock, debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenNonExistingDebtPositionTypeOrgWhenDeleteDebtPositionTypeOrgThenNotFoundException() {
    Long debtPositionTypeOrgId = 1L;

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class, () -> debtPositionTypeOrgService.deleteDebtPositionTypeOrg(debtPositionTypeOrgId));

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenOperatorsToHandleAndNonExistingDebtPositionTypeOrgWhenSaveDebtPositionTypeOrgThenOk() {
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = podamFactory.manufacturePojo(SaveDebtPositionTypeOrgDTO.class);
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(true);
    DebtPositionTypeOrg expectedResult = saveDebtPositionTypeOrgDTO.getDebtPositionTypeOrg();
    expectedResult.setDebtPositionTypeOrgId(null);
    expectedResult.setSpontaneousFormId(null);
    expectedResult.setIban("IT0000000000000000000000000");
    expectedResult.setPostalIban("IT00X0760100000000000000000");

    String accessToken = "accessToken";

    Mockito.when(debtPositionTypeOrgRepositoryMock.save(expectedResult))
      .thenReturn(expectedResult);
    Mockito.when(debtPositionTypeOrgOperatorsServiceMock.deleteOperatorsByDebtPositionTypeOrgId(
      expectedResult.getDebtPositionTypeOrgId())).thenReturn(1);
    Mockito.when(debtPositionTypeOrgOperatorsServiceMock.saveOperators(
      expectedResult.getDebtPositionTypeOrgId(), saveDebtPositionTypeOrgDTO.getEnabledOperators())).thenReturn(null);
    Mockito.when(debtPositionTypeOrgOperatorsServiceMock.deleteOperators(
      expectedResult.getDebtPositionTypeOrgId(), saveDebtPositionTypeOrgDTO.getDisabledOperators())).thenReturn(2);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrgBalanceCostRequestList(
      List.of(podamFactory.manufacturePojo(DebtPositionTypeOrgBalanceCostRequestDTO.class))
    );
    Mockito.when(debtPositionTypeOrgBalanceCostRepositoryMock.saveAll(Mockito.anyList()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    DebtPositionTypeOrg result = debtPositionTypeOrgService.saveDebtPositionTypeOrg(
      saveDebtPositionTypeOrgDTO, accessToken);

    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(debtPositionTypeOrgBalanceCostRepositoryMock).saveAll(Mockito.anyList());
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock, debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenBalanceCostListWhenSaveDebtPositionTypeOrgThenBalanceCostSaved() {
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(null);
    debtPositionTypeOrg.setSpontaneousFormId(null);
    debtPositionTypeOrg.setIban("IT0000000000000000000000000");
    debtPositionTypeOrg.setPostalIban("IT00X0760100000000000000000");

    String accessToken = "accessToken";

    DebtPositionTypeOrgBalanceCostRequestDTO dptoBalanceCostRequest =
      podamFactory.manufacturePojo(DebtPositionTypeOrgBalanceCostRequestDTO.class);

    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setEnabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setDisabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(false);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrgBalanceCostRequestList(List.of(dptoBalanceCostRequest));

    Long generatedId = 42L;
    DebtPositionTypeOrg savedDpto = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    savedDpto.setDebtPositionTypeOrgId(generatedId);

    Mockito.when(debtPositionTypeOrgRepositoryMock.save(debtPositionTypeOrg)).thenReturn(savedDpto);
    Mockito.when(debtPositionTypeOrgBalanceCostRepositoryMock.saveAll(Mockito.anyList()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken);

    Mockito.verify(debtPositionTypeOrgBalanceCostRepositoryMock).saveAll(Mockito.argThat(list -> {
      DebtPositionTypeOrgBalanceCost saved = list.iterator().next();
      TestUtils.checkNotNullFields(saved, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
      TestUtils.checkNotNullFields(saved.getId());
      Assertions.assertEquals(generatedId, saved.getId().getDebtPositionTypeOrgId());
      return true;
    }));
  }

  @Test
  void givenNoOperatorsToHandleAndNonExistingDebtPositionTypeOrgWhenSaveDebtPositionTypeOrgThenOk() {
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(null);
    debtPositionTypeOrg.setSpontaneousFormId(null);
    debtPositionTypeOrg.setIban("IT0000000000000000000000000");
    debtPositionTypeOrg.setPostalIban("IT00X0760100000000000000000");
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setEnabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setDisabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(false);

    String accessToken = "accessToken";

    Mockito.when(debtPositionTypeOrgRepositoryMock.save(debtPositionTypeOrg))
      .thenReturn(debtPositionTypeOrg);

    DebtPositionTypeOrg result = debtPositionTypeOrgService.saveDebtPositionTypeOrg(
      saveDebtPositionTypeOrgDTO, accessToken);

    Assertions.assertEquals(debtPositionTypeOrg, result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenNonExistingDebtPositionTypeOrgAndDebtPositionTypeOrgIdPopulatedWhenSaveDebtPositionTypeOrgThenNotFoundException() {
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(1L);
    debtPositionTypeOrg.setIban("IT0000000000000000000000000");
    debtPositionTypeOrg.setPostalIban("IT00X0760100000000000000000");
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setEnabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setDisabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(false);

    String accessToken = "accessToken";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeOrgId()))
      .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class, () -> debtPositionTypeOrgService.saveDebtPositionTypeOrg(
      saveDebtPositionTypeOrgDTO, accessToken));

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenExistingDebtPositionTypeOrgAndUpdatedReadOnlyFieldWhenSaveDebtPositionTypeOrgThenInvalidValueException() {
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(1L);
    debtPositionTypeOrg.setIban("IT0000000000000000000000000");
    debtPositionTypeOrg.setPostalIban("IT00X0760100000000000000000");
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setEnabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setDisabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(false);

    String accessToken = "accessToken";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeOrgId()))
      .thenReturn(Optional.of(podamFactory.manufacturePojo(DebtPositionTypeOrg.class)));

    Assertions.assertThrows(InvalidValueException.class, () -> debtPositionTypeOrgService.saveDebtPositionTypeOrg(
      saveDebtPositionTypeOrgDTO, accessToken));

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenExistingDebtPositionTypeOrgAndUnchangedReadOnlyFieldsWhenSaveDebtPositionTypeOrgThenOk() {
    Long orgId = 1L;

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(orgId);
    organization.setStatus(OrganizationStatus.ACTIVE);

    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(1L);
    debtPositionTypeOrg.setSpontaneousFormId(null);
    debtPositionTypeOrg.setIban("IT0000000000000000000000000");
    debtPositionTypeOrg.setPostalIban("IT00X0760100000000000000000");
    debtPositionTypeOrg.setOrganizationId(orgId);

    DebtPositionTypeOrg updatedDebtPositionTypeOrg = buildUpdatedDebtPositionTypeOrg(debtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(updatedDebtPositionTypeOrg);
    saveDebtPositionTypeOrgDTO.setEnabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setDisabledOperators(Collections.emptySet());
    saveDebtPositionTypeOrgDTO.setRemoveEnabledOperators(false);

    String accessToken = "accessToken";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeOrgId()))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    Mockito.when(debtPositionTypeOrgRepositoryMock.save(updatedDebtPositionTypeOrg))
      .thenReturn(updatedDebtPositionTypeOrg);

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken)).thenReturn(Optional.of(organization));

    DebtPositionTypeOrg result = debtPositionTypeOrgService.saveDebtPositionTypeOrg(
      saveDebtPositionTypeOrgDTO, accessToken);

    Assertions.assertEquals(updatedDebtPositionTypeOrg, result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  private static DebtPositionTypeOrg buildUpdatedDebtPositionTypeOrg(DebtPositionTypeOrg debtPositionTypeOrg) {
    DebtPositionTypeOrg dpto = new DebtPositionTypeOrg();
    BeanUtils.copyProperties(debtPositionTypeOrg, dpto);
    return dpto.toBuilder()
      //updatable fields
      .iban(debtPositionTypeOrg.getIban() + 1)
      .postalIban(debtPositionTypeOrg.getPostalIban() + 1)
      .postalAccountCode(debtPositionTypeOrg.getPostalAccountCode() + 1)
      .holderPostalCc(debtPositionTypeOrg.getHolderPostalCc() + 1)
      .amountCents(debtPositionTypeOrg.getAmountCents() + 1)
      .externalPaymentUrl(debtPositionTypeOrg.getExternalPaymentUrl() + 1)
      .flagSpontaneous(!debtPositionTypeOrg.isFlagSpontaneous())
      .serviceId(debtPositionTypeOrg.getServiceId() + 1)
      .ioTemplateSubject(debtPositionTypeOrg.getIoTemplateSubject() + 1)
      .ioTemplateMessage(debtPositionTypeOrg.getIoTemplateMessage() + 1)
      .amountActualizationOrgSilServiceId(debtPositionTypeOrg.getAmountActualizationOrgSilServiceId() + 1)
      .notifyOutcomePushOrgSilServiceId(debtPositionTypeOrg.getNotifyOutcomePushOrgSilServiceId() + 1)
      .flagNotifyIo(!debtPositionTypeOrg.isFlagNotifyIo())
      .build();
  }

  @Test
  void givenValidDebtPositionTypeOrgIdWhenUpdateFlagActiveDebtPositionTypeOrgThenUpdate() {
    //given
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeId(10L);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeOrgRepositoryMock.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true))
      .thenReturn(1);
    //when
    debtPositionTypeOrgService.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true);
    //then
    Mockito.verify(debtPositionTypeOrgRepositoryMock).findById(debtPositionTypeOrgId);
    Mockito.verify(debtPositionTypeOrgRepositoryMock).updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true);
  }

  @Test
  void givenInvalidDebtPositionTypeOrgIdWhenUpdateFlagActiveDebtPositionTypeOrgThenThrowException() {
    //given
    Long debtPositionTypeOrgId = 1L;

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.empty());
    //when
    NotFoundException ex = Assertions.assertThrows(NotFoundException.class, () -> debtPositionTypeOrgService.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true));
    //then
    Assertions.assertEquals("DEBT_POSITION_TYPE_ORG_NOT_FOUND",ex.getCode());
    Assertions.assertEquals("DebtPositionTypeOrg with id %d not found".formatted(debtPositionTypeOrgId), ex.getMessage());

    Mockito.verify(debtPositionTypeOrgRepositoryMock).findById(debtPositionTypeOrgId);
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.never()).updateFlagActiveDebtPositionTypeOrg(Mockito.anyLong(), Mockito.anyBoolean());
  }

  @Test
  void givenTechnicalDebtPositionTypeWhenUpdateFlagActiveTrueThenInvalidValueException() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeId(-1L);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    InvalidValueException ex = Assertions.assertThrows(
      InvalidValueException.class, () -> debtPositionTypeOrgService.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, true));

    Assertions.assertEquals("INVALID_FLAG_ACTIVE",ex.getCode());
    Assertions.assertEquals("Technical debtPositionTypeOrg cannot be enabled", ex.getMessage());

    Mockito.verify(debtPositionTypeOrgRepositoryMock).findById(debtPositionTypeOrgId);
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.never()).updateFlagActiveDebtPositionTypeOrg(Mockito.anyLong(), Mockito.anyBoolean());
  }

  @Test
  void givenTechnicalDebtPositionTypeWhenUpdateFlagActiveFalseThenOk() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeId(-1L);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(debtPositionTypeOrgRepositoryMock.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, false))
      .thenReturn(1);

    debtPositionTypeOrgService.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, false);

    Mockito.verify(debtPositionTypeOrgRepositoryMock).findById(debtPositionTypeOrgId);
    Mockito.verify(debtPositionTypeOrgRepositoryMock).updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, false);
  }

  @Test
  void givenTechnicalDebtPositionTypeAndFlagActiveTrueWhenSaveDebtPositionTypeOrgThenInvalidValueException() {
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(null);
    debtPositionTypeOrg.setDebtPositionTypeId(-1L);
    debtPositionTypeOrg.setFlagActive(true);
    debtPositionTypeOrg.setIban(null);
    debtPositionTypeOrg.setPostalIban(null);
    debtPositionTypeOrg.setSpontaneousFormId(null);

    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);

    String accessToken = "accessToken";

    InvalidValueException ex = Assertions.assertThrows(
      InvalidValueException.class, () -> debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken));

    Assertions.assertEquals("INVALID_FLAG_ACTIVE",ex.getCode());
    Assertions.assertEquals("Technical debtPositionTypeOrg cannot be enabled", ex.getMessage());

    Mockito.verifyNoInteractions(debtPositionTypeOrgRepositoryMock, debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenNullDebtPositionTypeOrgWhenSaveDebtPositionTypeOrgThenInvalidValueException() {
    // given
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(null);

    String accessToken = "accessToken";

    // then
    InvalidValueException ex = Assertions.assertThrows(InvalidValueException.class,
      () -> debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken));

    Assertions.assertEquals("MISSING_DEBT_POSITION_TYPE_ORG",ex.getCode());
    Assertions.assertEquals("DebtPositionTypeOrg must not be null", ex.getMessage());
    Mockito.verifyNoInteractions(debtPositionTypeOrgRepositoryMock, debtPositionTypeOrgOperatorsServiceMock);
  }


  @Test
  void givenMismatchedOrganizationIdWhenSaveDebtPositionTypeOrgThenInvalidValueException() {
    // given
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(null);
    debtPositionTypeOrg.setOrganizationId(1L);
    debtPositionTypeOrg.setSpontaneousFormId(100L);
    debtPositionTypeOrg.setIban("IT0000000000000000000000000");
    debtPositionTypeOrg.setPostalIban(null);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);

    String accessToken = "accessToken";

    var spontaneousForm = podamFactory.manufacturePojo(it.gov.pagopa.pu.debtpositions.model.SpontaneousForm.class);
    spontaneousForm.setOrganizationId(999L);

    Mockito.when(spontaneousFormRepositoryMock.findById(debtPositionTypeOrg.getSpontaneousFormId()))
      .thenReturn(Optional.of(spontaneousForm));

    //then
    InvalidValueException ex = Assertions.assertThrows(InvalidValueException.class,
      () -> debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken));

    Assertions.assertEquals("INVALID_SPONTANEOUS_FORM", ex.getCode());
    Assertions.assertEquals(
      "SpontaneousFormId %d is not tied to the organizationId %d"
        .formatted(debtPositionTypeOrg.getSpontaneousFormId(), debtPositionTypeOrg.getOrganizationId()),
      ex.getMessage()
    );

    Mockito.verify(spontaneousFormRepositoryMock).findById(debtPositionTypeOrg.getSpontaneousFormId());
    Mockito.verifyNoMoreInteractions(spontaneousFormRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgRepositoryMock, debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenInvalidIbanWhenSaveDebtPositionTypeOrgThenInvalidValueException() {
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setIban("invalidIban");
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);

    String accessToken = "accessToken";

    InvalidValueException ex = Assertions.assertThrows(InvalidValueException.class,
      () -> debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken));

    Assertions.assertEquals("INVALID_IBAN",ex.getCode());
    Assertions.assertEquals("Provided iban is not valid", ex.getMessage());
  }

  @ParameterizedTest()
  @MethodSource("provideInvalidPostalIbanParams")
  void givenInvalidPostalIbanWhenSaveDebtPositionTypeOrgThenInvalidValueException(
    String iban,
    String postalIban,
    String expectedMessage
  ) {
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setIban(iban);
    debtPositionTypeOrg.setPostalIban(postalIban);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);

    String accessToken = "accessToken";

    InvalidValueException ex = Assertions.assertThrows(InvalidValueException.class,
      () -> debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken));

    Assertions.assertEquals("INVALID_POSTAL_IBAN", ex.getCode());
    Assertions.assertEquals(expectedMessage, ex.getMessage());
  }

  private static Stream<Arguments> provideInvalidPostalIbanParams() {
    return Stream.of(
      Arguments.of(
        null,
        "IT00X0760100000000000000000",
        "It is not possible to set postalIban if the iban is null"
      ),
      Arguments.of(
        "IT0000000000000000000000000",
        "IT00X0760100000000000",
        "Provided postal iban is not valid"
      ),
      Arguments.of(
        "IT0000000000000000000000000",
        "invalidIban",
        "Provided postal iban is not valid"
      )
    );
  }

  @Test
  void givenSpontaneousFormIdButFormNotFoundWhenSaveDebtPositionTypeOrgThenInvalidValueException() {
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(null);
    debtPositionTypeOrg.setSpontaneousFormId(123L);
    debtPositionTypeOrg.setIban(null);
    debtPositionTypeOrg.setPostalIban(null);
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);

    String accessToken = "accessToken";

    Mockito.when(spontaneousFormRepositoryMock.findById(123L)).thenReturn(Optional.empty());

    InvalidValueException ex = Assertions.assertThrows(InvalidValueException.class,
      () -> debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken));

    Assertions.assertEquals("INVALID_SPONTANEOUS_FORM",ex.getCode());
    Assertions.assertEquals("SpontaneousFormId 123 not found", ex.getMessage());

    Mockito.verify(spontaneousFormRepositoryMock).findById(123L);
    Mockito.verifyNoMoreInteractions(spontaneousFormRepositoryMock);
    Mockito.verifyNoInteractions(debtPositionTypeOrgRepositoryMock, debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenMatchingOrganizationIdWhenSaveDebtPositionTypeOrgThenOk() {
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setDebtPositionTypeOrgId(null);
    debtPositionTypeOrg.setSpontaneousFormId(123L);
    debtPositionTypeOrg.setIban("IT0000000000000000000000000");
    debtPositionTypeOrg.setPostalIban("IT00X0760100000000000000000");
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);

    String accessToken = "accessToken";

    SpontaneousForm spontaneousForm = new SpontaneousForm();
    spontaneousForm.setOrganizationId(debtPositionTypeOrg.getOrganizationId());

    Mockito.when(spontaneousFormRepositoryMock.findById(123L)).thenReturn(Optional.of(spontaneousForm));
    Mockito.when(debtPositionTypeOrgRepositoryMock.save(debtPositionTypeOrg)).thenReturn(debtPositionTypeOrg);

    DebtPositionTypeOrg result = debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken);

    Assertions.assertEquals(debtPositionTypeOrg, result);
    Mockito.verify(spontaneousFormRepositoryMock).findById(123L);
    Mockito.verify(debtPositionTypeOrgRepositoryMock).save(debtPositionTypeOrg);
  }

  @Test
  void givenDebtPositionTypeOrgWithEmptyPostalIbanWhenSaveDebtPositionTypeOrgThenThrowInvalidValueException() {
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setDebtPositionTypeId(1L);
    debtPositionTypeOrg.setFlagActive(true);
    debtPositionTypeOrg.setPostalIban("");

    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(debtPositionTypeOrg);

    String accessToken = "accessToken";

    Assertions.assertThrows(InvalidValueException.class,
      () -> debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken)
    );
  }

  @Test
  void givenOldIbanNullAndNewIbanNotNullWhenSaveThenTriggerMassiveIbanUpdate() {
    Long orgId = 1L;
    String orgIban = "IT0000000000000000000000000";
    String newIban = "IT0000000000000000000000001";
    String accessToken = "accessToken";

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(orgId);
    organization.setStatus(OrganizationStatus.ACTIVE);
    organization.setIban(orgIban);

    DebtPositionTypeOrg existingDpto = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    existingDpto.setDebtPositionTypeOrgId(1L);
    existingDpto.setOrganizationId(orgId);
    existingDpto.setIban(null);
    existingDpto.setSpontaneousFormId(null);

    DebtPositionTypeOrg updatedDpto = buildUpdatedDebtPositionTypeOrg(existingDpto);
    updatedDpto.setIban(newIban);
    updatedDpto.setPostalIban(null);

    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(updatedDpto);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(updatedDpto.getDebtPositionTypeOrgId()))
      .thenReturn(Optional.of(existingDpto));
    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.save(updatedDpto))
      .thenReturn(updatedDpto);

    debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken);

    MassiveDebtPositionIbanUpdateRequestDTO expectedRequest = MassiveDebtPositionIbanUpdateRequestDTO.builder()
      .oldIban(orgIban)
      .newIban(newIban)
      .oldPostalIban(existingDpto.getPostalIban())
      .newPostalIban(updatedDpto.getPostalIban())
      .debtPositionTypeOrgId(updatedDpto.getDebtPositionTypeOrgId())
      .build();

    Mockito.verify(workflowDebtPositionServiceMock).massiveDpIbanUpdate(orgId, expectedRequest, accessToken);
  }

  @Test
  void givenUnchangedIbanAndChangedPostalIbanWhenSaveThenTriggerMassiveIbanUpdate() {
    Long orgId = 1L;
    String iban = "IT0000000000000000000000001";
    String oldPostalIban = null;
    String newPostalIban = "IT00X0760100000000000000000";
    String accessToken = "accessToken";

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrganizationId(orgId);
    organization.setStatus(OrganizationStatus.ACTIVE);

    DebtPositionTypeOrg existingDpto = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    existingDpto.setDebtPositionTypeOrgId(1L);
    existingDpto.setOrganizationId(orgId);
    existingDpto.setIban(iban);
    existingDpto.setPostalIban(oldPostalIban);
    existingDpto.setSpontaneousFormId(null);

    DebtPositionTypeOrg updatedDpto = buildUpdatedDebtPositionTypeOrg(existingDpto);
    updatedDpto.setIban(iban);
    updatedDpto.setPostalIban(newPostalIban);

    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO = new SaveDebtPositionTypeOrgDTO();
    saveDebtPositionTypeOrgDTO.setDebtPositionTypeOrg(updatedDpto);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(updatedDpto.getDebtPositionTypeOrgId()))
      .thenReturn(Optional.of(existingDpto));
    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(debtPositionTypeOrgRepositoryMock.save(updatedDpto))
      .thenReturn(updatedDpto);

    debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO, accessToken);

    MassiveDebtPositionIbanUpdateRequestDTO expectedRequest = MassiveDebtPositionIbanUpdateRequestDTO.builder()
      .oldIban(iban)
      .newIban(iban)
      .oldPostalIban(oldPostalIban)
      .newPostalIban(newPostalIban)
      .debtPositionTypeOrgId(updatedDpto.getDebtPositionTypeOrgId())
      .build();

    Mockito.verify(workflowDebtPositionServiceMock).massiveDpIbanUpdate(orgId, expectedRequest, accessToken);
  }
}
