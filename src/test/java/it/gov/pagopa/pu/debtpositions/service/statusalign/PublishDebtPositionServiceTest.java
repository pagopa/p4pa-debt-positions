package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
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
  private ValidateDebtPositionService validateDebtPositionServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;


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
      validateDebtPositionServiceMock,
      debtPositionTypeOrgRepositoryMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock,
      debtPositionSyncServiceMock,
      debtPositionProcessorServiceMock,
      organizationServiceMock,
      debtPositionHierarchyStatusAlignerServiceMock,
      validateDebtPositionServiceMock,
      debtPositionTypeOrgRepositoryMock
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

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));
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
    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    Mockito.verify(validateDebtPositionServiceMock).validateInstallment(installmentDTO, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin());

    assertEquals(DebtPositionStatus.TO_SYNC, result.getLeft().getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, result.getLeft().getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentStatus.TO_SYNC, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());
    assertEquals(InstallmentStatus.DRAFT, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus().getSyncStatusFrom());
    assertEquals(InstallmentStatus.UNPAID, result.getLeft().getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus().getSyncStatusTo());
  }

  @Test
  void givenNonDraftDebtPositionWhenPublishThenThrowConflictErrorException() {
    // Given
    Long debtPositionId = 1L;
    String accessToken = "accessToken";
    String operatorExternalId = "OPERATOREXTERNALID";
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder().massive(false).build();

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    // When & Then
    ConflictErrorException conflictErrorException = assertThrows(ConflictErrorException.class,
      () -> service.publishDebtPosition(debtPositionId, wfExecutionParameters, accessToken, operatorExternalId));
    assertEquals("The debt position with id 1 cannot be published because is not in an allowed status: UNPAID", conflictErrorException.getMessage());
  }
}
