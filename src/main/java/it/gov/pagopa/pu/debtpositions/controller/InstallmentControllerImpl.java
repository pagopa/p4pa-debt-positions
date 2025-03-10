package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.InstallmentApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
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
import java.util.List;

@RestController
public class InstallmentControllerImpl implements InstallmentApi {
  private final InstallmentService installmentService;
  private final Integer maxMonthsInterval;

  public InstallmentControllerImpl(InstallmentService installmentService, @Value("${data-export.installment-paid-view.max-months-interval}") Integer maxMonthsInterval) {
    this.installmentService = installmentService;
    this.maxMonthsInterval = maxMonthsInterval;
  }

  @Override
  public ResponseEntity<List<InstallmentDTO>> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin) {
    return ResponseEntity.ok(installmentService.getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOrigin));
  }

  @Override
  public ResponseEntity<InstallmentDetailDTO> getInstallmentDetail(Long installmentId, String operatorExternalUserId) {
    return ResponseEntity.ok(installmentService.getInstallmentDetail(installmentId, operatorExternalUserId));
  }

  @Override
  public ResponseEntity<PagedInstallmentsPaidView> exportPaidInstallments(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable) {
    if (!Utilities.isValidIntervalBetweenOffsetDateTime(paymentDateFrom, paymentDateTo, ChronoUnit.MONTHS, maxMonthsInterval)) {
      throw new InvalidDateTimeIntervalException("The date interval between %s and %s cannot exceed %d months".formatted(paymentDateFrom, paymentDateTo, maxMonthsInterval));
    }

    return ResponseEntity.ok(installmentService.getPagedInstallmentPaidView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, pageable));
  }
}
