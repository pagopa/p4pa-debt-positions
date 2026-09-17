package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.RelateUserToDefaultDPTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultDpTypeOrgOperatorFacadeServiceImplTest {

  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private it.gov.pagopa.pu.debtpositions.connector.migration.service.DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsServiceMock;
  @Mock
  private DebtPositionTypeOrgOperatorsFacadeService dptoServiceMock;

  @InjectMocks
  private DefaultDpTypeOrgOperatorFacadeServiceImpl service;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationServiceMock,
      debtPositionTypeOrgOperatorsServiceMock,
      dptoServiceMock
    );
  }

  @Test
  void whenRelateUserToDefaultDPTypeOrgThenOk() {
    // Given
    String accessToken = "accessToken";
    RelateUserToDefaultDPTypeOrgDTO relateUserToDefaultDPTypeOrgDTO = podamFactory.manufacturePojo(RelateUserToDefaultDPTypeOrgDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    List<Long> unconsumedDebtPositionTypeOrgIds = List.of(10L, 20L);
    List<DebtPositionTypeOrgOperators> expectedResult = List.of(new DebtPositionTypeOrgOperators());

    when(organizationServiceMock.getOrganizationById(relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    when(debtPositionTypeOrgOperatorsServiceMock.getUnconsumedDebtPositionTypeOrgIds(
      relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), relateUserToDefaultDPTypeOrgDTO.getFiscalCode(), organization.getIpaCode()))
      .thenReturn(unconsumedDebtPositionTypeOrgIds);
    when(dptoServiceMock.saveDebtPositionTypeOrgOperatorsForNewOperator(
      relateUserToDefaultDPTypeOrgDTO.getOperatorExternalUserId(), relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), new HashSet<>(unconsumedDebtPositionTypeOrgIds)))
      .thenReturn(expectedResult);
    doNothing().when(debtPositionTypeOrgOperatorsServiceMock).consumeDebtPositionTypeOrgOperators(
      relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), unconsumedDebtPositionTypeOrgIds, relateUserToDefaultDPTypeOrgDTO.getFiscalCode(), organization.getIpaCode());

    // When
    List<DebtPositionTypeOrgOperators> result = service.relateUserToDefaultDPTypeOrg(relateUserToDefaultDPTypeOrgDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenEmptyUnconsumedDebtPositionTypeOrgIdsWhenRelateUserToDefaultDPTypeOrgThenNoConsume() {
    // Given
    String accessToken = "accessToken";
    RelateUserToDefaultDPTypeOrgDTO relateUserToDefaultDPTypeOrgDTO = podamFactory.manufacturePojo(RelateUserToDefaultDPTypeOrgDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    List<DebtPositionTypeOrgOperators> expectedResult = List.of(new DebtPositionTypeOrgOperators());

    when(organizationServiceMock.getOrganizationById(relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    when(debtPositionTypeOrgOperatorsServiceMock.getUnconsumedDebtPositionTypeOrgIds(
      relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), relateUserToDefaultDPTypeOrgDTO.getFiscalCode(), organization.getIpaCode()))
      .thenReturn(List.of());
    when(dptoServiceMock.saveDebtPositionTypeOrgOperatorsForNewOperator(
      relateUserToDefaultDPTypeOrgDTO.getOperatorExternalUserId(), relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), new HashSet<>()))
      .thenReturn(expectedResult);

    // When
    List<DebtPositionTypeOrgOperators> result = service.relateUserToDefaultDPTypeOrg(relateUserToDefaultDPTypeOrgDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoFiscalCodeWhenRelateUserToDefaultDPTypeOrgThenNoMigrationCalls() {
    // Given
    String accessToken = "accessToken";
    RelateUserToDefaultDPTypeOrgDTO relateUserToDefaultDPTypeOrgDTO = podamFactory.manufacturePojo(RelateUserToDefaultDPTypeOrgDTO.class);
    relateUserToDefaultDPTypeOrgDTO.setFiscalCode(null);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    List<DebtPositionTypeOrgOperators> expectedResult = List.of(new DebtPositionTypeOrgOperators());

    when(organizationServiceMock.getOrganizationById(relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    when(dptoServiceMock.saveDebtPositionTypeOrgOperatorsForNewOperator(
      relateUserToDefaultDPTypeOrgDTO.getOperatorExternalUserId(), relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), new HashSet<>()))
      .thenReturn(expectedResult);

    // When
    List<DebtPositionTypeOrgOperators> result = service.relateUserToDefaultDPTypeOrg(relateUserToDefaultDPTypeOrgDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenOrganizationNotFoundWhenRelateUserToDefaultDPTypeOrgThenNotFoundException() {
    // Given
    String accessToken = "accessToken";
    RelateUserToDefaultDPTypeOrgDTO relateUserToDefaultDPTypeOrgDTO = podamFactory.manufacturePojo(RelateUserToDefaultDPTypeOrgDTO.class);

    when(organizationServiceMock.getOrganizationById(relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.empty());

    // When / Then
    Assertions.assertThrows(NotFoundException.class,
      () -> service.relateUserToDefaultDPTypeOrg(relateUserToDefaultDPTypeOrgDTO, accessToken));
  }
}
