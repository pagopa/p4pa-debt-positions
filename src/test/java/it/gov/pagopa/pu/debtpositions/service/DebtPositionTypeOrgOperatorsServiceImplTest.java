package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.gov.pagopa.pu.debtpositions.service.dptypeorg.UnknownDebtPositionTypeOrgRetrieverService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgOperatorsServiceImplTest {

  @Mock
  private DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepositoryMock;
  @Mock
  private UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverServiceMock;

  private DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService;

  @BeforeEach
  void setUp() {
    debtPositionTypeOrgOperatorsService = Mockito.spy(
      new DebtPositionTypeOrgOperatorsServiceImpl(
        debtPositionTypeOrgOperatorsRepositoryMock,
        unknownDebtPositionTypeOrgRetrieverServiceMock
      )
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionTypeOrgOperatorsRepositoryMock,
      unknownDebtPositionTypeOrgRetrieverServiceMock
    );
  }

  @Test
  void whenDeleteOperatorsByDebtPositionTypeOrgIdThenOk() {
    Long debtPositionTypeOrgId = 1L;
    Integer expectedResult = 2;

    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.deleteByDebtPositionTypeOrgId(debtPositionTypeOrgId)).thenReturn(
      expectedResult);

    Integer result = debtPositionTypeOrgOperatorsService.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrgId);

    Assertions.assertEquals(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorsRepositoryMock);
  }

  @Test
  void givenNoExistingDebtPositionTypeOrgOperatorsWhenSaveOperatorsThenOk() {
    Long debtPositionTypeOrgId = 1L;
    Set<String> operatorsSet = new HashSet<>();
    operatorsSet.add("operator1");
    operatorsSet.add("operator2");
    operatorsSet.add("operator3");
    List<DebtPositionTypeOrgOperators> expectedResult = new ArrayList<>();
    long i = 1;
    for (String operator : operatorsSet) {
      expectedResult.add(buildDebtPositionTypeOrgOperator(operator, debtPositionTypeOrgId,i));
      i++;
    }

    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.findByDebtPositionTypeOrgId(debtPositionTypeOrgId)).thenReturn(
      Collections.emptyList());
    ArgumentCaptor<List<DebtPositionTypeOrgOperators>> saveCaptor = ArgumentCaptor.forClass(
      List.class) ;
    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.saveAll(
      saveCaptor.capture())).thenReturn(expectedResult);

    List<DebtPositionTypeOrgOperators> result = debtPositionTypeOrgOperatorsService.saveOperators(debtPositionTypeOrgId,operatorsSet);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedResult,result);
    List<DebtPositionTypeOrgOperators> operators = saveCaptor.getValue();
    Assertions.assertEquals(operatorsSet.size(),operators.size());
    for (DebtPositionTypeOrgOperators operator : operators) {
      Assertions.assertEquals(debtPositionTypeOrgId,operator.getDebtPositionTypeOrgId());
      Assertions.assertTrue(operatorsSet.contains(operator.getOperatorExternalUserId()));
      operatorsSet.remove(operator.getOperatorExternalUserId());
    }
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorsRepositoryMock);
  }

  @Test
  void givenExistingDebtPositionTypeOrgOperatorsWhenSaveOperatorsThenEmptyList() {
    Long debtPositionTypeOrgId = 1L;
    Set<String> operatorsSet = new HashSet<>();
    operatorsSet.add("operator1");
    DebtPositionTypeOrgOperators operator = new DebtPositionTypeOrgOperators();
    operator.setDebtPositionTypeOrgId(debtPositionTypeOrgId);
    operator.setOperatorExternalUserId("operator1");

    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.findByDebtPositionTypeOrgId(debtPositionTypeOrgId)).thenReturn(
      Collections.singletonList(operator));

    List<DebtPositionTypeOrgOperators> result = debtPositionTypeOrgOperatorsService.saveOperators(debtPositionTypeOrgId,operatorsSet);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorsRepositoryMock);
  }

  private DebtPositionTypeOrgOperators buildDebtPositionTypeOrgOperator(
    String operator, Long debtPositionTypeOrgId, long bias) {
    DebtPositionTypeOrgOperators debtPositionTypeOrgOperators = new DebtPositionTypeOrgOperators();
    debtPositionTypeOrgOperators.setDebtPositionTypeOrgOperatorId(bias);
    debtPositionTypeOrgOperators.setDebtPositionTypeOrgId(debtPositionTypeOrgId);
    debtPositionTypeOrgOperators.setOperatorExternalUserId(operator);
    return debtPositionTypeOrgOperators;
  }

  @Test
  void whenDeleteOperatorsThenOk() {
    Long debtPositionTypeOrgId = 1L;
    Set<String> operatorsSet = new HashSet<>();
    operatorsSet.add("operator1");
    operatorsSet.add("operator2");
    operatorsSet.add("operator3");
    int expectedResult = 3;

    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.deleteByDebtPositionTypeOrgIdAndOperatorExternalUserId(debtPositionTypeOrgId,operatorsSet)).thenReturn(
      expectedResult);

    int result = debtPositionTypeOrgOperatorsService.deleteOperators(debtPositionTypeOrgId,operatorsSet);

    Assertions.assertEquals(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorsRepositoryMock);
  }

  @Test
  void givenNoExistingDebtPositionTypeOrgOperatorsWhenSaveDebtPositionTypeOrgOperatorsForOperatorThenOk() {
    String operatorExternalUserId = "operator1";
    Set<Long> orgIdsSet = new HashSet<>();
    orgIdsSet.add(10L);
    orgIdsSet.add(20L);
    orgIdsSet.add(30L);

    List<DebtPositionTypeOrgOperators> expectedResult = new ArrayList<>();
    long i = 1;
    for (Long orgId : orgIdsSet) {
      expectedResult.add(buildDebtPositionTypeOrgOperator(operatorExternalUserId, orgId, i));
      i++;
    }

    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.findByOperatorExternalUserId(operatorExternalUserId))
      .thenReturn(Collections.emptyList());

    ArgumentCaptor<List<DebtPositionTypeOrgOperators>> saveCaptor = ArgumentCaptor.forClass(List.class);
    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.saveAll(saveCaptor.capture()))
      .thenReturn(expectedResult);

    List<DebtPositionTypeOrgOperators> result =
      debtPositionTypeOrgOperatorsService.saveDebtPositionTypeOrgOperatorsForOperator(operatorExternalUserId, orgIdsSet);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedResult, result);

    List<DebtPositionTypeOrgOperators> savedEntities = saveCaptor.getValue();
    Assertions.assertEquals(orgIdsSet.size(), savedEntities.size());
    for (DebtPositionTypeOrgOperators entity : savedEntities) {
      Assertions.assertEquals(operatorExternalUserId, entity.getOperatorExternalUserId());
      Assertions.assertTrue(orgIdsSet.contains(entity.getDebtPositionTypeOrgId()));
      orgIdsSet.remove(entity.getDebtPositionTypeOrgId());
    }

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorsRepositoryMock);
  }

  @Test
  void givenExistingDebtPositionTypeOrgOperatorsWhenSaveDebtPositionTypeOrgOperatorsForOperatorThenEmptyList() {
    String operatorExternalUserId = "operator1";
    Set<Long> orgIdsSet = new HashSet<>();
    orgIdsSet.add(10L);

    DebtPositionTypeOrgOperators existing = new DebtPositionTypeOrgOperators();
    existing.setOperatorExternalUserId(operatorExternalUserId);
    existing.setDebtPositionTypeOrgId(10L);

    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.findByOperatorExternalUserId(operatorExternalUserId))
      .thenReturn(Collections.singletonList(existing));

    List<DebtPositionTypeOrgOperators> result =
      debtPositionTypeOrgOperatorsService.saveDebtPositionTypeOrgOperatorsForOperator(operatorExternalUserId, orgIdsSet);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());

    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorsRepositoryMock);
  }

  @Test
  void givenWhenSaveDefaultTechnicalDebtPositionTypeOrgOperatorsForOperatorThen() {
    //GIVEN
    String expectedOperatorExternalUserId = "operatorExternalUserId";
    DebtPositionTypeOrg unknownDebtPositionTypeOrg = DebtPositionTypeOrg.builder()
      .debtPositionTypeOrgId(1L)
      .debtPositionTypeId(-1L)
      .build();
    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(1L))
      .thenReturn(unknownDebtPositionTypeOrg);

    DebtPositionTypeOrgOperators expectedDebtPositionTypeOrgOperator = new DebtPositionTypeOrgOperators();
    expectedDebtPositionTypeOrgOperator.setDebtPositionTypeOrgId(1L);
    expectedDebtPositionTypeOrgOperator.setOperatorExternalUserId(expectedOperatorExternalUserId);
    ArgumentCaptor<List<DebtPositionTypeOrgOperators>> saveCaptor = ArgumentCaptor.forClass(List.class);
    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.saveAll(
      saveCaptor.capture()
    )).thenReturn(List.of(expectedDebtPositionTypeOrgOperator));

    //WHEN
    List<DebtPositionTypeOrgOperators> actualDebtPositionTypeOrgOperators = debtPositionTypeOrgOperatorsService.saveDefaultTechnicalDebtPositionTypeOrgOperatorsForOperator(
      expectedOperatorExternalUserId,
      1L
    );

    //THEN
    //Assert DebtPositionTypeOrgOperators List returned by method debtPositionTypeOrgOperatorsRepository.saveAll
    Assertions.assertNotNull(actualDebtPositionTypeOrgOperators);
    Assertions.assertEquals(1, actualDebtPositionTypeOrgOperators.size());
    Assertions.assertEquals(unknownDebtPositionTypeOrg.getDebtPositionTypeOrgId(), actualDebtPositionTypeOrgOperators.getFirst().getDebtPositionTypeOrgId());
    Assertions.assertEquals(expectedOperatorExternalUserId, actualDebtPositionTypeOrgOperators.getFirst().getOperatorExternalUserId());
    //Assert DebtPositionTypeOrgOperators List parameter passed to method debtPositionTypeOrgOperatorsRepository.saveAll
    List<DebtPositionTypeOrgOperators> capturedDebtPositionTypeOrgoperatorList = saveCaptor.getValue();
    Assertions.assertEquals(1, capturedDebtPositionTypeOrgoperatorList.size());
    Assertions.assertEquals(unknownDebtPositionTypeOrg.getDebtPositionTypeOrgId(), capturedDebtPositionTypeOrgoperatorList.getFirst().getDebtPositionTypeOrgId());
    Assertions.assertEquals(expectedOperatorExternalUserId, capturedDebtPositionTypeOrgoperatorList.getFirst().getOperatorExternalUserId());

  }
}
