package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DataExportsApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidDateTimeIntervalException;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@RestController
public class DataExportsControllerImpl implements DataExportsApi {
  private final InstallmentService installmentService;
  private final Integer maxMonthsInterval;

  public DataExportsControllerImpl(InstallmentService installmentService, @Value("${data-export.installment-paid-view.max-months-interval}") Integer maxMonthsInterval) {
    this.installmentService = installmentService;
    this.maxMonthsInterval = maxMonthsInterval;
  }

  @Override
  public ResponseEntity<PagedInstallmentsPaidView> exportPaidInstallments(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable) {
    if (!Utilities.isValidIntervalBetweenOffsetDateTime(paymentDateFrom, paymentDateTo, ChronoUnit.MONTHS, maxMonthsInterval)) {
      throw new InvalidDateTimeIntervalException("The date interval between %s and %s cannot exceed %d months".formatted(paymentDateFrom, paymentDateTo, maxMonthsInterval));
    }

    return ResponseEntity.ok(installmentService.getPagedInstallmentPaidView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, pageable));
  }

}
