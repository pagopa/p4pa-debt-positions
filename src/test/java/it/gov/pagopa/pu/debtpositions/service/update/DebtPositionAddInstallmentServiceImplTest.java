package it.gov.pagopa.pu.debtpositions.service.update;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtPositionAddInstallmentServiceImplTest {

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
  private DebtPositionCreationService debtPositionCreationServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

  private DebtPositionAddInstallmentService debtPositionAddInstallmentService;

  @BeforeEach
  void setUp() {
    debtPositionAddInstallmentService = new DebtPositionAddInstallmentServiceImpl(authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock, debtPositionSyncServiceMock, debtPositionProcessorServiceMock,
      organizationServiceMock, debtPositionHierarchyStatusAlignerServiceMock, validateDebtPositionServiceMock,
      debtPositionCreationServiceMock, debtPositionTypeOrgRepositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(authorizeOperatorOnDebtPositionTypeServiceMock,
      debtPositionServiceMock, debtPositionSyncServiceMock, debtPositionProcessorServiceMock,
      organizationServiceMock, debtPositionHierarchyStatusAlignerServiceMock, validateDebtPositionServiceMock,
      debtPositionCreationServiceMock, debtPositionTypeOrgRepositoryMock);
  }

  @Test
  void whenAddInstallmentThenOK(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";

    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.TO_SYNC);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    String iud = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud();
    WorkflowCreatedDTO workflow = new WorkflowCreatedDTO("workflowId", "runId");

    Mockito.doNothing().when(validateDebtPositionServiceMock).validateDebtorConsistency(debtPositionDTO);
    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(),2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(validateDebtPositionServiceMock).validateInstallment(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), organization, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin(), debtPositionDTO.getFlagPuPagoPaPayment());
    Mockito.doNothing().when(debtPositionCreationServiceMock).checkInstallment(debtPositionDTO, organization, debtPositionTypeOrg, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken);
    Mockito.when(debtPositionSyncServiceMock.syncDebtPosition(debtPositionDTO, wfExecutionParameters, PaymentEventType.DPI_ADDED, "IUD:"+iud, accessToken))
      .thenReturn(workflow);

    WorkflowCreatedDTO result = debtPositionAddInstallmentService.addInstallment(debtPositionDTO, List.of(buildInstallmentDTO()), wfExecutionParameters, accessToken, operatorExternalId);

    assertSame(workflow, result);
    assertEquals(DebtPositionStatus.TO_SYNC, debtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.TO_SYNC, debtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertEquals(new InstallmentSyncStatus(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID), debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.TO_SYNC, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

    Mockito.verify(validateDebtPositionServiceMock).validateDebtorConsistency(debtPositionDTO);
    Mockito.verify(debtPositionProcessorServiceMock).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void whenAddInstallmentDraftThenOK(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";

    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.DRAFT);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.DRAFT);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setSyncStatus(null);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.doNothing().when(validateDebtPositionServiceMock).validateDebtorConsistency(debtPositionDTO);
    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(),2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(validateDebtPositionServiceMock).validateInstallment(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), organization, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin(), debtPositionDTO.getFlagPuPagoPaPayment());
    Mockito.doNothing().when(debtPositionCreationServiceMock).checkInstallment(debtPositionDTO, organization, debtPositionTypeOrg, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(), accessToken);

    WorkflowCreatedDTO result = debtPositionAddInstallmentService.addInstallment(debtPositionDTO, List.of(buildInstallmentDTO()), wfExecutionParameters, accessToken, operatorExternalId);

    assertNull(result);
    assertEquals(DebtPositionStatus.DRAFT, debtPositionDTO.getStatus());
    assertEquals(PaymentOptionStatus.DRAFT, debtPositionDTO.getPaymentOptions().getFirst().getStatus());
    assertNull(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getSyncStatus());
    assertEquals(InstallmentStatus.DRAFT, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

    Mockito.verify(validateDebtPositionServiceMock).validateDebtorConsistency(debtPositionDTO);
    Mockito.verify(debtPositionProcessorServiceMock).updateAmounts(debtPositionDTO);
    Mockito.verify(debtPositionServiceMock).saveDebtPosition(debtPositionDTO);
    Mockito.verify(debtPositionHierarchyStatusAlignerServiceMock).alignHierarchyStatus(debtPositionDTO);
  }

  @Test
  void givenDuplicatePaymentOptionIndexWhenAddInstallmentThenThrowInvalidValueException(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";

    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    PaymentOptionDTO po = debtPositionDTO.getPaymentOptions().getFirst();
    po.setStatus(PaymentOptionStatus.TO_SYNC);
    debtPositionDTO.setPaymentOptions(List.of(po, po));

    List<InstallmentDTO> installments2operate = po.getInstallments();

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.doNothing().when(validateDebtPositionServiceMock).validateDebtorConsistency(debtPositionDTO);
    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(),2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(validateDebtPositionServiceMock).validateInstallment(po.getInstallments().getFirst(), organization, accessToken, debtPositionTypeOrg, debtPositionDTO.getDebtPositionOrigin(), debtPositionDTO.getFlagPuPagoPaPayment());
    Mockito.doNothing().when(debtPositionCreationServiceMock).checkInstallment(debtPositionDTO, organization, debtPositionTypeOrg, po.getInstallments().getFirst(), accessToken);

    InvalidValueException result = assertThrows(InvalidValueException.class, () -> debtPositionAddInstallmentService.addInstallment(debtPositionDTO, installments2operate, wfExecutionParameters, accessToken, operatorExternalId));

    Assertions.assertEquals("DUPLICATED_PAYMENT_OPTION_INDEX",result.getCode());
    Assertions.assertEquals("PaymentOption index duplicated: 1", result.getMessage());
  }

  @Test
  void givenDPTypeOrgNotFoundWhenTestAddInstallmentThenOK(){
    String accessToken = "ACCESSTOKEN";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    String operatorExternalId = "OPERATOREXTERNALID";

    Organization organization = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.TO_SYNC);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    List<InstallmentDTO> installments2Operate = List.of(buildInstallmentDTO());

    Mockito.doNothing().when(validateDebtPositionServiceMock).validateDebtorConsistency(debtPositionDTO);
    Mockito.when(organizationServiceMock.getOrganizationById(debtPositionDTO.getOrganizationId(), accessToken))
      .thenReturn(Optional.of(organization));
    Mockito.when(authorizeOperatorOnDebtPositionTypeServiceMock.authorize(organization.getIpaCode(),2L, operatorExternalId))
      .thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findById(2L))
      .thenReturn(Optional.empty());

    NotFoundException conflictException = assertThrows(NotFoundException.class, () ->
      debtPositionAddInstallmentService.addInstallment(debtPositionDTO,installments2Operate, wfExecutionParameters, accessToken, operatorExternalId));

    assertEquals("DEBT_POSITION_TYPE_ORG_NOT_FOUND",conflictException.getCode());
    assertEquals("The debt position type org with id 2 was not found for organization id 500", conflictException.getMessage());
  }

}
