package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BalanceResolverServiceTest {

  private BalanceResolverService service;

  @Mock
  private BalanceService balanceServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;

  @BeforeEach
  void setUp() {
    service = new BalanceResolverService(balanceServiceMock, organizationServiceMock);
  }

  @Test
  void givenDebtPositionTypeOrgWithBalanceWhenGetBalanceThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    String result = service.getBalanceDefault(orgId, debtPositionTypeOrg, accessToken);

    assertEquals(debtPositionTypeOrg.getBalance(), result);
    verify(balanceServiceMock, times(0)).getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrg.getCode(), accessToken);
  }

  @Test
  void givenDebtPositionTypeOrgWithBalanceNullWhenGetBalanceThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setBalance(null);

    Mockito.when(balanceServiceMock.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrg.getCode(), accessToken)).thenReturn("balance");

    String result = service.getBalanceDefault(orgId, debtPositionTypeOrg, accessToken);

    assertEquals("balance", result);
  }

  @Test
  void givenInstallmentWhenResolveAmountBalanceThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";
    InstallmentNoPII installment = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken)).thenReturn(Optional.ofNullable(buildOrganization()));
    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance(installment.getBalance()).amountCents(100L).remittanceInformation(installment.getRemittanceInformation()).build();
    Mockito.when(balanceServiceMock.calculateAmountBalance(amountBalanceRequest, accessToken)).thenReturn("balanceResolved");

    String result = service.resolveAmountBalance(orgId, installment, accessToken);

    assertEquals("balanceResolved", result);
  }

  @Test
  void givenInstallmentWithOrgNotFoundWhenResolveAmountBalanceThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";
    InstallmentNoPII installment = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
      () -> service.resolveAmountBalance(orgId, installment, accessToken));

    assertEquals("ORGANIZATION_NOT_FOUND",exception.getCode());
    assertEquals("Organization with id 1 not found", exception.getMessage());
    Mockito.verify(balanceServiceMock, times(0)).calculateAmountBalance(Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenInstallmentWithNoBalanceWhenUpdateBalanceResolvingAmountThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";
    InstallmentNoPII installment = buildInstallmentNoPII();
    installment.setBalance(null);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken))
      .thenReturn(Optional.ofNullable(buildOrganization()));
    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance("balance").amountCents(100L).remittanceInformation(installment.getRemittanceInformation()).build();
    Mockito.when(balanceServiceMock.calculateAmountBalance(amountBalanceRequest, accessToken))
      .thenReturn("balanceResolved");

    service.updateBalanceResolvingAmount(installment, orgId, debtPositionTypeOrg, accessToken);

    assertEquals("balanceResolved", installment.getBalance());
  }

  @Test
  void givenInstallmentWhenUpdateBalanceResolvingAmountThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";
    InstallmentNoPII installment = buildInstallmentNoPII();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken))
      .thenReturn(Optional.ofNullable(buildOrganization()));
    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance(installment.getBalance()).amountCents(100L).remittanceInformation(installment.getRemittanceInformation()).build();
    Mockito.when(balanceServiceMock.calculateAmountBalance(amountBalanceRequest, accessToken))
      .thenReturn("balanceResolved");

    service.updateBalanceResolvingAmount(installment, orgId, debtPositionTypeOrg, accessToken);

    assertEquals("balanceResolved", installment.getBalance());
  }

  @Test
  void givenInstallmentWithEmptyBalanceWhenUpdateBalanceResolvingAmountThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";
    InstallmentNoPII installment = buildInstallmentNoPII();
    installment.setBalance("");
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setBalance("");

    Mockito.when(balanceServiceMock.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrg.getCode(), accessToken))
      .thenReturn("");

    service.updateBalanceResolvingAmount(installment, orgId, debtPositionTypeOrg, accessToken);

    assertEquals("", installment.getBalance());
  }
}
