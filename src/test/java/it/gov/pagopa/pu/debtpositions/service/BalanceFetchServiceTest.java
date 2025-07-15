package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BalanceFetchServiceTest {

  private BalanceFetchService service;

  @Mock private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock private BalanceService balanceServiceMock;

  @BeforeEach
  void setUp() {
    service = new BalanceFetchService(debtPositionTypeOrgRepositoryMock, balanceServiceMock);
  }

  @Test
  void givenDebtPositionTypeOrgNotValidWhenGetBalanceThenException(){
    String debtPositionTypeOrgCode = "CODE";
    Long orgId = 1L;
    String accessToken = "accessToken";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(orgId, debtPositionTypeOrgCode)).thenReturn(Optional.empty());

    NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
      service.getBalanceDefault(orgId, debtPositionTypeOrgCode, accessToken));
    assertEquals("The debt position type code CODE is not found for this organizationId 1", notFoundException.getMessage());
  }

  @Test
  void givenDebtPositionTypeOrgWithBalanceWhenGetBalanceThenSuccess(){
    String debtPositionTypeOrgCode = "CODE";
    Long orgId = 1L;
    String accessToken = "accessToken";

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(orgId, debtPositionTypeOrgCode)).thenReturn(Optional.of(debtPositionTypeOrg));

    String result = service.getBalanceDefault(orgId, debtPositionTypeOrgCode, accessToken);

    assertEquals(debtPositionTypeOrg.getBalance(), result);
    verify(balanceServiceMock, times(0)).getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrgCode, accessToken);
  }

  @Test
  void givenDebtPositionTypeOrgWithBalanceNullWhenGetBalanceThenSuccess(){
    String debtPositionTypeOrgCode = "CODE";
    Long orgId = 1L;
    String accessToken = "accessToken";

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setBalance(null);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(orgId, debtPositionTypeOrgCode)).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(balanceServiceMock.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrgCode, accessToken)).thenReturn("balance");

    String result = service.getBalanceDefault(orgId, debtPositionTypeOrgCode, accessToken);

    assertEquals("balance", result);
  }

}
