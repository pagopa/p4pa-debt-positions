package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface InstallmentService {
  List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin);

  InstallmentDetailDTO getInstallmentDetail(Long installmentId, String operatorExternalUserId);

  PagedInstallmentsPaidView getPagedInstallmentPaidView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable);

  String updateInstallmentNotificationDate(UpdateInstallmentNotificationDateRequest updateInstallmentNotificationDateRequest, WfExecutionParameters wfExecutionParameters, String operatorExternalUserId,  String accessToken);

  InstallmentDTO updateInstallmentNotificationFee(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin, long newFeeCents, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId);
}
