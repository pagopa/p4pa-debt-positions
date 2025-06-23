package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistry;
import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistryStatus;
import it.gov.pagopa.pu.classification.dto.generated.PagedModelAssessmentsRegistry;
import it.gov.pagopa.pu.classification.dto.generated.PagedModelAssessmentsRegistryEmbedded;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.AssessmentsRegistryService;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeFetchBalanceServiceImplTest {

  @Mock
  private AssessmentsRegistryService assessmentsRegistryServiceMock;
  @Mock
  private BalanceService balanceServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  private static final String OPERATING_YEAR = String.valueOf(LocalDate.now().getYear());
  private static final AssessmentsRegistryStatus ASSESSMENTS_REGISTRY_STATUS = AssessmentsRegistryStatus.ACTIVE;

  private InstallmentSynchronizeFetchBalanceService installmentSynchronizeFetchBalanceService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeFetchBalanceService = new InstallmentSynchronizeFetchBalanceService(debtPositionTypeOrgRepositoryMock,
      assessmentsRegistryServiceMock, balanceServiceMock);
  }
  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepositoryMock,
      assessmentsRegistryServiceMock, balanceServiceMock
    );
  }

  @Test
  void givenNoBalanceInDTOWhenRetrieveFromDebtPositionTypeOrgThenReturnBalance() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("DPT_CODE");
    String accessToken = "ACCESS_TOKEN";

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setBalance("RETRIEVED_BALANCE");

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "DPT_CODE"))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    // When
    String result = installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken);

    // Then
    assertEquals("RETRIEVED_BALANCE", result);
  }

  @Test
  void givenPagedModelAssessmentsRegistryNullWhenGetDebtPositionTypeDefaultBalanceThenEmptyString() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("DPT_CODE");
    dto.setAmountCents(1000L);
    String accessToken = "ACCESS_TOKEN";

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "DPT_CODE"))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(assessmentsRegistryServiceMock.findAssessmentsRegistriesByFilters(
      dto.getOrganizationId(),
      Set.of(dto.getDebtPositionTypeCode()),
      null, null, null, null, null, null,
      OPERATING_YEAR,
      ASSESSMENTS_REGISTRY_STATUS,
      0,
      1,
      null,
      accessToken
    )).thenReturn(null);

    // When
    String result = installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken);

    //Then
    assertEquals("", result);
  }

  @Test
  void givenPagedModelAssessmentsRegistryListEmptyWhenGetDebtPositionTypeDefaultBalanceThenEmptyString() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("DPT_CODE");
    dto.setAmountCents(1000L);
    String accessToken = "ACCESS_TOKEN";

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    PagedModelAssessmentsRegistry registryResponse = new PagedModelAssessmentsRegistry();
    registryResponse.setEmbedded(new PagedModelAssessmentsRegistryEmbedded(List.of()));

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "DPT_CODE"))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(assessmentsRegistryServiceMock.findAssessmentsRegistriesByFilters(
      dto.getOrganizationId(),
      Set.of(dto.getDebtPositionTypeCode()),
      null, null, null, null, null, null,
      OPERATING_YEAR,
      ASSESSMENTS_REGISTRY_STATUS,
      0,
      1,
      null,
      accessToken
    )).thenReturn(registryResponse);

    // When
    String result = installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken);

    //Then
    assertEquals("", result);
  }

  @Test
  void givenPagedModelAssessmentsRegistryEmbeddedNullWhenGetDebtPositionTypeDefaultBalanceThenEmptyString() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("DPT_CODE");
    dto.setAmountCents(1000L);
    String accessToken = "ACCESS_TOKEN";

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    PagedModelAssessmentsRegistry registryResponse = new PagedModelAssessmentsRegistry();
    registryResponse.setEmbedded(null);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "DPT_CODE"))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(assessmentsRegistryServiceMock.findAssessmentsRegistriesByFilters(
      dto.getOrganizationId(),
      Set.of(dto.getDebtPositionTypeCode()),
      null, null, null, null, null, null,
      OPERATING_YEAR,
      ASSESSMENTS_REGISTRY_STATUS,
      0,
      1,
      null,
      accessToken
    )).thenReturn(registryResponse);

    // When
    String result = installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken);

    //Then
    assertEquals("", result);
  }

  @Test
  void givenDebtPositionTypeOrgNotFoundWhenGetDebtPositionTypeDefaultBalanceThenThrowException() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("INVALID_CODE");
    String accessToken = "ACCESS_TOKEN";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "INVALID_CODE"))
      .thenReturn(Optional.empty());

    // When & Then
    InvalidValueException exception = assertThrows(InvalidValueException.class,
      () -> installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken));

    assertEquals("The debt position type code INVALID_CODE is not valid for this organizationId 1", exception.getMessage());
  }

  @Test
  void givenPagedModelAssessmentsRegistryListTooManyAssessmentsWhenGetDebtPositionTypeDefaultBalanceThenThrowException() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("DPT_CODE");
    dto.setAmountCents(1000L);
    String accessToken = "ACCESS_TOKEN";

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    PagedModelAssessmentsRegistry registryResponse = new PagedModelAssessmentsRegistry();
    registryResponse.setEmbedded(new PagedModelAssessmentsRegistryEmbedded(List.of(new AssessmentsRegistry(), new AssessmentsRegistry())));

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "DPT_CODE"))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(assessmentsRegistryServiceMock.findAssessmentsRegistriesByFilters(
      dto.getOrganizationId(),
      Set.of(dto.getDebtPositionTypeCode()),
      null, null, null, null, null, null,
      OPERATING_YEAR,
      ASSESSMENTS_REGISTRY_STATUS,
      0,
      1,
      null,
      accessToken
    )).thenReturn(registryResponse);

    // When & Then
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken));

    assertEquals("Expected exactly one Assessments registry result, but found 2.", exception.getMessage());
  }

  @Test
  void givenIncorrectXmlWhenGetDebtPositionTypeDefaultBalanceThenThrowException() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("DPT_CODE");
    dto.setAmountCents(100L);
    String accessToken = "ACCESS_TOKEN";

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    AssessmentsRegistry assessmentsRegistry = new AssessmentsRegistry();
    assessmentsRegistry.sectionCode("sectionCode");
    assessmentsRegistry.setOfficeCode("officeCode");
    assessmentsRegistry.setAssessmentCode("codAccertamento");
    PagedModelAssessmentsRegistry registryResponse = new PagedModelAssessmentsRegistry();
    registryResponse.setEmbedded(new PagedModelAssessmentsRegistryEmbedded(List.of(assessmentsRegistry)));

    String expectedResult = "<bilancio><capitolo><codCapitolo>sectionCode</codCapitolo><codUfficio>officeCode</codUfficio><accertamento><codAccertamento>codAccertamento</codAccertamento><importo>1.00</importo></accertamento></capitolo></bilancio>";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "DPT_CODE"))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(assessmentsRegistryServiceMock.findAssessmentsRegistriesByFilters(
      dto.getOrganizationId(),
      Set.of(dto.getDebtPositionTypeCode()),
      null, null, null, null, null, null,
      OPERATING_YEAR,
      ASSESSMENTS_REGISTRY_STATUS,
      0,
      1,
      null,
      accessToken
    )).thenReturn(registryResponse);
    Mockito.when(balanceServiceMock.isValidBalance(expectedResult, accessToken)).thenReturn(false);

    // When & Then
    InvalidValueException exception = assertThrows(InvalidValueException.class,
      () -> installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken));

    assertEquals("Balance is not formally valid", exception.getMessage());
  }

  @Test
  void givenPagedModelAssessmentsRegistryWhenGetDebtPositionTypeDefaultBalanceThenGenerateXml() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("DPT_CODE");
    dto.setAmountCents(100L);
    String accessToken = "ACCESS_TOKEN";

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    AssessmentsRegistry assessmentsRegistry = new AssessmentsRegistry();
    assessmentsRegistry.sectionCode("sectionCode");
    assessmentsRegistry.setOfficeCode("officeCode");
    assessmentsRegistry.setAssessmentCode("codAccertamento");
    PagedModelAssessmentsRegistry registryResponse = new PagedModelAssessmentsRegistry();
    registryResponse.setEmbedded(new PagedModelAssessmentsRegistryEmbedded(List.of(assessmentsRegistry)));

    String expectedResult = "<bilancio><capitolo><codCapitolo>sectionCode</codCapitolo><codUfficio>officeCode</codUfficio><accertamento><codAccertamento>codAccertamento</codAccertamento><importo>1.00</importo></accertamento></capitolo></bilancio>";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "DPT_CODE"))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(assessmentsRegistryServiceMock.findAssessmentsRegistriesByFilters(
      dto.getOrganizationId(),
      Set.of(dto.getDebtPositionTypeCode()),
      null, null, null, null, null, null,
      OPERATING_YEAR,
      ASSESSMENTS_REGISTRY_STATUS,
      0,
      1,
      null,
      accessToken
    )).thenReturn(registryResponse);
    Mockito.when(balanceServiceMock.isValidBalance(expectedResult, accessToken)).thenReturn(true);

    // When & Then
    String result = installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken);

    assertEquals(result, expectedResult);
  }

  @Test
  void givenPagedModelAssessmentsRegistryNoOfficeCodeWhenGetDebtPositionTypeDefaultBalanceThenGenerateXml() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("DPT_CODE");
    dto.setAmountCents(100L);
    String accessToken = "ACCESS_TOKEN";

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    AssessmentsRegistry assessmentsRegistry = new AssessmentsRegistry();
    assessmentsRegistry.sectionCode("sectionCode");
    assessmentsRegistry.setAssessmentCode("codAccertamento");
    PagedModelAssessmentsRegistry registryResponse = new PagedModelAssessmentsRegistry();
    registryResponse.setEmbedded(new PagedModelAssessmentsRegistryEmbedded(List.of(assessmentsRegistry)));

    String expectedResult = "<bilancio><capitolo><codCapitolo>sectionCode</codCapitolo><accertamento><codAccertamento>codAccertamento</codAccertamento><importo>1.00</importo></accertamento></capitolo></bilancio>";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "DPT_CODE"))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(assessmentsRegistryServiceMock.findAssessmentsRegistriesByFilters(
      dto.getOrganizationId(),
      Set.of(dto.getDebtPositionTypeCode()),
      null, null, null, null, null, null,
      OPERATING_YEAR,
      ASSESSMENTS_REGISTRY_STATUS,
      0,
      1,
      null,
      accessToken
    )).thenReturn(registryResponse);
    Mockito.when(balanceServiceMock.isValidBalance(expectedResult, accessToken)).thenReturn(true);

    // When & Then
    String result = installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken);

    assertEquals(result, expectedResult);
  }

  @Test
  void givenPagedModelAssessmentsRegistryNoAssessmentCodeWhenGetDebtPositionTypeDefaultBalanceThenGenerateXml() {
    // Given
    InstallmentSynchronizeDTO dto = new InstallmentSynchronizeDTO();
    dto.setOrganizationId(1L);
    dto.setDebtPositionTypeCode("DPT_CODE");
    dto.setAmountCents(100L);
    String accessToken = "ACCESS_TOKEN";

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();

    AssessmentsRegistry assessmentsRegistry = new AssessmentsRegistry();
    assessmentsRegistry.sectionCode("sectionCode");
    assessmentsRegistry.setOfficeCode("officeCode");
    PagedModelAssessmentsRegistry registryResponse = new PagedModelAssessmentsRegistry();
    registryResponse.setEmbedded(new PagedModelAssessmentsRegistryEmbedded(List.of(assessmentsRegistry)));

    String expectedResult = "<bilancio><capitolo><codCapitolo>sectionCode</codCapitolo><codUfficio>officeCode</codUfficio><accertamento><importo>1.00</importo></accertamento></capitolo></bilancio>";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(1L, "DPT_CODE"))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.when(assessmentsRegistryServiceMock.findAssessmentsRegistriesByFilters(
      dto.getOrganizationId(),
      Set.of(dto.getDebtPositionTypeCode()),
      null, null, null, null, null, null,
      OPERATING_YEAR,
      ASSESSMENTS_REGISTRY_STATUS,
      0,
      1,
      null,
      accessToken
    )).thenReturn(registryResponse);
    Mockito.when(balanceServiceMock.isValidBalance(expectedResult, accessToken)).thenReturn(true);

    // When & Then
    String result = installmentSynchronizeFetchBalanceService.getDebtPositionTypeDefaultBalance(dto, accessToken);

    assertEquals(result, expectedResult);
  }

}
