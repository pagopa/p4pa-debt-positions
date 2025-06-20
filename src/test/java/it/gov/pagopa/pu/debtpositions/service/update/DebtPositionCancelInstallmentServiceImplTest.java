package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.delete.InstallmentDeletionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DebtPositionCancelInstallmentServiceImplTest {

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
  @Mock
  private InstallmentDeletionService installmentDeletionServiceMock;

  private DebtPositionCancelInstallmentService debtPositionCancelInstallmentService;

  @BeforeEach
  void setUp() {
    debtPositionCancelInstallmentService = new DebtPositionCancelInstallmentServiceImpl(authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock, debtPositionSyncServiceMock, debtPositionProcessorServiceMock,
      organizationServiceMock, debtPositionHierarchyStatusAlignerServiceMock, installmentDeletionServiceMock);
  }

  @Test
  void testCancelInstallmentThenOK(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.TO_SYNC);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    installmentDTO.setStatus(InstallmentStatus.UNPAID);
    installmentDTO.setSyncStatus(null);
    installmentDTO.setIun(null);
    String iud = installmentDTO.getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), 2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DPI_CANCELLED, "IUD:"+iud, accessToken))
      .thenReturn(workflow);

    WorkflowCreatedDTO result = debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, List.of(installmentDTO), wfExecutionParameters, accessToken, operatorExternalId);

    assertSame(workflow, result);
    assertEquals(DebtPositionStatus.TO_SYNC, debtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, debtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.UNPAID, InstallmentStatus.CANCELLED), installmentDTO.getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, installmentDTO.getStatus());

    Mockito.verify(debtPositionProcessorServiceMock).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDraftDebtPositionWhenCancelInstallmentThenDeleteDraftInstallments(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    PaymentOptionDTO paymentOptionDTO1 = buildPaymentOptionDTO();
    PaymentOptionDTO paymentOptionDTO2 = buildPaymentOptionDTO();
    paymentOptionDTO2.setPaymentOptionId(20L);
    InstallmentDTO installmentDTO1 = buildInstallmentDTO();
    installmentDTO1.setInstallmentId(200L);
    installmentDTO1.setIun(null);
    installmentDTO1.setStatus(InstallmentStatus.DRAFT);
    InstallmentDTO installmentDTO2 = buildInstallmentDTO();
    installmentDTO2.setInstallmentId(300L);
    installmentDTO2.setStatus(InstallmentStatus.DRAFT);
    paymentOptionDTO2.setInstallments(new ArrayList<>(List.of(installmentDTO1, installmentDTO2)));
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(paymentOptionDTO1, paymentOptionDTO2)));

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), 2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);

    WorkflowCreatedDTO result = debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, List.of(installmentDTO1), wfExecutionParameters, accessToken, operatorExternalId);

    assertNull(result);
    Mockito.verify(installmentDeletionServiceMock).deleteDraftInstallments(debtPositionDTO, Set.of(200L));
    Mockito.verify(debtPositionProcessorServiceMock).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock, times(0)).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock, times(0)).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenInstallmentInStatusPaidWhenCancelThenException(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";

    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    List<InstallmentDTO> installments = List.of(buildInstallmentDTO().status(InstallmentStatus.PAID));

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(), 2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class, () -> debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, installments, wfExecutionParameters, accessToken, operatorExternalId));
    assertEquals("The installment with id 100 cannot be cancelled because is not in allowed status: PAID", exception.getMessage());
  }
}
