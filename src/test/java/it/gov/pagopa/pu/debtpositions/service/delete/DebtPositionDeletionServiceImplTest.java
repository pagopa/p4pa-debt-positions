package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionCancelInstallmentServiceImpl;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
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
  private static final String WORKFLOW_ID = "workflowId";

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

    Mockito.when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);
    Mockito.when(organizationServiceMock.getOrganizationById(debtPosition.getOrganizationId(), ACCESS_TOKEN)).thenReturn(Optional.ofNullable(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), debtPosition.getDebtPositionTypeOrgId(), OPERATOR_EXTERNAL_ID))
      .thenReturn(buildDebtPositionTypeOrg());
    Mockito.doNothing().when(debtPositionServiceMock).delete(debtPosition);

    String result = debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertNull(result);
    Mockito.verify(debtPositionCancelInstallmentServiceMock, Mockito.times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDraftDebtPositionWithOrgInactiveWhenDeleteThenThrowException() {
    Long debtPositionId = 1L;
    Organization organization = buildOrganization();
    organization.setStatus(OrganizationStatus.DRAFT);
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.DRAFT);

    Mockito.when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);
    Mockito.when(organizationServiceMock.getOrganizationById(debtPosition.getOrganizationId(), ACCESS_TOKEN)).thenReturn(Optional.of(organization));

    InvalidValueException exception = assertThrows(InvalidValueException.class,
      () -> debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("Provided organization is not ACTIVE", exception.getMessage());

  }

  @Test
  void givenDebtPositionWithInstallmentNotifiedWhenDeleteThenThrowException() {
    Long debtPositionId = 1L;
    DebtPosition debtPosition = buildDebtPosition();

    Mockito.when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("The debt position with id 1 cannot be deleted because it is been notified", exception.getMessage());

    Mockito.verify(debtPositionCancelInstallmentServiceMock, Mockito.times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionInPaidStatusWhenDeleteThenThrowException() {
    Long debtPositionId = 1L;
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);
    debtPosition.setStatus(DebtPositionStatus.PAID);

    Mockito.when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("The debt position with id 1 cannot be deleted because is not in allowed status: PAID", exception.getMessage());

    Mockito.verify(debtPositionCancelInstallmentServiceMock, Mockito.times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionUnpaidWhenDeleteThenSuccess() {
    Long debtPositionId = 1L;
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder().massive(false).partialChange(false).build();
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.UNPAID);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionServiceMock.getDebtPositionNoPII(debtPositionId)).thenReturn(debtPosition);
    Mockito.when(debtPositionServiceMock.mapDebtPosition(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_ID);

    String result = debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertEquals(WORKFLOW_ID, result);

  }

}
