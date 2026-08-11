package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.exception.common.IllegalStateBusinessException;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.BalanceResolverService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.Constants.UNKNOWN_STATION_ID;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildMixedDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnicalMixedDebtPositionMapperTest {

  @Mock
  private BalanceResolverService balanceResolverServiceMock;
  @Mock
  private DebtPositionProcessorService debtPositionProcessorServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

  private TechnicalMixedDebtPositionMapper mapper;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    mapper = new TechnicalMixedDebtPositionMapper(
      balanceResolverServiceMock,
      debtPositionTypeOrgRepositoryMock,
      debtPositionProcessorServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      balanceResolverServiceMock,
      debtPositionTypeOrgRepositoryMock,
      debtPositionProcessorServiceMock
    );
  }

  @Test
  void whenToTechnicalMixedDebtPositionThenCorrectMapping() {
    DebtPosition debtPosition = buildMixedDebtPosition();
    PaymentOption paymentOption = debtPosition.getPaymentOptions().getFirst();
    InstallmentNoPII installment = paymentOption.getInstallments().getFirst();
    Transfer transfer = installment.getTransfers().getFirst();
    installment.setRemittanceInformation(transfer.getRemittanceInformation());

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    MixedDpAdditionalData mixedDpAdditionalData = MixedDpAdditionalData.builder()
      .transferIndex(1)
      .iud("IUD")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .build();

    Mockito.doNothing().when(debtPositionProcessorServiceMock)
      .updateAmounts(Mockito.any(DebtPosition.class));

    DebtPosition result = mapper.toTechnicalMixedDebtPosition(debtPosition, 1L,
      true, List.of(mixedDpAdditionalData), paymentDateTime, accessToken);

    checkDebtPosition(debtPosition, result, true);

    PaymentOption resultPO = result.getPaymentOptions().getFirst();
    checkPaymentOption(paymentOption, resultPO, true);

    InstallmentNoPII resultInstallment = resultPO.getInstallments().getFirst();
    checkInstallment(installment, resultInstallment, mixedDpAdditionalData, true);

    Transfer resultTransfer = installment.getTransfers().getFirst();
    checkTransfer(transfer, resultTransfer);
  }

  @Test
  void whenToTechnicalMixedDebtPositionPaidThenCorrectMapping() {
    DebtPosition debtPosition = buildMixedDebtPosition();
    PaymentOption paymentOption = debtPosition.getPaymentOptions().getFirst();
    InstallmentNoPII installment = paymentOption.getInstallments().getFirst();
    Transfer transfer = installment.getTransfers().getFirst();
    installment.setRemittanceInformation(transfer.getRemittanceInformation());
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    MixedDpAdditionalData mixedDpAdditionalData = MixedDpAdditionalData.builder()
      .transferIndex(1)
      .iud("IUD")
      .balance("balanceResolved")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .build();

    when(debtPositionTypeOrgRepositoryMock.findById(1L))
      .thenReturn(Optional.of(debtPositionTypeOrg));
    Mockito.doNothing().when(balanceResolverServiceMock)
      .updateBalanceResolvingAmount(Mockito.any(InstallmentNoPII.class),
        Mockito.eq(debtPosition.getOrganizationId()),
        Mockito.any(DebtPositionTypeOrg.class),
        Mockito.eq(paymentDateTime),
        Mockito.eq(accessToken));

    Mockito.doNothing().when(debtPositionProcessorServiceMock)
      .updateAmounts(Mockito.any(DebtPosition.class));

    DebtPosition result = mapper.toTechnicalMixedDebtPosition(debtPosition, 1L,
      false, List.of(mixedDpAdditionalData), paymentDateTime, accessToken);

    checkDebtPosition(debtPosition, result, false);

    PaymentOption resultPO = result.getPaymentOptions().getFirst();
    checkPaymentOption(paymentOption, resultPO, false);

    InstallmentNoPII resultInstallment = resultPO.getInstallments().getFirst();
    checkInstallment(installment, resultInstallment, mixedDpAdditionalData, false);

    Transfer resultTransfer = installment.getTransfers().getFirst();
    checkTransfer(transfer, resultTransfer);
  }

  @Test
  void givenMissingTransfersWhenToTechnicalMixedDebtPositionsThenCorrectMapping() {
    DebtPosition debtPosition = buildMixedDebtPosition();
    PaymentOption paymentOption = debtPosition.getPaymentOptions().getFirst();
    InstallmentNoPII installment = paymentOption.getInstallments().getFirst();
    Transfer transfer = installment.getTransfers().getFirst();
    transfer.setTransferIndex(999);

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    MixedDpAdditionalData mixedDpAdditionalData = MixedDpAdditionalData.builder()
      .transferIndex(1)
      .iud("IUD")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .build();
    Executable exec = () -> mapper.toTechnicalMixedDebtPosition(debtPosition, 1L, true, List.of(mixedDpAdditionalData), paymentDateTime, accessToken);

    assertThrows(IllegalStateBusinessException.class, exec);
  }

  @Test
  void givenDPMixedWithMultipleInstallmentsWhenToTechnicalMixedDebtPositionsThenCorrectMappingForPaymentOptionType() {
    DebtPosition debtPosition = buildMixedDebtPosition();
    PaymentOption expectedPO = debtPosition.getPaymentOptions().getFirst();

    OffsetDateTime paymentDateTime = OffsetDateTime.now();

    MixedDpAdditionalData mixedDpAdditionalDataEl1 = MixedDpAdditionalData.builder()
      .transferIndex(1)
      .iud("IUD")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .build();

    MixedDpAdditionalData mixedDpAdditionalDataEl2 = MixedDpAdditionalData.builder()
      .transferIndex(2)
      .iud("IUD")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .build();

    Mockito.doNothing().when(debtPositionProcessorServiceMock)
      .updateAmounts(Mockito.any(DebtPosition.class));

    DebtPosition result = mapper.toTechnicalMixedDebtPosition(debtPosition, 1L,
      true, List.of(mixedDpAdditionalDataEl1, mixedDpAdditionalDataEl2), paymentDateTime, accessToken);

    PaymentOption resultPO = result.getPaymentOptions().getFirst();

    TestUtils.checkNotNullFields(result, "paymentOptionId", "debtPositionId",
      "creationDate", "updateDate", "updateOperatorExternalId",
      "updateTraceId");

    assertEquals(expectedPO.getTotalAmountCents(), resultPO.getTotalAmountCents());
    assertEquals(PaymentOptionStatus.UNPAID, resultPO.getStatus());
    assertEquals(expectedPO.getDescription(), resultPO.getDescription());
    assertEquals(PaymentOptionType.INSTALLMENTS, resultPO.getPaymentOptionType());
    assertEquals(expectedPO.getPaymentOptionIndex(), resultPO.getPaymentOptionIndex());
    assertEquals(1, resultPO.getInstallments().size());
  }

  private static void checkDebtPosition(DebtPosition expected,
    DebtPosition result, boolean isUnpaid) {
    TestUtils.checkNotNullFields(result, "debtPositionId", "creationDate",
      "updateDate", "updateOperatorExternalId", "updateTraceId");

    assertEquals(expected.getIupdOrg(), result.getIupdOrg());
    assertEquals(expected.getDescription(), result.getDescription());
    if (isUnpaid) {
      assertEquals(DebtPositionStatus.UNPAID, result.getStatus());
    } else {
      assertEquals(DebtPositionStatus.PAID, result.getStatus());
    }
    assertEquals(DebtPositionOrigin.SPONTANEOUS_MIXED,
      result.getDebtPositionOrigin());
    assertEquals(expected.getOrganizationId(), result.getOrganizationId());
    assertEquals(1L, result.getDebtPositionTypeOrgId());
    assertEquals(expected.getValidityDate(), result.getValidityDate());
    assertEquals(expected.isMultiDebtor(), result.isMultiDebtor());
    assertEquals(expected.isFlagPuPagoPaPayment(),
      result.isFlagPuPagoPaPayment());
    assertEquals(UNKNOWN_STATION_ID, result.getStationId());
    assertEquals(1, result.getPaymentOptions().size());
  }

  private static void checkPaymentOption(PaymentOption expected,
    PaymentOption result, boolean isUnpaid) {
    TestUtils.checkNotNullFields(result, "paymentOptionId", "debtPositionId",
      "creationDate", "updateDate", "updateOperatorExternalId",
      "updateTraceId");

    assertEquals(expected.getTotalAmountCents(),
      result.getTotalAmountCents());
    if (isUnpaid) {
      assertEquals(PaymentOptionStatus.UNPAID, result.getStatus());
    } else {
      assertEquals(PaymentOptionStatus.PAID, result.getStatus());
    }
    assertEquals(expected.getDescription(), result.getDescription());
    assertEquals(expected.getPaymentOptionType(),
      result.getPaymentOptionType());
    assertEquals(expected.getPaymentOptionIndex(),
      result.getPaymentOptionIndex());
    assertEquals(1, result.getInstallments().size());
  }

  private static void checkInstallment(InstallmentNoPII installment,
    InstallmentNoPII result, MixedDpAdditionalData mixedDpAdditionalData, boolean isUnpaid) {
    TestUtils.checkNotNullFields(result, "installmentId", "paymentOptionId",
      "syncStatus", "creationDate", "updateDate", "updateOperatorExternalId",
      "updateTraceId");

    if(isUnpaid) {
      assertEquals(InstallmentStatus.UNPAID, result.getStatus());
      assertEquals(mixedDpAdditionalData.getBalance(), result.getBalance());
    } else {
      assertEquals(InstallmentStatus.PAID, result.getStatus());
      assertEquals("balanceResolved", result.getBalance());
    }
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
    assertEquals(installment.getTransfers().getFirst().getAmountCents(), result.getAmountCents());
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
    assertEquals(mixedDpAdditionalData.getLegacyPaymentMetadata(),
      result.getLegacyPaymentMetadata());
    assertEquals(1, result.getTransfers().size());
  }

  private static void checkTransfer(Transfer expected, Transfer result) {
    TestUtils.checkNotNullFields(result, "transferId", "creationDate", "updateDate",
      "updateOperatorExternalId", "updateTraceId", "flagOwner");

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
