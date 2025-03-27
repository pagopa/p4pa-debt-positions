package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
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

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    String workflowId = "workflowId";

    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.TO_SYNC);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(2L, operatorExternalId)).thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(2L)).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(validateDebtPositionServiceMock).validateInstallment(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin());
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO, organization)).thenReturn(debtPosition);
    Mockito.when(debtPositionProcessorServiceMock.updateAmounts(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionHierarchyStatusAlignerServiceMock.alignHierarchyStatusAndRemap(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DPI_UPDATED, "IUD:"+iud, accessToken)).thenReturn(WorkflowCreatedDTO.builder().workflowId(workflowId).build());

    org.apache.commons.lang3.tuple.Pair<DebtPositionDTO, String> result = debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, List.of(buildInstallmentDTO()), wfExecutionParameters, accessToken, operatorExternalId);

    assertEquals(workflowId, result.getRight());
    DebtPositionDTO resultDebtPositionDTO = result.getLeft();
    assertEquals(DebtPositionStatus.TO_SYNC, resultDebtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, resultDebtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.UNPAID, InstallmentStatus.UNPAID), resultDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, resultDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

  }

  @Test
  void testUpdateInstallmentExpiredThenOK(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";
    String workflowId = "workflowId";

    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.TO_SYNC);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(2L, operatorExternalId)).thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(2L)).thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(validateDebtPositionServiceMock).validateInstallment(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin());
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO, organization)).thenReturn(debtPosition);
    Mockito.when(debtPositionProcessorServiceMock.updateAmounts(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionHierarchyStatusAlignerServiceMock.alignHierarchyStatusAndRemap(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DPI_UPDATED, "IUD:"+iud, accessToken)).thenReturn(WorkflowCreatedDTO.builder().workflowId(workflowId).build());

    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.EXPIRED);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(LocalDate.now().plusDays(1));

    org.apache.commons.lang3.tuple.Pair<DebtPositionDTO, String> result = debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO,
      List.of(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst()), wfExecutionParameters, accessToken, operatorExternalId);

    assertEquals(workflowId, result.getRight());
    DebtPositionDTO resultDebtPositionDTO = result.getLeft();
    assertEquals(DebtPositionStatus.TO_SYNC, resultDebtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, resultDebtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.EXPIRED, InstallmentStatus.UNPAID), resultDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, resultDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

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

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(2L, operatorExternalId)).thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(2L)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () -> debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, installments, wfExecutionParameters, accessToken, operatorExternalId));

    assertEquals("The debt position type org with id 2 was not found for organization id 500", exception.getMessage());
  }
}
