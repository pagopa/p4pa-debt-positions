package it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildSyncDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstallmentSynchronizeMapperTest {

  private InstallmentSynchronizeMapper installmentSynchronizeMapper;


  @BeforeEach
  void setUp() {
    installmentSynchronizeMapper = new InstallmentSynchronizeMapper();
  }

  @Test
  void testSyncMapper(){
    String stationId = "stationId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setBalance(null);
    DebtPositionDTO expectedDebtPositionDTO = buildSyncDebtPositionDTO();
    expectedDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setBalance(null);
    expectedDebtPositionDTO.setDebtPositionTypeOrgId(2L);
    expectedDebtPositionDTO.setStationId(stationId);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg, stationId);

    assertEquals(result, expectedDebtPositionDTO);
    reflectionEqualsByName(expectedDebtPositionDTO, result);
    verifyNotNullFields(result, true);
  }

  @Test
  void testSyncMapperDraft(){
    String stationId = "stationId";
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setDraft(Boolean.TRUE);
    DebtPositionDTO expectedDebtPositionDTO = buildSyncDebtPositionDTO();
    expectedDebtPositionDTO.setDebtPositionTypeOrgId(2L);
    expectedDebtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().setStatus(PaymentOptionStatus.DRAFT);
    expectedDebtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentStatus.DRAFT);
    expectedDebtPositionDTO.setStationId(stationId);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    DebtPositionDTO result = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg, stationId);

    assertEquals(expectedDebtPositionDTO, result);
    reflectionEqualsByName(expectedDebtPositionDTO, result);
    verifyNotNullFields(result, false);
  }

  private void verifyNotNullFields(DebtPositionDTO result, boolean isBalanceNull) {
    checkNotNullFields(result, "debtPositionId", "stationId", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");

    PaymentOptionDTO paymentOption = result.getPaymentOptions().getFirst();
    checkNotNullFields(paymentOption, "paymentOptionId", "debtPositionId", "totalAmountCents", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");

    InstallmentDTO installment = paymentOption.getInstallments().getFirst();
    String[] installmentIgnored = isBalanceNull ?
      new String[]{"installmentId", "paymentOptionId", "syncStatus", "generateNotice", "iupdPagopa", "iur", "iuf", "nav", "iun", "switchToExpired", "notificationFeeCents", "balance", "receiptId", "originalRemittanceInformation", "noPII", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId"} :
      new String[]{"installmentId", "paymentOptionId", "syncStatus", "generateNotice", "iupdPagopa", "iur", "iuf", "nav", "iun", "switchToExpired", "notificationFeeCents", "receiptId", "originalRemittanceInformation", "noPII", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId"};
    checkNotNullFields(installment, installmentIgnored);

    TransferDTO transferDTO = installment.getTransfers().getFirst();
    checkNotNullFields(transferDTO, "transferId", "installmentId", "stampType", "stampHashDocument", "stampProvincialResidence", "postalIban", "mbdAttachment", "flagOwner", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }
}
