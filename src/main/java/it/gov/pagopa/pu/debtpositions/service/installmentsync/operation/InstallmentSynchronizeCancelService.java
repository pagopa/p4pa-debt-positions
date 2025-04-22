package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.BaseInstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionCancelInstallmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InstallmentSynchronizeCancelService extends BaseInstallmentSynchronizeService {

  private final DebtPositionCancelInstallmentService debtPositionCancelInstallmentService;

  public InstallmentSynchronizeCancelService(DebtPositionCancelInstallmentService debtPositionCancelInstallmentService) {
    this.debtPositionCancelInstallmentService = debtPositionCancelInstallmentService;
  }

  public String syncInstallment(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO debtPositionDTO,
                                WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {

    Pair<PaymentOptionDTO, InstallmentDTO> result = findInstallmentAndThrowException(debtPositionDTO, installmentSynchronizeDTO);
    InstallmentDTO installmentDTO = result.getRight();

    if(isInstallmentAlreadyElaborated(installmentDTO, installmentSynchronizeDTO)){ return null;}

    validateStatus(installmentDTO, installmentSynchronizeDTO);
    installmentDTO.setIngestionFlowFileId(installmentSynchronizeDTO.getIngestionFlowFileId());
    installmentDTO.setIngestionFlowFileLineNumber(installmentSynchronizeDTO.getIngestionFlowFileLineNumber());

    return debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, List.of(installmentDTO), wfExecutionParameters, accessToken, operatorExternalUserId);
  }
}
