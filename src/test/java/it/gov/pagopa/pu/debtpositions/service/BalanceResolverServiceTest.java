package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
import it.gov.pagopa.pu.classification.dto.generated.DebtPositionTypeOrgBalanceCostDTO;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.enums.DebtPositionTypeOrgBalanceCostType;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgBalanceCost;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgBalanceCostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
  @Mock
  private DebtPositionTypeOrgBalanceCostRepository debtPositionTypeOrgBalanceCostRepositoryMock;

  @BeforeEach
  void setUp() {
    service = new BalanceResolverService(
      balanceServiceMock,
      organizationServiceMock,
      debtPositionTypeOrgBalanceCostRepositoryMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      balanceServiceMock,
      organizationServiceMock,
      debtPositionTypeOrgBalanceCostRepositoryMock
    );
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
  void givenInstallmentWithNoBalanceAndNoNotificationFeeCentsWhenUpdateBalanceResolvingAmountThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";
    InstallmentNoPII installment = buildInstallmentNoPII();
    installment.setBalance(null);
    installment.setNotificationFeeCents(null);

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken))
      .thenReturn(Optional.ofNullable(buildOrganization()));

    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance("balance")
      .amountCents(1100L)
      .notificationFeeCents(installment.getNotificationFeeCents())
      .remittanceInformation(installment.getRemittanceInformation())
      .debtPositionTypeOrgBalanceCost(null)
      .build();

    Mockito.when(balanceServiceMock.calculateAmountBalance(amountBalanceRequest, accessToken))
      .thenReturn("balanceResolved");

    service.updateBalanceResolvingAmount(installment, orgId, debtPositionTypeOrg, paymentDateTime, accessToken);

    assertEquals("balanceResolved", installment.getBalance());
  }

  @Test
  void givenInstallmentWithBalanceAndNoNotificationFeeCentsWhenUpdateBalanceResolvingAmountThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";
    InstallmentNoPII installment = buildInstallmentNoPII();
    installment.setNotificationFeeCents(null);

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken))
      .thenReturn(Optional.ofNullable(buildOrganization()));

    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance(installment.getBalance())
      .amountCents(1100L)
      .notificationFeeCents(installment.getNotificationFeeCents())
      .debtPositionTypeOrgBalanceCost(null)
      .remittanceInformation(installment.getRemittanceInformation())
      .build();

    Mockito.when(balanceServiceMock.calculateAmountBalance(amountBalanceRequest, accessToken))
      .thenReturn("balanceResolved");

    service.updateBalanceResolvingAmount(installment, orgId, debtPositionTypeOrg, paymentDateTime, accessToken);

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

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    Mockito.when(balanceServiceMock.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrg.getCode(), accessToken))
      .thenReturn("");

    service.updateBalanceResolvingAmount(installment, orgId, debtPositionTypeOrg, paymentDateTime, accessToken);

    assertEquals("", installment.getBalance());
  }

  @Test
  void givenInstallmentWithOrgNotFoundWhenUpdateBalanceResolvingAmountThenThrowsException() {
    Long orgId = 1L;
    String accessToken = "accessToken";
    InstallmentNoPII installment = buildInstallmentNoPII();

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
      () -> service.updateBalanceResolvingAmount(installment, orgId, debtPositionTypeOrg, paymentDateTime, accessToken));

    assertEquals("ORGANIZATION_NOT_FOUND", exception.getCode());
    assertEquals("Organization with id 1 not found", exception.getMessage());
    Mockito.verify(balanceServiceMock, times(0)).calculateAmountBalance(Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenInstallmentWithBalanceAndNoNotificationFeeCentsAndDebtPositionTypeOrgBalanceCostFoundWhenUpdateBalanceResolvingAmountThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";

    InstallmentNoPII installment = buildInstallmentNoPII();

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Long debtPositionTypeOrgId = debtPositionTypeOrg.getDebtPositionTypeOrgId();

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken))
      .thenReturn(Optional.ofNullable(buildOrganization()));

    DebtPositionTypeOrgBalanceCost debtPositionTypeOrgBalanceCost = new DebtPositionTypeOrgBalanceCost();
    debtPositionTypeOrgBalanceCost.setAssessmentCode("ASS");
    debtPositionTypeOrgBalanceCost.setOfficeCode("OFF");
    debtPositionTypeOrgBalanceCost.setSectionCode("SEC");

    DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId id = new DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId(
      debtPositionTypeOrgId,
      DebtPositionTypeOrgBalanceCostType.NOTIFICATION_COST,
      String.valueOf(LocalDate.now().getYear())
    );

    Mockito.when(debtPositionTypeOrgBalanceCostRepositoryMock.findById(id))
      .thenReturn(Optional.of(debtPositionTypeOrgBalanceCost));

    DebtPositionTypeOrgBalanceCostDTO debtPositionTypeOrgBalanceCostDTO = DebtPositionTypeOrgBalanceCostDTO.builder()
      .assessmentCode(debtPositionTypeOrgBalanceCost.getAssessmentCode())
      .officeCode(debtPositionTypeOrgBalanceCost.getOfficeCode())
      .sectionCode(debtPositionTypeOrgBalanceCost.getSectionCode())
      .build();

    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance(installment.getBalance())
      .amountCents(100L)
      .notificationFeeCents(installment.getNotificationFeeCents())
      .debtPositionTypeOrgBalanceCost(debtPositionTypeOrgBalanceCostDTO)
      .remittanceInformation(installment.getRemittanceInformation())
      .build();

    Mockito.when(balanceServiceMock.calculateAmountBalance(amountBalanceRequest, accessToken))
      .thenReturn("balanceResolved");

    service.updateBalanceResolvingAmount(installment, orgId, debtPositionTypeOrg, paymentDateTime, accessToken);

    assertEquals("balanceResolved", installment.getBalance());
  }

  @Test
  void givenInstallmentWithBalanceAndNoNotificationFeeCentsAndDebtPositionTypeOrgBalanceCostNotFoundWhenUpdateBalanceResolvingAmountThenSuccess() {
    Long orgId = 1L;
    String accessToken = "accessToken";

    InstallmentNoPII installment = buildInstallmentNoPII();
    installment.setBalance("existingBalance");

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    Long debtPositionTypeOrgId = debtPositionTypeOrg.getDebtPositionTypeOrgId();

    Mockito.when(organizationServiceMock.getOrganizationById(orgId, accessToken))
      .thenReturn(Optional.ofNullable(buildOrganization()));

    DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId id = new DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId(
      debtPositionTypeOrgId,
      DebtPositionTypeOrgBalanceCostType.NOTIFICATION_COST,
      String.valueOf(LocalDate.now().getYear())
    );

    Mockito.when(debtPositionTypeOrgBalanceCostRepositoryMock.findById(id))
      .thenReturn(Optional.empty());

    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance(installment.getBalance())
      .amountCents(100L)
      .notificationFeeCents(installment.getNotificationFeeCents())
      .debtPositionTypeOrgBalanceCost(null)
      .remittanceInformation(installment.getRemittanceInformation())
      .build();

    Mockito.when(balanceServiceMock.calculateAmountBalance(amountBalanceRequest, accessToken))
      .thenReturn("balanceResolved");

    service.updateBalanceResolvingAmount(installment, orgId, debtPositionTypeOrg, paymentDateTime, accessToken);

    assertEquals("balanceResolved", installment.getBalance());
  }
}
