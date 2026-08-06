package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionUpdateInstallmentServiceImplTest {

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
  private ValidateDebtPositionService validateDebtPositionServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

  private DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService;

  @BeforeEach
  void setUp() {
    debtPositionUpdateInstallmentService = new DebtPositionUpdateInstallmentServiceImpl(authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock, debtPositionSyncServiceMock, debtPositionProcessorServiceMock,
      organizationServiceMock, debtPositionHierarchyStatusAlignerServiceMock,
      validateDebtPositionServiceMock, debtPositionTypeOrgRepositoryMock);
  }

  @Test
  void testUpdateInstallmentThenOK(){
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
    String iud = installmentDTO.getIud();

    when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(),2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);
    when(debtPositionTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(validateDebtPositionServiceMock).validateInstallment(installmentDTO, organization, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin(), debtPositionDTO.getFlagPuPagoPaPayment());
    when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DPI_UPDATED, "IUD:"+iud, accessToken))
      .thenReturn(workflow);

    WorkflowCreatedDTO result = debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, List.of(installmentDTO), wfExecutionParameters, accessToken, operatorExternalId);

    assertSame(workflow, result);
    assertEquals(DebtPositionStatus.TO_SYNC, debtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, debtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.UNPAID, InstallmentStatus.UNPAID), installmentDTO.getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, installmentDTO.getStatus());

    verify(debtPositionProcessorServiceMock).updateAmounts(debtPositionDTO);
    verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO, accessToken);
    verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void testUpdateInstallmentExpiredThenOK(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.TO_SYNC);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(),2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);
    when(debtPositionTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(validateDebtPositionServiceMock).validateInstallment(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), organization, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin(), debtPositionDTO.getFlagPuPagoPaPayment());
    when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DPI_UPDATED, "IUD:"+iud, accessToken))
      .thenReturn(workflow);

    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.EXPIRED);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(LocalDate.now().plusDays(1));

    WorkflowCreatedDTO result = debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO,
      List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), wfExecutionParameters, accessToken, operatorExternalId);

    assertSame(workflow, result);
    assertEquals(DebtPositionStatus.TO_SYNC, debtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, debtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.EXPIRED, InstallmentStatus.UNPAID), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

    verify(debtPositionProcessorServiceMock).updateAmounts(debtPositionDTO);
    verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO, accessToken);
    verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void testUpdateInstallmentWhenDPTypeOrgNotFoundThenException(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";

    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    List<InstallmentDTO> installments = List.of(buildInstallmentDTO());

    when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(),2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);
    when(debtPositionTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () -> debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, installments, wfExecutionParameters, accessToken, operatorExternalId));

    assertEquals("DEBT_POSITION_TYPE_ORG_NOT_FOUND",exception.getCode());
    assertEquals("The debt position type org with id 2 was not found for organization id 500", exception.getMessage());
  }
}
