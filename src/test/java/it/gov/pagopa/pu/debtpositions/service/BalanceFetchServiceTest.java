package it.gov.pagopa.pu.debtpositions.service;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
  void givenDebtPositionTypeOrgWithBalanceWhenGetBalanceThenSuccess(){
    Long orgId = 1L;
    String accessToken = "accessToken";

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    String result = service.getBalanceDefault(orgId, debtPositionTypeOrg, accessToken);

    assertEquals(debtPositionTypeOrg.getBalance(), result);
    verify(balanceServiceMock, times(0)).getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrg.getCode(), accessToken);
  }

  @Test
  void givenDebtPositionTypeOrgWithBalanceNullWhenGetBalanceThenSuccess(){
    Long orgId = 1L;
    String accessToken = "accessToken";

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setBalance(null);

    Mockito.when(balanceServiceMock.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrg.getCode(), accessToken)).thenReturn("balance");

    String result = service.getBalanceDefault(orgId, debtPositionTypeOrg, accessToken);

    assertEquals("balance", result);
  }

}
