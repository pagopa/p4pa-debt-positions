package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;

import java.util.List;

public interface InstallmentService {
  List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav);

  InstallmentDetailDTO getInstallmentDetail(Long installmentId, String operatorExternalUserId);
}
