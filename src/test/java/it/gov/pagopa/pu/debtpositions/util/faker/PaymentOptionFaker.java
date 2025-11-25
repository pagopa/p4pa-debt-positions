package it.gov.pagopa.pu.debtpositions.util.faker;

import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildGeneratedIuvInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildMixedInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildSyncInstallmentDTO;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class PaymentOptionFaker {
  private static final LocalDate DATE = LocalDate.of(2099, 1, 2);
  private static final OffsetDateTime DATETIME = OffsetDateTime.of(DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);

  public static PaymentOption buildPaymentOption() {
    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(10L);
    paymentOption.setDebtPositionId(1L);
    paymentOption.setTotalAmountCents(2000L);
    paymentOption.setStatus(PaymentOptionStatus.TO_SYNC);
    paymentOption.setDescription("Payment description");
    paymentOption.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    paymentOption.setPaymentOptionIndex(1);
    paymentOption.setCreationDate(DATETIME.toLocalDateTime());
    paymentOption.setUpdateDate(DATETIME.toLocalDateTime());
    paymentOption.setUpdateOperatorExternalId("OPERATOREXTERNALUSERID");
    paymentOption.setUpdateTraceId("TRACEID");
    paymentOption.setInstallments(new TreeSet<>(new ArrayList<>(List.of(buildInstallmentNoPII()))));
    return paymentOption;
  }

  public static PaymentOptionDTO buildPaymentOptionDTO() {
    PaymentOptionDTO paymentOptionDTO = new PaymentOptionDTO();
    paymentOptionDTO.setPaymentOptionId(10L);
    paymentOptionDTO.setDebtPositionId(1L);
    paymentOptionDTO.setTotalAmountCents(2000L);
    paymentOptionDTO.setStatus(PaymentOptionStatus.UNPAID);
    paymentOptionDTO.setDescription("Payment description");
    paymentOptionDTO.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    paymentOptionDTO.setPaymentOptionIndex(1);
    paymentOptionDTO.setCreationDate(DATETIME);
    paymentOptionDTO.setUpdateDate(DATETIME);
    paymentOptionDTO.setUpdateOperatorExternalId("OPERATOREXTERNALUSERID");
    paymentOptionDTO.setUpdateTraceId("TRACEID");
    paymentOptionDTO.setInstallments(new ArrayList<>(List.of(buildInstallmentDTO())));
    return paymentOptionDTO;
  }

  public static PaymentOptionDTO buildGeneratedIuvPaymentOptionDTO() {
    PaymentOptionDTO paymentOptionDTO = new PaymentOptionDTO();
    paymentOptionDTO.setPaymentOptionId(10L);
    paymentOptionDTO.setDebtPositionId(1L);
    paymentOptionDTO.setTotalAmountCents(2000L);
    paymentOptionDTO.setStatus(PaymentOptionStatus.UNPAID);
    paymentOptionDTO.setDescription("Payment description");
    paymentOptionDTO.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    paymentOptionDTO.setPaymentOptionIndex(1);
    paymentOptionDTO.setInstallments(new ArrayList<>(List.of(buildGeneratedIuvInstallmentDTO())));
    return paymentOptionDTO;
  }

  public static PaymentOptionDTO buildSyncPaymentOptionDTO(){
    PaymentOptionDTO paymentOptionDTO = new PaymentOptionDTO();
    paymentOptionDTO.setDescription("Payment description");
    paymentOptionDTO.setStatus(PaymentOptionStatus.UNPAID);
    paymentOptionDTO.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    paymentOptionDTO.setPaymentOptionIndex(1);
    paymentOptionDTO.setInstallments(new ArrayList<>(List.of(buildSyncInstallmentDTO())));
    return paymentOptionDTO;
  }

  public static PaymentOption buildMixedPaymentOption() {
    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(10L);
    paymentOption.setDebtPositionId(1L);
    paymentOption.setTotalAmountCents(2000L);
    paymentOption.setStatus(PaymentOptionStatus.UNPAID);
    paymentOption.setDescription("Payment description");
    paymentOption.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    paymentOption.setPaymentOptionIndex(1);
    paymentOption.setCreationDate(DATETIME.toLocalDateTime());
    paymentOption.setUpdateDate(DATETIME.toLocalDateTime());
    paymentOption.setUpdateOperatorExternalId("OPERATOREXTERNALUSERID");
    paymentOption.setUpdateTraceId("TRACEID");
    paymentOption.setInstallments(new TreeSet<>(new ArrayList<>(List.of(buildMixedInstallmentNoPII()))));
    return paymentOption;
  }
}
