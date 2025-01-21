package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgOperatorsFaker.buildDebtPositionTypeOrgOperators;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizeOperatorOnDebtPositionTypeServiceTest {

  @Mock
  private DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepositoryMock;

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;

  @BeforeEach
  void init() {
    authorizeOperatorOnDebtPositionTypeService = new AuthorizeOperatorOnDebtPositionTypeServiceImpl(debtPositionTypeOrgOperatorsRepositoryMock, debtPositionTypeOrgRepositoryMock);
  }

  @Test
  void givenAuthorizeThenSuccess() {
    String username = "username";
    Long orgId = 1L;
    Long debtPositionTypeOrgId = 1L;

    DebtPositionTypeOrg debtPositionTypeOrgDTO = new DebtPositionTypeOrg();
    debtPositionTypeOrgDTO.setDebtPositionTypeOrgId(debtPositionTypeOrgId);
    debtPositionTypeOrgDTO.setOrganizationId(orgId);

    when(debtPositionTypeOrgOperatorsRepositoryMock.findDebtPositionTypeOrgOperatorsByOperatorExternalUserId(username))
      .thenReturn(buildDebtPositionTypeOrgOperators());

    when(debtPositionTypeOrgRepositoryMock.findDebtPositionTypeOrgByOrganizationIdAndDebtPositionTypeOrgId(orgId, debtPositionTypeOrgId))
      .thenReturn(Optional.of(debtPositionTypeOrgDTO));

    DebtPositionTypeOrg result = authorizeOperatorOnDebtPositionTypeService.authorize(orgId, debtPositionTypeOrgId, username);

    assertEquals(debtPositionTypeOrgDTO, result);
  }

  @Test
  void givenAuthorizeWhenNotEqualTypeCodeThenThrowValidatorException() {
    String username = "username";
    Long orgId = 1L;
    Long debtPositionTypeOrgId = 1L;

    DebtPositionTypeOrg debtPositionTypeOrgDTO = new DebtPositionTypeOrg();
    debtPositionTypeOrgDTO.setDebtPositionTypeOrgId(1L);

    when(debtPositionTypeOrgOperatorsRepositoryMock.findDebtPositionTypeOrgOperatorsByOperatorExternalUserId(username))
      .thenReturn(buildDebtPositionTypeOrgOperators());

    when(debtPositionTypeOrgRepositoryMock.findDebtPositionTypeOrgByOrganizationIdAndDebtPositionTypeOrgId(orgId, debtPositionTypeOrgId))
      .thenReturn(Optional.empty());

    assertThrows(OperatorNotAuthorizedException.class, () ->
      authorizeOperatorOnDebtPositionTypeService.authorize(orgId, debtPositionTypeOrgId, username));
  }
}

