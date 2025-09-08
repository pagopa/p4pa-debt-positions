package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import static it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService.DEBT_POSITION_TYPE_MIXED;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildMixedDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowTypeOrgService;
import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.MixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowTypeOrg;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MixedDebtPositionCreationServiceTest {

  @InjectMocks
  private MixedDebtPositionCreationServiceImpl mixedDebtPositionCreationService;

  @Mock
  private WorkflowTypeOrgService workflowTypeOrgServiceMock;
  @Mock
  private DebtPositionCreationService debtPositionCreationServiceMock;
  @Mock
  private MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverServiceMock;
  @Mock
  private TechnicalMixedDebtPositionBuilderService technicalMixedDebtPositionBuilderServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
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
      debtPositionCreationServiceMock,
      mixedDebtPositionTypeOrgRetrieverServiceMock,
      technicalMixedDebtPositionBuilderServiceMock,
      debtPositionTypeOrgRepositoryMock,
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

    // checkWorkflowTypeOrgExists
    when(workflowTypeOrgServiceMock.getById(anyString(), eq(accessToken)))
      .thenReturn(Optional.empty());

    // validateIudUniqueness
    when(installmentNoPIIRepositoryMock.isInstallmentExists(
      eq(mixedDebtPositionDTO.getOrganizationId()), anyString(), eq(null),
      eq(null), eq(InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS)))
      .thenReturn(false);

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setOrganizationId(
      mixedDebtPositionDTO.getOrganizationId());
    debtPositionTypeOrg.setDebtPositionTypeOrgId(DEBT_POSITION_TYPE_MIXED);
    when(
      debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndDebtPositionTypeOrgId(
        mixedDebtPositionDTO.getOrganizationId(), DEBT_POSITION_TYPE_MIXED))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    when(mixedDebtPositionMapperMock.mapToDebtPositionDTO(mixedDebtPositionDTO,
      debtPositionTypeOrg.getDebtPositionTypeOrgId()))
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
        debtPosition)).thenReturn(Collections.emptyList());

    // When
    Pair<WorkflowCreatedDTO, DebtPositionDTO> result = mixedDebtPositionCreationService.createMixedDebtPosition(
      mixedDebtPositionDTO, accessToken, operatorExternalUserId);

    // Then
    assertEquals(workflow, result.getLeft());
    assertEquals(debtPositionDTO, result.getRight());

    verifyNoInteractions(mixedDebtPositionTypeOrgRetrieverServiceMock);
  }

  @Test
  void givenExistingWorkflowTypeOrgWhenCreateMixedDebtPositionThenThrowException() {
    // Given
    String accessToken = "TOKEN";
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    String operatorExternalUserId = "USERID";

    // checkWorkflowTypeOrgExists
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

    // checkWorkflowTypeOrgExists
    when(workflowTypeOrgServiceMock.getById(anyString(), eq(accessToken)))
      .thenReturn(Optional.empty());

    // validateIudUniqueness
    when(installmentNoPIIRepositoryMock.isInstallmentExists(
      eq(mixedDebtPositionDTO.getOrganizationId()), anyString(), eq(null),
      eq(null), eq(InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS)))
      .thenReturn(true);

    // When
    Executable exec = () -> mixedDebtPositionCreationService.createMixedDebtPosition(
      mixedDebtPositionDTO, accessToken, operatorExternalUserId);

    // Then
    assertThrows(InvalidValueException.class, exec);
  }

  @Test
  void givenMissingDebtPositionTypeOrgWhenCreateMixedDebtPositionThenRetrieveMixed() {
    // Given
    String accessToken = "TOKEN";
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    String operatorExternalUserId = "USERID";

    // checkWorkflowTypeOrgExists
    when(workflowTypeOrgServiceMock.getById(anyString(), eq(accessToken)))
      .thenReturn(Optional.empty());

    // validateIudUniqueness
    when(installmentNoPIIRepositoryMock.isInstallmentExists(
      eq(mixedDebtPositionDTO.getOrganizationId()), anyString(), eq(null),
      eq(null), eq(InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS)))
      .thenReturn(false);

    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setOrganizationId(
      mixedDebtPositionDTO.getOrganizationId());
    debtPositionTypeOrg.setDebtPositionTypeOrgId(DEBT_POSITION_TYPE_MIXED);
    when(
      debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndDebtPositionTypeOrgId(
        mixedDebtPositionDTO.getOrganizationId(), DEBT_POSITION_TYPE_MIXED))
      .thenReturn(Optional.empty());
    when(mixedDebtPositionTypeOrgRetrieverServiceMock.getMixedDebtPositionTypeOrg(mixedDebtPositionDTO.getOrganizationId()))
      .thenReturn(debtPositionTypeOrg);

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    when(mixedDebtPositionMapperMock.mapToDebtPositionDTO(mixedDebtPositionDTO,
      debtPositionTypeOrg.getDebtPositionTypeOrgId()))
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
        debtPosition)).thenReturn(Collections.emptyList());

    // When
    Pair<WorkflowCreatedDTO, DebtPositionDTO> result = mixedDebtPositionCreationService.createMixedDebtPosition(
      mixedDebtPositionDTO, accessToken, operatorExternalUserId);

    // Then
    assertEquals(workflow, result.getLeft());
    assertEquals(debtPositionDTO, result.getRight());
  }
}
