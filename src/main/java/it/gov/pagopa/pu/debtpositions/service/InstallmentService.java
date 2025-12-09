package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.ExportPaidInstallmentsFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InstallmentService {
  List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin);

  InstallmentDetailDTO getInstallmentDetail(Long installmentId, String operatorExternalUserId);

  PagedInstallmentsPaidView getPagedInstallmentPaidView(ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO, Pageable pageable);

  WorkflowCreatedDTO updateInstallmentNotificationDate(UpdateInstallmentNotificationDateRequest updateInstallmentNotificationDateRequest, WfExecutionParameters wfExecutionParameters, String operatorExternalUserId, String accessToken);

  InstallmentDTO updateInstallmentNotificationFee(ActualizeAmountRequestDTO actualizeAmountRequestDTO, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId);

  List<InstallmentDTO> getInstallmentsByOrganizationIdAndReceiptId(Long organizationId, Long receiptId, List<DebtPositionOrigin> debtPositionOrigin);

  List<InstallmentDebtorDTO> getInstallmentsByIuvOrNav(String iuvOrNav, String debtorFiscalCode, Long organizationId);
}
