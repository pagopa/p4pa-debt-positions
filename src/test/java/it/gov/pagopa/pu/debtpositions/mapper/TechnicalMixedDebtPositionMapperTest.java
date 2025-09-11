package it.gov.pagopa.pu.debtpositions.mapper;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildMixedDebtPosition;
import static org.junit.jupiter.api.Assertions.assertEquals;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TechnicalMixedDebtPositionMapperTest {

  private final TechnicalMixedDebtPositionMapper mapper = new TechnicalMixedDebtPositionMapper();

  @Test
  void whenToTechnicalMixedDebtPositionThenCorrectMapping() {
    DebtPosition debtPosition = buildMixedDebtPosition();
    PaymentOption paymentOption = debtPosition.getPaymentOptions().getFirst();
    InstallmentNoPII installment = paymentOption.getInstallments().getFirst();
    Transfer transfer = installment.getTransfers().getFirst();

    MixedDpAdditionalData mixedDpAdditionalData = MixedDpAdditionalData.builder()
      .transferIndex(1)
      .iud("IUD")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .build();
    DebtPosition result = mapper.toTechnicalMixedDebtPosition(debtPosition, 1L,
      true, List.of(mixedDpAdditionalData));

    checkDebtPosition(debtPosition, result);

    PaymentOption resultPO = result.getPaymentOptions().getFirst();
    checkPaymentOption(paymentOption, resultPO);

    InstallmentNoPII resultInstallment = resultPO.getInstallments().getFirst();
    checkInstallment(installment, resultInstallment, mixedDpAdditionalData);

    Transfer resultTransfer = installment.getTransfers().getFirst();
    checkTransfer(transfer, resultTransfer);
  }

  private static void checkDebtPosition(DebtPosition expected,
    DebtPosition result) {
    TestUtils.checkNotNullFields(result, "debtPositionId", "creationDate",
      "updateDate", "updateOperatorExternalId", "updateTraceId");

    assertEquals(expected.getIupdOrg(), result.getIupdOrg());
    assertEquals(expected.getDescription(), result.getDescription());
    assertEquals(DebtPositionStatus.UNPAID, result.getStatus());
    assertEquals(DebtPositionOrigin.SPONTANEOUS_MIXED,
      result.getDebtPositionOrigin());
    assertEquals(expected.getOrganizationId(), result.getOrganizationId());
    assertEquals(1L, result.getDebtPositionTypeOrgId());
    assertEquals(expected.getValidityDate(), result.getValidityDate());
    assertEquals(expected.isFlagIuvVolatile(), result.isFlagIuvVolatile());
    assertEquals(expected.isMultiDebtor(), result.isMultiDebtor());
    assertEquals(expected.isFlagPuPagoPaPayment(),
      result.isFlagPuPagoPaPayment());
    assertEquals(1, result.getPaymentOptions().size());
  }

  private static void checkPaymentOption(PaymentOption expected,
    PaymentOption result) {
    TestUtils.checkNotNullFields(result, "paymentOptionId", "debtPositionId",
      "creationDate", "updateDate", "updateOperatorExternalId",
      "updateTraceId");

    assertEquals(expected.getTotalAmountCents(),
      result.getTotalAmountCents());
    assertEquals(PaymentOptionStatus.UNPAYABLE, result.getStatus());
    assertEquals(expected.getDescription(), result.getDescription());
    assertEquals(expected.getPaymentOptionType(),
      result.getPaymentOptionType());
    assertEquals(expected.getPaymentOptionIndex(),
      result.getPaymentOptionIndex());
    assertEquals(1, result.getInstallments().size());
  }

  private static void checkInstallment(InstallmentNoPII installment,
    InstallmentNoPII result, MixedDpAdditionalData mixedDpAdditionalData) {
    TestUtils.checkNotNullFields(result, "installmentId", "paymentOptionId",
      "syncStatus", "creationDate", "updateDate", "updateOperatorExternalId",
      "updateTraceId");

    assertEquals(InstallmentStatus.UNPAYABLE, result.getStatus());
    assertEquals(installment.getIupdPagopa(), result.getIupdPagopa());
    assertEquals(installment.isGenerateNotice(), result.isGenerateNotice());
    assertEquals(installment.getIuv(), result.getIuv());
    assertEquals(installment.getIur(), result.getIur());
    assertEquals(installment.getIuf(), result.getIuf());
    assertEquals(installment.getNav(), result.getNav());
    assertEquals(installment.getIun(), result.getIun());
    assertEquals(installment.getDueDate(), result.getDueDate());
    assertEquals(installment.isSwitchToExpired(), result.isSwitchToExpired());
    assertEquals(installment.getNotificationFeeCents(),
      result.getNotificationFeeCents());
    assertEquals(installment.getAmountCents(), result.getAmountCents());
    assertEquals(installment.getRemittanceInformation(),
      result.getRemittanceInformation());
    assertEquals(installment.getPersonalDataId(), result.getPersonalDataId());
    assertEquals(installment.getDebtorEntityType(),
      result.getDebtorEntityType());
    assertEquals(installment.getDebtorFiscalCodeHash(),
      result.getDebtorFiscalCodeHash());
    assertEquals(installment.getNotificationDate(),
      result.getNotificationDate());
    assertEquals(installment.getIngestionFlowFileId(),
      result.getIngestionFlowFileId());
    assertEquals(installment.getIngestionFlowFileLineNumber(),
      result.getIngestionFlowFileLineNumber());
    assertEquals(installment.getIngestionFlowFileAction(),
      result.getIngestionFlowFileAction());
    assertEquals(installment.getSourceFlowName(), result.getSourceFlowName());
    assertEquals(installment.getReceiptId(), result.getReceiptId());
    assertEquals(mixedDpAdditionalData.getIud(), result.getIud());
    assertEquals(mixedDpAdditionalData.getBalance(), result.getBalance());
    assertEquals(mixedDpAdditionalData.getLegacyPaymentMetadata(),
      result.getLegacyPaymentMetadata());
    assertEquals(1, result.getTransfers().size());
  }

  private static void checkTransfer(Transfer expected, Transfer result) {
    TestUtils.checkNotNullFields(result, "creationDate", "updateDate",
      "updateOperatorExternalId", "updateTraceId");

    assertEquals(expected.getTransferIndex(),
      result.getTransferIndex());
    assertEquals(expected.getOrgFiscalCode(),
      result.getOrgFiscalCode());
    assertEquals(expected.getOrgName(), result.getOrgName());
    assertEquals(expected.getAmountCents(), result.getAmountCents());
    assertEquals(expected.getRemittanceInformation(),
      result.getRemittanceInformation());
    assertEquals(expected.getStamp(), result.getStamp());
    assertEquals(expected.getIban(), result.getIban());
    assertEquals(expected.getPostalIban(), result.getPostalIban());
    assertEquals(expected.getCategory(), result.getCategory());
    assertEquals(expected.getMbdAttachment(),
      result.getMbdAttachment());
  }
}
