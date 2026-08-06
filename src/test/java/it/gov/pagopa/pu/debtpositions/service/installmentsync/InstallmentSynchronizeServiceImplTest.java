package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.common.ConflictException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeCancelService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeInsertService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeUpdateService;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.dto.generated.Action.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeServiceImplTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;
  @Mock
  private InstallmentSynchronizeCancelService installmentSynchronizeCancelServiceMock;
  @Mock
  private InstallmentSynchronizeUpdateService installmentSynchronizeUpdateServiceMock;
  @Mock
  private InstallmentSynchronizeInsertService installmentSynchronizeInsertServiceMock;

  private InstallmentSynchronizeService installmentSynchronizeService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeService = new InstallmentSynchronizeServiceImpl(debtPositionRepositoryMock,
      debtPositionMapperMock, installmentSynchronizeCancelServiceMock,
      installmentSynchronizeUpdateServiceMock, installmentSynchronizeInsertServiceMock);
  }

  @Test
  void testInstallmentSynchronizeWithDebtPositionExistingWithDifferentOrigin(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAction(A);

    DebtPosition debtPosition = buildDebtPosition();
    when(debtPositionRepositoryMock.findEntityGraphByIupdOrgAndOrganizationId(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getOrganizationId())).thenReturn(debtPosition);

    assertThrows(ConflictException.class, () -> installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, operatorExternalUserId),
      String.format("There is another debt position with iupd %s requested but different origin", installmentSynchronizeDTO.getIupdOrg()));

    verify(debtPositionMapperMock, times(0)).mapToDto(debtPosition);
  }

  @Test
  void testInstallmentSynchronizeCancelActionThenOk(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAction(A);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(debtPositionOrigin);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    when(debtPositionRepositoryMock.findEntityGraphByIupdOrgAndOrganizationId(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getOrganizationId()))
      .thenReturn(debtPosition);
    when(debtPositionMapperMock.mapToDto(debtPosition))
      .thenReturn(debtPositionDTO);
    when(installmentSynchronizeCancelServiceMock.syncInstallment(installmentSynchronizeDTO, debtPositionDTO,
      wfExecutionParameters, accessToken, operatorExternalUserId))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }

  @Test
  void testInstallmentSynchronizeCancelActionWhenDPNullThenOk(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAction(A);

    when(debtPositionRepositoryMock.findEntityGraphByIupdOrgAndOrganizationId(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getOrganizationId())).thenReturn(null);
    when(installmentSynchronizeCancelServiceMock.syncInstallment(installmentSynchronizeDTO, null,
      wfExecutionParameters, accessToken, operatorExternalUserId)).thenReturn(null);

    WorkflowCreatedDTO result = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, operatorExternalUserId);

    assertNull(result);
  }

  @Test
  void testInstallmentSynchronizeUpdateActionThenOk(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAction(M);

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(debtPositionOrigin);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    when(debtPositionRepositoryMock.findEntityGraphByIupdOrgAndOrganizationId(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getOrganizationId())).thenReturn(debtPosition);
    when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);
    when(installmentSynchronizeUpdateServiceMock.syncInstallment(installmentSynchronizeDTO, debtPositionDTO,
      wfExecutionParameters, accessToken, operatorExternalUserId)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }

  @Test
  void testInstallmentSynchronizeInsertActionThenOk(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAction(I);

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(debtPositionOrigin);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    when(debtPositionRepositoryMock.findEntityGraphByIupdOrgAndOrganizationId(installmentSynchronizeDTO.getIupdOrg(),
        installmentSynchronizeDTO.getOrganizationId())).thenReturn(debtPosition);
    when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);
    when(installmentSynchronizeInsertServiceMock.syncInstallment(installmentSynchronizeDTO, debtPositionDTO,
      wfExecutionParameters, accessToken, operatorExternalUserId)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }

  @Test
  void givenInstallmentDTOWithIupdNullWhenSynchronizeUpdateActionThenOk() {
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIupdOrg(null);
    installmentSynchronizeDTO.setAction(M);

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(debtPositionOrigin);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");

    when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndInstallmentIud(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getIud(), List.of(debtPositionOrigin))).thenReturn(List.of(debtPosition));
    when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);
    when(installmentSynchronizeUpdateServiceMock.syncInstallment(installmentSynchronizeDTO, debtPositionDTO,
      wfExecutionParameters, accessToken, operatorExternalUserId)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, operatorExternalUserId);

    assertSame(expectedResult, result);
  }

  @Test
  void givenInstallmentDTOWithIupdNullWhenSynchronizeCancelActionAndfindEntityGraphByOrganizationIdAndInstallmentIudReturnNullDpThenOk(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIupdOrg(null);
    installmentSynchronizeDTO.setAction(A);

    when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndInstallmentIud(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getIud(), List.of(debtPositionOrigin))).thenReturn(Collections.emptyList());
    when(installmentSynchronizeCancelServiceMock.syncInstallment(installmentSynchronizeDTO, null,
      wfExecutionParameters, accessToken, operatorExternalUserId)).thenReturn(null);

    WorkflowCreatedDTO result = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, operatorExternalUserId);

    assertNull(result);
  }

  @Test
  void givenInstallmentDTOWithIupdNullWhenSynchronizeCancelActionAndFindEntityGraphByOrganizationIdAndInstallmentIudReturnMultipleDpDpThenThrowEx() {
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setIupdOrg(null);
    installmentSynchronizeDTO.setAction(A);

    List<DebtPosition> debtPositions = List.of(buildDebtPosition(), buildDebtPosition());
    when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndInstallmentIud(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getIud(), List.of(debtPositionOrigin))).thenReturn(debtPositions);

    assertThrows(ConflictException.class, () -> installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, debtPositionOrigin, accessToken, operatorExternalUserId),
      String.format("Multiple debt positions found for iud %s", installmentSynchronizeDTO.getIud()));

    verify(debtPositionMapperMock, times(0)).mapToDto(debtPositions.getFirst());
  }
}
