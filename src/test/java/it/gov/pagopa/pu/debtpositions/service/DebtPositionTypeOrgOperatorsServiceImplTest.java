package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

  private DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService;

  @BeforeEach
  void setUp() {
    debtPositionTypeOrgOperatorsService = Mockito.spy(new DebtPositionTypeOrgOperatorsServiceImpl(debtPositionTypeOrgOperatorsRepositoryMock));
  }

  @Test
  void whenDeleteOperatorsByDebtPositionTypeOrgIdThenOk() {
    Long debtPositionTypeOrgId = 1L;
    long expectedResult = 2L;

    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.deleteByDebtPositionTypeOrgId(debtPositionTypeOrgId)).thenReturn(
      expectedResult);

    long result = debtPositionTypeOrgOperatorsService.deleteOperatorsByDebtPositionTypeOrgId(debtPositionTypeOrgId);

    Assertions.assertEquals(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorsRepositoryMock);
  }

  @Test
  void whenSaveOperatorsThenOk() {
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

    ArgumentCaptor<List<DebtPositionTypeOrgOperators>> captor = ArgumentCaptor.forClass(
      List.class) ;
    Mockito.when(debtPositionTypeOrgOperatorsRepositoryMock.saveAll(
      captor.capture())).thenReturn(expectedResult);

    List<DebtPositionTypeOrgOperators> result = debtPositionTypeOrgOperatorsService.saveOperators(debtPositionTypeOrgId,operatorsSet);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedResult,result);
    List<DebtPositionTypeOrgOperators> operators = captor.getValue();
    Assertions.assertEquals(operatorsSet.size(),operators.size());
    for (DebtPositionTypeOrgOperators operator : operators) {
      Assertions.assertEquals(debtPositionTypeOrgId,operator.getDebtPositionTypeOrgId());
      Assertions.assertTrue(operatorsSet.contains(operator.getOperatorExternalUserId()));
      operatorsSet.remove(operator.getOperatorExternalUserId());
    }
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
}
