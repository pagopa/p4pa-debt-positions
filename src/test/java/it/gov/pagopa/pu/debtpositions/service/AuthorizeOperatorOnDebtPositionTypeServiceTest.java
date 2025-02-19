package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizeOperatorOnDebtPositionTypeServiceTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;

  @BeforeEach
  void init() {
    authorizeOperatorOnDebtPositionTypeService = new AuthorizeOperatorOnDebtPositionTypeServiceImpl(debtPositionTypeOrgRepositoryMock);
  }

  @Test
  void givenAuthorizeThenSuccess() {
    String username = "username";
    Long debtPositionTypeOrgId = 1L;

    DebtPositionTypeOrg debtPositionTypeOrgDTO = new DebtPositionTypeOrg();
    debtPositionTypeOrgDTO.setDebtPositionTypeOrgId(debtPositionTypeOrgId);

    when(debtPositionTypeOrgRepositoryMock.findByDebtPositionTypeOrgIdAndOperatorExternalUserId(debtPositionTypeOrgId, username))
      .thenReturn(Optional.of(debtPositionTypeOrgDTO));

    DebtPositionTypeOrg result = authorizeOperatorOnDebtPositionTypeService.authorize(debtPositionTypeOrgId, username);

    assertEquals(debtPositionTypeOrgDTO, result);
  }

  @Test
  void givenAuthorizeWhenNotEqualTypeCodeThenThrowValidatorException() {
    String username = "username";
    Long debtPositionTypeOrgId = 1L;

    DebtPositionTypeOrg debtPositionTypeOrgDTO = new DebtPositionTypeOrg();
    debtPositionTypeOrgDTO.setDebtPositionTypeOrgId(1L);

    when(debtPositionTypeOrgRepositoryMock.findByDebtPositionTypeOrgIdAndOperatorExternalUserId(debtPositionTypeOrgId, username))
      .thenReturn(Optional.empty());

    assertThrows(OperatorNotAuthorizedException.class, () ->
      authorizeOperatorOnDebtPositionTypeService.authorize(debtPositionTypeOrgId, username));
  }
}

