package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowTypeOrgService;
import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.exception.custom.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.MixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionSaveService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowTypeOrg;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildMixedDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MixedDebtPositionCreationServiceTest {

  @InjectMocks
  private MixedDebtPositionCreationServiceImpl mixedDebtPositionCreationService;

  @Mock
  private WorkflowTypeOrgService workflowTypeOrgServiceMock;
  @Mock
  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeServiceMock;
  @Mock
  private MixedDebtPositionCreationServiceImpl.MixedOrdinaryDebtPositionCreationInnerService debtPositionCreationServiceMock;
  @Mock
  private TechnicalMixedDebtPositionBuilderService technicalMixedDebtPositionBuilderServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private DebtPositionSaveService debtPositionSaveServiceMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private MixedDebtPositionMapper mixedDebtPositionMapperMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowTypeOrgServiceMock,
      authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionCreationServiceMock,
      technicalMixedDebtPositionBuilderServiceMock,
      organizationServiceMock,
      debtPositionSaveServiceMock,
      installmentNoPIIRepositoryMock,
      mixedDebtPositionMapperMock,
      debtPositionMapperMock
    );
  }

  @Test
  void whenCreateMixedDebtPositionThenOk() {
    // Given
    String accessToken = "TOKEN";
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    String operatorExternalUserId = "USERID";

    Organization organization = new Organization();
    organization.setIpaCode("ipaCode");
    when(
      organizationServiceMock.getOrganizationById(anyLong(), eq(accessToken)))
      .thenReturn(Optional.of(organization));

    // checkWorkflowTypeOrgExists
    when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(
      eq(organization.getIpaCode()), anyLong(), eq(operatorExternalUserId)))
      .thenReturn(new DebtPositionTypeOrg());
    when(workflowTypeOrgServiceMock.getById(anyString(), eq(accessToken)))
      .thenReturn(Optional.empty());

    // validateIudUniqueness
    when(installmentNoPIIRepositoryMock.isInstallmentExists(
      eq(mixedDebtPositionDTO.getOrganizationId()), anyString(), eq(null),
      eq(null), eq(InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)))
      .thenReturn(false);

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    when(mixedDebtPositionMapperMock.mapToDebtPositionDTO(organization, mixedDebtPositionDTO))
      .thenReturn(debtPositionDTO);

    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");
    when(debtPositionCreationServiceMock.createDebtPosition(eq(debtPositionDTO),
      any(
        WfExecutionParameters.class), eq(accessToken),
      eq(operatorExternalUserId)))
      .thenReturn(workflow);

    Map<Long, List<MixedDpAdditionalData>> debtPositionTypeOrgId2TransfersData = new HashMap<>();
    when(mixedDebtPositionMapperMock.buildDebtPositionTypeOrgId2TransfersData(
      anyList()))
      .thenReturn(debtPositionTypeOrgId2TransfersData);

    DebtPosition debtPosition = new DebtPosition();
    when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(
      debtPosition);

    when(
      technicalMixedDebtPositionBuilderServiceMock.createTechnicalMixedDebtPositions(
        debtPositionTypeOrgId2TransfersData,
        debtPosition, accessToken)).thenReturn(List.of(debtPosition));

    doNothing().when(debtPositionSaveServiceMock)
      .saveDebtPosition(any(DebtPosition.class));

    // When
    Pair<WorkflowCreatedDTO, DebtPositionDTO> result = mixedDebtPositionCreationService.createMixedDebtPosition(
      mixedDebtPositionDTO, accessToken, operatorExternalUserId);

    // Then
    assertEquals(workflow, result.getLeft());
    assertEquals(debtPositionDTO, result.getRight());
  }

  @Test
  void givenNotFoundOrganizationWhenCreateMixedDebtPositionThenThrowException() {
    // Given
    String accessToken = "TOKEN";
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    String operatorExternalUserId = "USERID";

    when(
      organizationServiceMock.getOrganizationById(anyLong(), eq(accessToken)))
      .thenReturn(Optional.empty());

    // When
    Executable exec = () -> mixedDebtPositionCreationService.createMixedDebtPosition(
      mixedDebtPositionDTO, accessToken, operatorExternalUserId);

    // Then
    assertThrows(NotFoundException.class, exec);
  }

  @Test
  void givenNotAuthorizedOperatorWhenCreateMixedDebtPositionThenThrowException() {
    // Given
    String accessToken = "TOKEN";
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    String operatorExternalUserId = "USERID";

    Organization organization = new Organization();
    organization.setIpaCode("ipaCode");
    when(
      organizationServiceMock.getOrganizationById(anyLong(), eq(accessToken)))
      .thenReturn(Optional.of(organization));

    // checkWorkflowTypeOrgExists
    when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(
      eq(organization.getIpaCode()), anyLong(), eq(operatorExternalUserId)))
      .thenThrow(new OperatorNotAuthorizedException("ERROR"));

    // When
    Executable exec = () -> mixedDebtPositionCreationService.createMixedDebtPosition(
      mixedDebtPositionDTO, accessToken, operatorExternalUserId);

    // Then
    assertThrows(OperatorNotAuthorizedException.class, exec);
  }

  @Test
  void givenExistingWorkflowTypeOrgWhenCreateMixedDebtPositionThenThrowException() {
    // Given
    String accessToken = "TOKEN";
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    String operatorExternalUserId = "USERID";

    Organization organization = new Organization();
    organization.setIpaCode("ipaCode");
    when(
      organizationServiceMock.getOrganizationById(anyLong(), eq(accessToken)))
      .thenReturn(Optional.of(organization));

    // checkWorkflowTypeOrgExists
    when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(
      eq(organization.getIpaCode()), anyLong(), eq(operatorExternalUserId)))
      .thenReturn(new DebtPositionTypeOrg());
    when(workflowTypeOrgServiceMock.getById(anyString(), eq(accessToken)))
      .thenReturn(Optional.of(new WorkflowTypeOrg()));

    // When
    Executable exec = () -> mixedDebtPositionCreationService.createMixedDebtPosition(
      mixedDebtPositionDTO, accessToken, operatorExternalUserId);

    // Then
    assertThrows(InvalidValueException.class, exec);
  }


  @Test
  void givenExistingIudWhenCreateMixedDebtPositionThenThrowException() {
    // Given
    String accessToken = "TOKEN";
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    String operatorExternalUserId = "USERID";

    Organization organization = new Organization();
    organization.setIpaCode("ipaCode");
    when(
      organizationServiceMock.getOrganizationById(anyLong(), eq(accessToken)))
      .thenReturn(Optional.of(organization));

    // checkWorkflowTypeOrgExists
    when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(
      eq(organization.getIpaCode()), anyLong(), eq(operatorExternalUserId)))
      .thenReturn(new DebtPositionTypeOrg());
    when(workflowTypeOrgServiceMock.getById(anyString(), eq(accessToken)))
      .thenReturn(Optional.empty());

    // validateIudUniqueness
    when(installmentNoPIIRepositoryMock.isInstallmentExists(
      eq(mixedDebtPositionDTO.getOrganizationId()), anyString(), eq(null),
      eq(null), eq(InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)))
      .thenReturn(true);

    // When
    Executable exec = () -> mixedDebtPositionCreationService.createMixedDebtPosition(
      mixedDebtPositionDTO, accessToken, operatorExternalUserId);

    // Then
    assertThrows(InvalidValueException.class, exec);
  }

}
