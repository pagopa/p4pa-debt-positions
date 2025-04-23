package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
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
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallment;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionDeletionServiceImplTest {

  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private PaymentOptionRepository paymentOptionRepositoryMock;
  @Mock
  private InstallmentPIIRepository installmentPIIRepositoryMock;
  @Mock
  private TransferRepository transferRepositoryMock;
  @Mock
  private InstallmentMapper installmentMapperMock;
  @Mock
  private DebtPositionCancelInstallmentServiceImpl debtPositionCancelInstallmentServiceMock;

  private DebtPositionDeletionService debtPositionDeletionService;

  private static final String OPERATOR_EXTERNAL_ID = "operatorExternalId";
  private static final String ACCESS_TOKEN = "accessToken";
  private static final String WORKFLOW_ID = "workflowId";

  @BeforeEach
  void setUp() {
    debtPositionDeletionService = new DebtPositionDeletionServiceImpl(debtPositionServiceMock,
      debtPositionRepositoryMock, paymentOptionRepositoryMock, installmentPIIRepositoryMock,
      transferRepositoryMock, installmentMapperMock, debtPositionCancelInstallmentServiceMock);
  }

  @Test
  void givenDraftDebtPositionWhenDeleteThenNoWorkflow(){
    Long debtPositionId = 1L;
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    Installment installment = buildInstallment();

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);
    Mockito.doNothing().when(transferRepositoryMock).deleteById(1000L);
    Mockito.when(installmentMapperMock.mapToModel(buildInstallmentDTO())).thenReturn(installment);
    Mockito.doNothing().when(installmentPIIRepositoryMock).delete(installment);
    Mockito.doNothing().when(paymentOptionRepositoryMock).deleteById(10L);
    Mockito.doNothing().when(debtPositionRepositoryMock).deleteById(1L);

    String result = debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID);

    assertNull(result);
    Mockito.verify(debtPositionCancelInstallmentServiceMock, Mockito.times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionWithInstallmentNotifiedWhenDeleteThenThrowException(){
    Long debtPositionId = 1L;
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("The debt position with id 1 cannot be deleted because it is been notified", exception.getMessage());

    Mockito.verify(transferRepositoryMock, Mockito.times(0)).deleteById(1000L);
    Mockito.verify(installmentMapperMock, Mockito.times(0)).mapToModel(buildInstallmentDTO());
    Mockito.verify(installmentPIIRepositoryMock, Mockito.times(0)).delete(buildInstallment());
    Mockito.verify(paymentOptionRepositoryMock, Mockito.times(0)).deleteById(10L);
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(0)).deleteById(1L);
    Mockito.verify(debtPositionCancelInstallmentServiceMock, Mockito.times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionInPaidStatusWhenDeleteThenThrowException(){
    Long debtPositionId = 1L;
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setIun(null);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);

    Mockito.when(debtPositionServiceMock.getDebtPosition(debtPositionId)).thenReturn(debtPositionDTO);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> debtPositionDeletionService.deleteDebtPosition(debtPositionId, ACCESS_TOKEN, OPERATOR_EXTERNAL_ID));

    assertEquals("The debt position with id 1 cannot be deleted because is not in allowed status: PAID", exception.getMessage());

    Mockito.verify(transferRepositoryMock, Mockito.times(0)).deleteById(1000L);
    Mockito.verify(installmentMapperMock, Mockito.times(0)).mapToModel(buildInstallmentDTO());
    Mockito.verify(installmentPIIRepositoryMock, Mockito.times(0)).delete(buildInstallment());
    Mockito.verify(paymentOptionRepositoryMock, Mockito.times(0)).deleteById(10L);
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(0)).deleteById(1L);
    Mockito.verify(debtPositionCancelInstallmentServiceMock, Mockito.times(0))
      .cancelInstallment(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenDebtPositionUnpaidWhenDeleteThenSuccess(){
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

    Mockito.verify(transferRepositoryMock, Mockito.times(0)).deleteById(1000L);
    Mockito.verify(installmentMapperMock, Mockito.times(0)).mapToModel(buildInstallmentDTO());
    Mockito.verify(installmentPIIRepositoryMock, Mockito.times(0)).delete(buildInstallment());
    Mockito.verify(paymentOptionRepositoryMock, Mockito.times(0)).deleteById(10L);
    Mockito.verify(debtPositionRepositoryMock, Mockito.times(0)).deleteById(1L);
  }

}
