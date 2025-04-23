package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionCancelInstallmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
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
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    String result = debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertNull(result);
    Mockito.verify(debtPositionCancelInstallmentServiceMock, Mockito.times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionWithInstallmentNotifiedWhenDeleteThenThrowException() {
    Long debtPositionId = 1L;
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("The debt position with id 1 cannot be deleted because it is been notified", exception.getMessage());

    Mockito.verify(debtPositionCancelInstallmentServiceMock, Mockito.times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionInPaidStatusWhenDeleteThenThrowException() {
    Long debtPositionId = 1L;
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

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
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionCancelInstallmentServiceMock.cancelInstallment(debtPositionDTO,
        List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), wfExecutionParameters, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID))
      .thenReturn(WORKFLOW_ID);

    String result = debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertEquals(WORKFLOW_ID, result);

  }

}
