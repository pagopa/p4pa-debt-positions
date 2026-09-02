package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.common.ConflictException;
import it.gov.pagopa.pu.debtpositions.exception.common.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionCancelInstallmentServiceImpl;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionDeletionServiceImplTest {

  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeServiceMock;
  @Mock
  private DebtPositionCancelInstallmentServiceImpl debtPositionCancelInstallmentServiceMock;

  private DebtPositionDeletionService debtPositionDeletionService;

  private static final String OPERATOR_EXTERNAL_ID = "operatorExternalId";
  private static final String ACCESS_TOKEN = "accessToken";
  private static final WorkflowCreatedDTO WORKFLOW = new WorkflowCreatedDTO("workflowId", "runId");

  @BeforeEach
  void setUp() {
    debtPositionDeletionService = new DebtPositionDeletionServiceImpl(debtPositionServiceMock,
      organizationServiceMock, authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionCancelInstallmentServiceMock);
  }

  @Test
  void givenDraftDebtPositionWhenDeleteThenNoWorkflow() {
    Long debtPositionId = 1L;
    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.DRAFT);

    when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);
    when(organizationServiceMock.getOrganizationById(debtPosition.getOrganizationId(), ACCESS_TOKEN)).thenReturn(Optional.ofNullable(organization));
    when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPosition.getDebtPositionTypeOrgId(), OPERATOR_EXTERNAL_ID))
      .thenReturn(buildDebtPositionTypeOrg());
    Mockito.doNothing().when(debtPositionServiceMock).delete(debtPosition);

    WorkflowCreatedDTO result = debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertNull(result);
    verify(debtPositionCancelInstallmentServiceMock, times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDraftDebtPositionWithOrgInactiveWhenDeleteThenThrowException() {
    Long debtPositionId = 1L;
    Organization organization = buildOrganization();
    organization.setStatus(OrganizationStatus.DRAFT);
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.DRAFT);

    when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);
    when(organizationServiceMock.getOrganizationById(debtPosition.getOrganizationId(), ACCESS_TOKEN)).thenReturn(Optional.of(organization));

    InvalidValueException exception = assertThrows(InvalidValueException.class,
      () -> debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("INVALID_ORGANIZATION_STATUS",exception.getCode());
    assertEquals("Provided organization is not ACTIVE", exception.getMessage());

  }

  @Test
  void givenDebtPositionWithInstallmentNotifiedWhenDeleteThenThrowException() {
    Long debtPositionId = 1L;
    DebtPosition debtPosition = buildDebtPosition();

    when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);

    ConflictException exception = assertThrows(ConflictException.class,
      () -> debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("INVALID_DEBT_POSITION_STATUS",exception.getCode());
    assertEquals("DebtPosition with id 1 cannot be deleted because it is been notified", exception.getMessage());

    verify(debtPositionCancelInstallmentServiceMock, times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionInPaidStatusWhenDeleteThenThrowException() {
    Long debtPositionId = 1L;
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);
    debtPosition.setStatus(DebtPositionStatus.PAID);

    when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);

    ConflictException exception = assertThrows(ConflictException.class,
      () -> debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("INVALID_DEBT_POSITION_STATUS",exception.getCode());
    assertEquals("DebtPosition with id 1 cannot be deleted because is not in allowed status: PAID", exception.getMessage());

    verify(debtPositionCancelInstallmentServiceMock, times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionUnpaidWhenDeleteThenSuccess() {
    Long debtPositionId = 1L;
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder().massive(false).partialChange(false).build();
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.UNPAID);
    InstallmentNoPII installment = debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst();
    installment.setIun(null);
    installment.setStatus(InstallmentStatus.UNPAID);
    installment.setSyncStatus(null);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    installmentDTO.setStatus(InstallmentStatus.UNPAID);
    installmentDTO.setSyncStatus(null);

    when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);
    when(debtPositionServiceMock.mapDebtPosition(debtPosition)).thenReturn(debtPositionDTO);
    when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO,
        List.of(installmentDTO), wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW);

    WorkflowCreatedDTO result = debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertSame(WORKFLOW, result);

  }

}
