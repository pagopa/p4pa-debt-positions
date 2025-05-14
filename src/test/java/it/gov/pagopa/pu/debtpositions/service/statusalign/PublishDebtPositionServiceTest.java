package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PublishDebtPositionServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;


  @Mock
  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeServiceMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private DebtPositionSyncService debtPositionSyncServiceMock;
  @Mock
  private DebtPositionProcessorService debtPositionProcessorServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerServiceMock;

  private PublishDebtPositionService service;

  @BeforeEach
  void setUp() {
    service = new PublishDebtPositionServiceImpl(
      authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock,
      debtPositionSyncServiceMock,
      debtPositionProcessorServiceMock,
      organizationServiceMock,
      debtPositionHierarchyStatusAlignerServiceMock,
      debtPositionRepositoryMock,
      debtPositionMapperMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock,
      debtPositionSyncServiceMock,
      debtPositionProcessorServiceMock,
      organizationServiceMock,
      debtPositionHierarchyStatusAlignerServiceMock,
      debtPositionRepositoryMock,
      debtPositionMapperMock
    );
  }

  @Test
  void givenPublishDebtPositionThenOk() {
    // Given
    Long debtPositionId = 1L;
    String operatorExternalId = "OPERATOREXTERNALID";
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.DRAFT);
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .build();
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");
    String accessToken = "accessToken";
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Long debtPositionTypeOrgId = 2L;
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPositionTypeOrgId, operatorExternalId)).thenReturn(debtPositionTypeOrg);

    Mockito.doAnswer(invocation -> {
      DebtPositionDTO dp = invocation.getArgument(0);
      dp.setStatus(DebtPositionStatus.TO_SYNC);
      dp.getPaymentOptions().forEach(po -> po.setStatus(PaymentOptionStatus.TO_SYNC));
      return null;
    }).when(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(Mockito.any());

    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DP_CREATED, "IUD:" + iud, accessToken))
      .thenReturn(workflow);

    // When
    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = service.publishDebtPosition(debtPositionId, wfExecutionParameters, accessToken, operatorExternalId);

    // Then
    Mockito.verify(debtPositionProcessorServiceMock, Mockito.times(1)).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);

    assertEquals(DebtPositionStatus.TO_SYNC, result.getLeft().getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, result.getLeft().getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.TO_SYNC, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertEquals(InstallmentStatus.DRAFT, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus().getSyncStatusFrom());
    assertEquals(InstallmentStatus.UNPAID, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus().getSyncStatusTo());
  }

  @Test
  void givenPublishDebtPositionWhenDebtPositionNotFoundThenThrowNotFoundException() {
    // Given
    Long debtPositionId = 1L;
    String accessToken = "accessToken";
    String operatorExternalId = "OPERATOREXTERNALID";
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .build();

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(null);

    // When & Then
    assertThrows(NotFoundException.class, () -> service.publishDebtPosition(debtPositionId, wfExecutionParameters, accessToken, operatorExternalId),
      "Debt position related to the id 1 does not found");
  }

  @Test
  void givenNonDraftDebtPositionWhenPublishThenThrowException() {
    // Given
    Long debtPositionId = 1L;
    String accessToken = "accessToken";
    String operatorExternalId = "OPERATOREXTERNALID";
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder().massive(false).build();

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.TO_SYNC);

    DebtPositionDTO dto = buildDebtPositionDTO();
    dto.setStatus(DebtPositionStatus.TO_SYNC);

    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(dto);
    Mockito.when(organizationServiceMock.getOrganizationById(dto.getOrganizationId(), accessToken)).thenReturn(Optional.of(buildOrganization()));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(buildDebtPositionTypeOrg());

    // When & Then
    assertThrows(InvalidValueException.class,
      () -> service.publishDebtPosition(debtPositionId, wfExecutionParameters, accessToken, operatorExternalId),
      "Only debt positions in DRAFT status can be published"
    );
  }
}
