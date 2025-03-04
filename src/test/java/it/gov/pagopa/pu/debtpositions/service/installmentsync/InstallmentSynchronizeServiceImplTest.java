package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeCancelService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

  private InstallmentSynchronizeService installmentSynchronizeService;

  @BeforeEach
  void setUp() {
    installmentSynchronizeService = new InstallmentSynchronizeServiceImpl(debtPositionRepositoryMock,
      debtPositionMapperMock, installmentSynchronizeCancelServiceMock, installmentSynchronizeUpdateServiceMock);
  }

  @Test
  void testInstallmentSynchronizeWithDebtPositionExistingWithDifferentOrigin(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    boolean massive = false;
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAction(InstallmentSynchronizeDTO.ActionEnum.A);

    DebtPosition debtPosition = buildDebtPosition();
    Mockito.when(debtPositionRepositoryMock.findByIupdOrgAndOrganizationId(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getOrganizationId())).thenReturn(debtPosition);

    assertThrows(ConflictErrorException.class, () -> installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, massive, debtPositionOrigin, accessToken, operatorExternalUserId),
      String.format("There is another debt position with iupd %s requested but different origin", installmentSynchronizeDTO.getIupdOrg()));

    verify(debtPositionMapperMock, times(0)).mapToDto(debtPosition);
  }

  @Test
  void testInstallmentSynchronizeCancelActionThenOk(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    boolean massive = false;
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAction(InstallmentSynchronizeDTO.ActionEnum.A);

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(debtPositionOrigin);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionRepositoryMock.findByIupdOrgAndOrganizationId(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getOrganizationId())).thenReturn(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(installmentSynchronizeCancelServiceMock.syncInstallment(installmentSynchronizeDTO, debtPositionDTO,
      massive, accessToken, operatorExternalUserId)).thenReturn("workflowId");

    String result = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, massive, debtPositionOrigin, accessToken, operatorExternalUserId);

    assertEquals("workflowId", result);
  }

  @Test
  void testInstallmentSynchronizeCancelActionWhenDPNullThenOk(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    boolean massive = false;
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAction(InstallmentSynchronizeDTO.ActionEnum.A);

    Mockito.when(debtPositionRepositoryMock.findByIupdOrgAndOrganizationId(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(installmentSynchronizeCancelServiceMock.syncInstallment(installmentSynchronizeDTO, null,
      massive, accessToken, operatorExternalUserId)).thenReturn(null);

    String result = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, massive, debtPositionOrigin, accessToken, operatorExternalUserId);

    assertNull(result);
  }

  @Test
  void testInstallmentSynchronizeUpdateActionThenOk(){
    String accessToken = "accessToken";
    String operatorExternalUserId = "operatorExternalUserId";
    boolean massive = false;
    DebtPositionOrigin debtPositionOrigin = DebtPositionOrigin.ORDINARY_SIL;
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setAction(InstallmentSynchronizeDTO.ActionEnum.M);

    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setDebtPositionOrigin(debtPositionOrigin);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionRepositoryMock.findByIupdOrgAndOrganizationId(installmentSynchronizeDTO.getIupdOrg(), installmentSynchronizeDTO.getOrganizationId())).thenReturn(debtPosition);
    Mockito.when(debtPositionMapperMock.mapToDto(debtPosition)).thenReturn(debtPositionDTO);
    Mockito.when(installmentSynchronizeUpdateServiceMock.syncInstallment(installmentSynchronizeDTO, debtPositionDTO,
      massive, accessToken, operatorExternalUserId)).thenReturn("workflowId");

    String result = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, massive, debtPositionOrigin, accessToken, operatorExternalUserId);

    assertEquals("workflowId", result);
  }

}
