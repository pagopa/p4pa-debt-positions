package it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildSyncDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.service.BalanceFetchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeMapperTest {
  @Mock
  private BalanceFetchService balanceFetchServiceMock;

  private InstallmentSynchronizeMapper installmentSynchronizeMapper;


  @BeforeEach
  void setUp() {
    installmentSynchronizeMapper = new InstallmentSynchronizeMapper(balanceFetchServiceMock);
  }

  @Test
  void testSyncMapper(){
    String accessToken = "ACCESSTOKEN";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setBalance(null);
    DebtPositionDTO expectedDebtPositionDTO = buildSyncDebtPositionDTO();
    expectedDebtPositionDTO.setDebtPositionTypeOrgId(2L);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(balanceFetchServiceMock.getBalanceDefault(installmentSynchronizeDTO.getOrganizationId(), debtPositionTypeOrg, accessToken))
      .thenReturn("balance");

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg, accessToken);

    assertEquals(result, expectedDebtPositionDTO);
    reflectionEqualsByName(expectedDebtPositionDTO, result);
    checkNotNullFields(result, "debtPositionId", "flagIuvVolatile", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }

  @Test
  void testSyncMapperDraft(){
    String accessToken = "ACCESSTOKEN";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setDraft(Boolean.TRUE);
    DebtPositionDTO expectedDebtPositionDTO = buildSyncDebtPositionDTO();
    expectedDebtPositionDTO.setDebtPositionTypeOrgId(2L);
    expectedDebtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.DRAFT);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg, accessToken);

    assertEquals(expectedDebtPositionDTO, result);
    reflectionEqualsByName(expectedDebtPositionDTO, result);
    checkNotNullFields(result, "debtPositionId", "flagIuvVolatile", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }
}
