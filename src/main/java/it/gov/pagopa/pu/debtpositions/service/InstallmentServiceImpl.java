package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidDateTimeIntervalException;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentDetailPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentPaidViewPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class InstallmentServiceImpl implements InstallmentService {
  private final InstallmentPIIRepository installmentPIIRepository;
  private final InstallmentMapper installmentMapper;
  private final InstallmentDetailPIIViewRepository installmentDetailPIIViewRepository;
  private final InstallmentPaidViewPIIViewRepository installmentPaidViewPIIViewRepository;
  private final Integer maxMonthsInterval;

  public InstallmentServiceImpl(InstallmentPIIRepository installmentPIIRepository, InstallmentMapper installmentMapper, InstallmentDetailPIIViewRepository installmentDetailPIIViewRepository, InstallmentPaidViewPIIViewRepository installmentPaidViewPIIViewRepository, @Value("${installment-paid-view.max-months-interval}") Integer maxMonthsInterval) {
    this.installmentPIIRepository = installmentPIIRepository;
    this.installmentMapper = installmentMapper;
    this.installmentDetailPIIViewRepository = installmentDetailPIIViewRepository;
    this.installmentPaidViewPIIViewRepository = installmentPaidViewPIIViewRepository;
    this.maxMonthsInterval = maxMonthsInterval;
  }

  @Override
  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin) {
    return installmentPIIRepository.getByOrganizationIdAndNav(organizationId, nav, debtPositionOrigin).stream()
            .map(installmentMapper::mapToDto)
            .toList();
  }

  @Override
  public InstallmentDetailDTO getInstallmentDetail(Long installmentId, String operatorExternalUserId) {
    return installmentDetailPIIViewRepository.getInstallmentDetail(installmentId, operatorExternalUserId);
  }

  @Override
  public PagedInstallmentsPaidView getPagedInstallmentPaidView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable) {
    long intervalOffsetDateTime = Utilities.calculateIntervalBetweenOffsetDateTime(paymentDateFrom, paymentDateTo, ChronoUnit.MONTHS);

    if(intervalOffsetDateTime > maxMonthsInterval){
      throw new InvalidDateTimeIntervalException("The date interval between %s and %s cannot exceed %d months. The provided interval is %d months".formatted(paymentDateFrom, paymentDateTo, maxMonthsInterval, intervalOffsetDateTime));
    }
    return installmentPaidViewPIIViewRepository.getPagedInstallmentPaidView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, pageable);
  }
}
