package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeOrgOperatorsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgOperatorsControllerTest {
  @Mock
  private DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsServiceMock;

  private DebtPositionTypeOrgOperatorsController controller;

  @BeforeEach
  void setUp() {
    controller = new DebtPositionTypeOrgOperatorsController(debtPositionTypeOrgOperatorsServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgOperatorsServiceMock);
  }

  @Test
  void givenDebtPositionTypeOrgIdAndSetOfExternalOperatorUserIdsWhenDeleteOperatorsThenReturnDeletedCount() {
    Long debtPositionTypeOrgId = 123L;
    Set<String> operatorIds = Set.of("op1", "op2");
    int expectedDeletedCount = 2;

    when(debtPositionTypeOrgOperatorsServiceMock.deleteOperators(debtPositionTypeOrgId, operatorIds))
      .thenReturn(expectedDeletedCount);

    ResponseEntity<Integer> result = controller.deleteOperators(debtPositionTypeOrgId, operatorIds);

    assertNotNull(result);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(expectedDeletedCount, result.getBody());
  }
}
