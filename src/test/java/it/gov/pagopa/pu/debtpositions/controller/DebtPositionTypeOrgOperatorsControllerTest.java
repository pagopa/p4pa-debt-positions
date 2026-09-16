package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.dto.generated.RelateUserToDefaultDPTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeOrgOperatorsFacadeService;
import it.gov.pagopa.pu.debtpositions.service.DefaultDpTypeOrgOperatorFacadeService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgOperatorsControllerTest {
  @Mock
  private DebtPositionTypeOrgOperatorsFacadeService debtPositionTypeOrgOperatorsFacadeServiceMock;
  @Mock
  private DefaultDpTypeOrgOperatorFacadeService defaultDpTypeOrgOperatorFacadeServiceMock;

  private DebtPositionTypeOrgOperatorsController controller;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void setUp() {
    controller = new DebtPositionTypeOrgOperatorsController(
      debtPositionTypeOrgOperatorsFacadeServiceMock,
      defaultDpTypeOrgOperatorFacadeServiceMock
    );
    SecurityUtilsTest.configureSecurityContext(accessToken, "userId");
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionTypeOrgOperatorsFacadeServiceMock,
      defaultDpTypeOrgOperatorFacadeServiceMock
    );
  }

  @Test
  void givenDebtPositionTypeOrgIdAndSetOfExternalOperatorUserIdsWhenDeleteOperatorsThenReturnDeletedCount() {
    Long debtPositionTypeOrgId = 123L;
    Set<String> operatorIds = Set.of("op1", "op2");
    int expectedDeletedCount = 2;

    when(debtPositionTypeOrgOperatorsFacadeServiceMock.deleteOperators(debtPositionTypeOrgId, operatorIds))
      .thenReturn(expectedDeletedCount);

    ResponseEntity<Integer> result = controller.deleteOperators(debtPositionTypeOrgId, operatorIds);

    assertNotNull(result);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(expectedDeletedCount, result.getBody());
  }

  @Test
  void givenOperatorExternalUserIdAndSetOfDebtPositionTypeOrgIdsWhenSaveDebtPositionTypeOrgOperatorsForOperatorThenReturnCreated() {
    String operatorExternalUserId = "operator1";
    Set<Long> debtPositionTypeOrgIds = Set.of(10L, 20L);

    when(debtPositionTypeOrgOperatorsFacadeServiceMock
      .saveDebtPositionTypeOrgOperatorsForOperator(operatorExternalUserId, debtPositionTypeOrgIds))
      .thenReturn(Collections.emptyList());

    ResponseEntity<Void> result = controller.saveDebtPositionTypeOrgOperatorsForOperator(operatorExternalUserId, debtPositionTypeOrgIds);

    assertNotNull(result);
    assertEquals(HttpStatus.CREATED, result.getStatusCode());
    assertNull(result.getBody());
  }

  @Test
  void givenNonEmptyListOfDebtPositionTypeOrgOperatorWhenRelateUserToDefaultDPTypeOrgThenReturnCreated() {
    //GIVEN
    String operatorExternalUserId = "operator1";
    long organizationId = 1L;
    RelateUserToDefaultDPTypeOrgDTO relateUserToDefaultDPTypeOrgDTO = new RelateUserToDefaultDPTypeOrgDTO();
    relateUserToDefaultDPTypeOrgDTO.setOperatorExternalUserId(operatorExternalUserId);
    relateUserToDefaultDPTypeOrgDTO.setOrganizationId(organizationId);
    when(defaultDpTypeOrgOperatorFacadeServiceMock
      .relateUserToDefaultDPTypeOrg(relateUserToDefaultDPTypeOrgDTO,accessToken))
      .thenReturn(List.of(new DebtPositionTypeOrgOperators()));
    //WHEN
    ResponseEntity<Void> result = controller.relateUserToDefaultDPTypeOrg(relateUserToDefaultDPTypeOrgDTO);
    //THEN
    assertNotNull(result);
    assertEquals(HttpStatus.CREATED, result.getStatusCode());
    assertNull(result.getBody());
  }

  @Test
  void givenEmptyListOfDebtPositionTypeOrgOperatorWhenRelateUserToDefaultDPTypeOrgThenReturnOk() {
    //GIVEN
    String operatorExternalUserId = "operator1";
    long organizationId = 1L;
    RelateUserToDefaultDPTypeOrgDTO relateUserToDefaultDPTypeOrgDTO = new RelateUserToDefaultDPTypeOrgDTO();
    relateUserToDefaultDPTypeOrgDTO.setOperatorExternalUserId(operatorExternalUserId);
    relateUserToDefaultDPTypeOrgDTO.setOrganizationId(organizationId);
    when(defaultDpTypeOrgOperatorFacadeServiceMock
      .relateUserToDefaultDPTypeOrg(relateUserToDefaultDPTypeOrgDTO,accessToken))
      .thenReturn(Collections.emptyList());
    //WHEN
    ResponseEntity<Void> result = controller.relateUserToDefaultDPTypeOrg(relateUserToDefaultDPTypeOrgDTO);
    //THEN
    assertNotNull(result);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNull(result.getBody());
  }

}
