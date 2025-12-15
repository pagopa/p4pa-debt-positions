package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.exception.custom.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgOperatorsFaker.buildDebtPositionTypeOrgOperators;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizeOperatorOnDebtPositionTypeServiceTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepositoryMock;

  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;

  @BeforeEach
  void init() {
    authorizeOperatorOnDebtPositionTypeService = new AuthorizeOperatorOnDebtPositionTypeServiceImpl(
      debtPositionTypeOrgRepositoryMock, debtPositionTypeOrgOperatorsRepositoryMock);
  }

  @Test
  void givenOperatorAuthorizedWhenAuthorizeThenSuccess() {
    String operatorExternalUserId = "operatorExternalUserId";
    String orgIpaCode = "orgIpaCode";
    Long debtPositionTypeOrgId = 2L;

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    when(debtPositionTypeOrgOperatorsRepositoryMock.findByDebtPositionTypeOrgIdAndOperatorExternalUserId(debtPositionTypeOrgId, operatorExternalUserId))
      .thenReturn(Optional.of(buildDebtPositionTypeOrgOperators()));

    DebtPositionTypeOrg result = authorizeOperatorOnDebtPositionTypeService.authorize(orgIpaCode, debtPositionTypeOrgId, operatorExternalUserId);

    assertEquals(debtPositionTypeOrg, result);
  }

  @Test
  void givenOrgTechnicalUserWhenAuthorizeThenSuccess() {
    String operatorExternalUserId = "WS_USER-orgIpaCode";
    String orgIpaCode = "orgIpaCode";
    Long debtPositionTypeOrgId = 2L;

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    DebtPositionTypeOrg result = authorizeOperatorOnDebtPositionTypeService.authorize(orgIpaCode, debtPositionTypeOrgId, operatorExternalUserId);

    assertEquals(debtPositionTypeOrg, result);
  }

  @Test
  void givenSystemTechnicalUserWhenAuthorizeThenSuccess() {
    String operatorExternalUserId = "WS_USER-piattaforma-unitaria_";
    String orgIpaCode = "orgIpaCode";
    Long debtPositionTypeOrgId = 2L;

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    DebtPositionTypeOrg result = authorizeOperatorOnDebtPositionTypeService.authorize(orgIpaCode, debtPositionTypeOrgId, operatorExternalUserId);

    assertEquals(debtPositionTypeOrg, result);
  }

  @Test
  void givenDebtPositionTypeOrgNotFoundWhenAuthorizeThenException() {
    String operatorExternalUserId = "operatorExternalUserId";
    String orgIpaCode = "orgIpaCode";
    Long debtPositionTypeOrgId = 1L;

    when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () ->
      authorizeOperatorOnDebtPositionTypeService.authorize(orgIpaCode, debtPositionTypeOrgId, operatorExternalUserId));

    assertEquals("[P4PA_MISSING_DEBT_POS_TYPE_ORG] The DebtPositionTypeOrg with id 1 was not found", exception.getMessage());
  }

  @Test
  void givenOperatorNotAuthorizedWhenAuthorizeThenException() {
    String operatorExternalUserId = "operatorExternalUserId";
    String orgIpaCode = "orgIpaCode";
    Long debtPositionTypeOrgId = 2L;

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    when(debtPositionTypeOrgOperatorsRepositoryMock.findByDebtPositionTypeOrgIdAndOperatorExternalUserId(debtPositionTypeOrgId, operatorExternalUserId))
      .thenReturn(Optional.empty());

    OperatorNotAuthorizedException exception = assertThrows(OperatorNotAuthorizedException.class, () ->
      authorizeOperatorOnDebtPositionTypeService.authorize(orgIpaCode, debtPositionTypeOrgId, operatorExternalUserId));

    assertEquals("[P4PA_DEBT_POS_TYPE_ORG_UNAUTHORIZED] The operator operatorExternalUserId is not authorized on the DebtPositionTypeOrg 2", exception.getMessage());
  }
}

