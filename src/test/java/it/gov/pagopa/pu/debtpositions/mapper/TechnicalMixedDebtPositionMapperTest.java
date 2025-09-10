package it.gov.pagopa.pu.debtpositions.mapper;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransfer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TechnicalMixedDebtPositionMapperTest {

  private final TechnicalMixedDebtPositionMapper mapper = new TechnicalMixedDebtPositionMapper();

  @Test
  void whenToTechnicalMixedDebtPositionThenCorrectMapping() {
    DebtPosition debtPosition = buildDebtPosition();
    PaymentOption paymentOption = debtPosition.getPaymentOptions().getFirst();

    DebtPosition result = mapper.toTechnicalMixedDebtPosition(debtPosition, 1L,
      DebtPositionStatus.UNPAID);

    checkDebtPosition(result, debtPosition);

    PaymentOption resultPO = result.getPaymentOptions().getFirst();
    checkPaymentOption(resultPO, paymentOption);
  }

  @Test
  void whenToTechnicalMixedDPInstallmentThenCorrectMapping() {
    InstallmentNoPII installment = buildInstallmentNoPII();
    Transfer transfer = installment.getTransfers().getFirst();
    MixedDpAdditionalData mixedDpAdditionalData = MixedDpAdditionalData.builder()
      .transferIndex(1)
      .iud("IUD")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .build();

    InstallmentNoPII result = mapper.toTechnicalMixedDPInstallment(installment,
      InstallmentStatus.UNPAYABLE, mixedDpAdditionalData);

    checkInstallment(result, installment, mixedDpAdditionalData);

    Transfer resultTransfer = installment.getTransfers().getFirst();
    checkTransfer(transfer, resultTransfer);
  }

  @Test
  void givenMissingTransfersWhenToTechnicalMixedDPInstallmentThenCorrectMapping() {
    Transfer transfer = buildTransfer();
    transfer.setTransferIndex(999);

    InstallmentNoPII installment = buildInstallmentNoPII();
    installment.setTransfers(new TreeSet<>(new ArrayList<>(List.of(transfer))));

    MixedDpAdditionalData mixedDpAdditionalData = new MixedDpAdditionalData();
    mixedDpAdditionalData.setTransferIndex(1);

    Executable exec = () -> mapper.toTechnicalMixedDPInstallment(installment, InstallmentStatus.UNPAYABLE, mixedDpAdditionalData);

    assertThrows(RuntimeException.class, exec);
  }

  private static void checkDebtPosition(DebtPosition result,
    DebtPosition debtPosition) {
    assertNull(result.getDebtPositionId());
    assertEquals(debtPosition.getIupdOrg(), result.getIupdOrg());
    assertEquals(debtPosition.getDescription(), result.getDescription());
    assertEquals(DebtPositionStatus.UNPAID, result.getStatus());
    assertEquals(DebtPositionOrigin.SPONTANEOUS_MIXED,
      result.getDebtPositionOrigin());
    assertEquals(debtPosition.getOrganizationId(), result.getOrganizationId());
    assertEquals(1L, result.getDebtPositionTypeOrgId());
    assertEquals(debtPosition.getValidityDate(), result.getValidityDate());
    assertEquals(debtPosition.isFlagIuvVolatile(), result.isFlagIuvVolatile());
    assertEquals(debtPosition.isMultiDebtor(), result.isMultiDebtor());
    assertEquals(debtPosition.isFlagPuPagoPaPayment(),
      result.isFlagPuPagoPaPayment());
    assertEquals(1, result.getPaymentOptions().size());
  }

  private static void checkPaymentOption(PaymentOption resultPO,
    PaymentOption paymentOption) {
    assertNull(resultPO.getPaymentOptionId());
    assertNull(resultPO.getDebtPositionId());
    assertNull(resultPO.getInstallments());
    assertEquals(paymentOption.getTotalAmountCents(),
      resultPO.getTotalAmountCents());
    assertEquals(PaymentOptionStatus.UNPAYABLE, resultPO.getStatus());
    assertEquals(paymentOption.getDescription(), resultPO.getDescription());
    assertEquals(paymentOption.getPaymentOptionType(),
      resultPO.getPaymentOptionType());
    assertEquals(paymentOption.getPaymentOptionIndex(),
      resultPO.getPaymentOptionIndex());
  }

  private static void checkInstallment(InstallmentNoPII result,
    InstallmentNoPII installment, MixedDpAdditionalData mixedDpAdditionalData) {
    assertNull(result.getInstallmentId());
    assertNull(result.getPaymentOptionId());
    assertEquals(InstallmentStatus.UNPAYABLE, result.getStatus());
    assertNull(result.getSyncStatus());
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
    assertEquals(1, installment.getTransfers().size());
  }

  private static void checkTransfer(Transfer transfer, Transfer resultTransfer) {
    assertEquals(transfer.getTransferIndex(),
      resultTransfer.getTransferIndex());
    assertEquals(transfer.getOrgFiscalCode(),
      resultTransfer.getOrgFiscalCode());
    assertEquals(transfer.getOrgName(), resultTransfer.getOrgName());
    assertEquals(transfer.getAmountCents(), resultTransfer.getAmountCents());
    assertEquals(transfer.getRemittanceInformation(),
      resultTransfer.getRemittanceInformation());
    assertEquals(transfer.getStamp(), resultTransfer.getStamp());
    assertEquals(transfer.getIban(), resultTransfer.getIban());
    assertEquals(transfer.getPostalIban(), resultTransfer.getPostalIban());
    assertEquals(transfer.getCategory(), resultTransfer.getCategory());
    assertEquals(transfer.getMbdAttachment(),
      resultTransfer.getMbdAttachment());
  }
}
