package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DataExportsApi;
import it.gov.pagopa.pu.debtpositions.dto.ExportPaidInstallmentsFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.LocalDateTimeIntervalFilter;
import it.gov.pagopa.pu.debtpositions.dto.OffsetDateTimeIntervalFilter;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidDateTimeIntervalException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidParamException;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataExportsControllerImpl implements DataExportsApi {

  private final InstallmentService installmentService;
  private final ReceiptService receiptService;
  private final Integer exportPaidMaxMonthsInterval;
  private final Integer exportArchivingMaxMonthsInterval;

  public DataExportsControllerImpl(InstallmentService installmentService, ReceiptService receiptService,
                                   @Value("${data-export.installment-paid-view.max-months-interval}") Integer exportPaidMaxMonthsInterval,
                                   @Value("${data-export.receipt-archiving-view.max-months-interval}") Integer exportArchivingMaxMonthsInterval) {
    this.installmentService = installmentService;
    this.receiptService = receiptService;
    this.exportPaidMaxMonthsInterval = exportPaidMaxMonthsInterval;
    this.exportArchivingMaxMonthsInterval = exportArchivingMaxMonthsInterval;
  }

  @Override
  public ResponseEntity<PagedInstallmentsPaidView> exportPaidInstallments(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateTimeFrom, OffsetDateTime paymentDateTimeTo, OffsetDateTime installmentUpdateDateTimeFrom, OffsetDateTime installmentUpdateDateTimeTo, Long debtPositionTypeOrgId, List<DebtPositionOrigin> debtPositionOrigins, Pageable pageable) {
    String invalidDateTimeIntervalErrorMessage = "[DATE_FILTER_INTERVAL_EXCEEDED] The date interval between %s and %s cannot exceed %d months";
    boolean hasPaymentDates = paymentDateTimeFrom != null && paymentDateTimeTo != null;
    boolean hasInstallmentDates = installmentUpdateDateTimeFrom != null && installmentUpdateDateTimeTo != null;

    if (hasPaymentDates == hasInstallmentDates) {
      throw new InvalidParamException(
        "[EXPORT_PAID_INSTALLMENTS_INVALID_DATE_FILTER_COMBINATION] You must provide only one of the following date ranges: either the payment date range (paymentDateTimeFrom and paymentDateTimeTo) or the installment update date range (installmentUpdateDateTimeFrom and installmentUpdateDateTimeTo). Providing both or neither is not allowed"
      );
    }

    if (hasPaymentDates && !Utilities.isValidIntervalBetweenOffsetDateTime(paymentDateTimeFrom, paymentDateTimeTo, ChronoUnit.MONTHS, exportPaidMaxMonthsInterval)) {
      throw new InvalidDateTimeIntervalException(invalidDateTimeIntervalErrorMessage.formatted(paymentDateTimeFrom, paymentDateTimeTo, exportPaidMaxMonthsInterval));
    }

    if (hasInstallmentDates && !Utilities.isValidIntervalBetweenOffsetDateTime(installmentUpdateDateTimeFrom, installmentUpdateDateTimeTo, ChronoUnit.MONTHS, exportPaidMaxMonthsInterval)) {
      throw new InvalidDateTimeIntervalException(invalidDateTimeIntervalErrorMessage.formatted(installmentUpdateDateTimeFrom, installmentUpdateDateTimeTo, exportPaidMaxMonthsInterval));
    }

    OffsetDateTimeIntervalFilter paymentDateTime = new OffsetDateTimeIntervalFilter(paymentDateTimeFrom, paymentDateTimeTo);
    LocalDateTimeIntervalFilter installmentUpdateDateTime = new LocalDateTimeIntervalFilter(Utilities.toLocalDateTime(installmentUpdateDateTimeFrom), Utilities.toLocalDateTime(installmentUpdateDateTimeTo));

    return ResponseEntity.ok(installmentService.getPagedInstallmentPaidView(
      ExportPaidInstallmentsFiltersDTO.builder()
        .organizationId(organizationId)
        .operatorExternalUserId(operatorExternalUserId)
        .paymentDateTime(paymentDateTime)
        .installmentUpdateDateTime(installmentUpdateDateTime)
        .debtPositionTypeOrgId(debtPositionTypeOrgId)
        .debtPositionOrigins(debtPositionOrigins)
        .build(),
     pageable));
  }

  @Override
  public ResponseEntity<PagedReceiptsArchivingView> exportArchivingReceipts(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Pageable pageable) {
    if (!Utilities.isValidIntervalBetweenOffsetDateTime(paymentDateFrom, paymentDateTo, ChronoUnit.MONTHS, exportArchivingMaxMonthsInterval)) {
      throw new InvalidDateTimeIntervalException("[DATE_FILTER_INTERVAL_EXCEEDED] The date interval between %s and %s cannot exceed %d months".formatted(paymentDateFrom, paymentDateTo, exportArchivingMaxMonthsInterval));
    }

    return ResponseEntity.ok(receiptService.getPagedReceiptArchivingView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, pageable));
  }
}
