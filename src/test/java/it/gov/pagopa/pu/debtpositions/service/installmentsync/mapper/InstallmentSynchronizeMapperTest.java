package it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeCreateBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildSyncDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeMapperTest {
  @Mock
  private InstallmentSynchronizeCreateBalanceService installmentSynchronizeCreateBalanceServiceMock;

  private InstallmentSynchronizeMapper installmentSynchronizeMapper;

  @BeforeEach
  void setUp() {
    installmentSynchronizeMapper = new InstallmentSynchronizeMapper(installmentSynchronizeCreateBalanceServiceMock);
  }

  @Test
  void testSyncMapper(){
    String accessToken = "ACCESSTOKEN";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    DebtPositionDTO expectedDebtPositionDTO = buildSyncDebtPositionDTO();

    Mockito.when(installmentSynchronizeCreateBalanceServiceMock.createBalance(installmentSynchronizeDTO, accessToken))
      .thenReturn("balance");

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, 1L, accessToken);

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
    expectedDebtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.DRAFT);

    Mockito.when(installmentSynchronizeCreateBalanceServiceMock.createBalance(installmentSynchronizeDTO, accessToken))
      .thenReturn("balance");

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, 1L, accessToken);

    assertEquals(result, expectedDebtPositionDTO);
    reflectionEqualsByName(expectedDebtPositionDTO, result);
    checkNotNullFields(result, "debtPositionId", "flagIuvVolatile", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }
}
