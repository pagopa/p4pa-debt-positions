package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
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
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
  private DebtPositionMapper debtPositionMapperMock;
  @Mock
  private DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerServiceMock;

  private DebtPositionCancelInstallmentService debtPositionCancelInstallmentService;

  @BeforeEach
  void setUp() {
    debtPositionCancelInstallmentService = new DebtPositionCancelInstallmentServiceImpl(authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock, debtPositionSyncServiceMock, debtPositionProcessorServiceMock,
      organizationServiceMock, debtPositionMapperMock, debtPositionHierarchyStatusAlignerServiceMock);
  }

  @Test
  void testCancelInstallmentThenOK(){
    String accessToken = "ACCESSTOKEN";
    boolean massive = true;
    String operatorExternalId = "OPERATOREXTERNALID";
    String workflowId = "workflowId";

    Organization organization = buildOrganization();
    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.TO_SYNC);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken)).thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(2L, operatorExternalId)).thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionServiceMock.saveDebtPosition(debtPositionDTO, organization)).thenReturn(Pair.of(debtPosition, debtPositionDTO));
    Mockito.when(debtPositionProcessorServiceMock.updateAmounts(debtPositionDTO)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(Pair.of(debtPosition, Map.of(installmentNoPII, buildInstallment())));
    Mockito.when(debtPositionHierarchyStatusAlignerServiceMock.alignHierarchyStatus(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, massive, PaymentEventType.DPI_CANCELLED, accessToken)).thenReturn(WorkflowCreatedDTO.builder().workflowId(workflowId).build());

    org.apache.commons.lang3.tuple.Pair<DebtPositionDTO, String> result = debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, List.of(buildInstallmentDTO()), massive, accessToken, operatorExternalId);

    assertEquals(workflowId, result.getRight());
    DebtPositionDTO resultDebtPositionDTO = result.getLeft();
    assertEquals(DebtPositionStatus.TO_SYNC, resultDebtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, resultDebtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.UNPAID, InstallmentStatus.CANCELLED), resultDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, resultDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

  }
}
