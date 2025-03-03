package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.BaseInstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.apply.InstallmentSynchronizeApplierService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionUpdateInstallmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InstallmentSynchronizeUpdateService extends BaseInstallmentSynchronizeService {

  private final DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService;
  private final InstallmentSynchronizeApplierService installmentSynchronizeApplierService;

  public InstallmentSynchronizeUpdateService(DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService, InstallmentSynchronizeApplierService installmentSynchronizeApplierService) {
    this.debtPositionUpdateInstallmentService = debtPositionUpdateInstallmentService;
    this.installmentSynchronizeApplierService = installmentSynchronizeApplierService;
  }

  public String syncInstallment(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO storedDebtPosition,
                                Boolean massive, String accessToken, String operatorExternalUserId) {

    Pair<PaymentOptionDTO, InstallmentDTO> result = findInstallmentAndThrowException(storedDebtPosition, installmentSynchronizeDTO);

    if(isInstallmentAlreadyElaborated(result.getRight(), installmentSynchronizeDTO)){ return null;}

    validateStatus(result.getRight(), installmentSynchronizeDTO);

    InstallmentDTO installment2operate = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, storedDebtPosition, result.getLeft(), result.getRight(), accessToken);

    return debtPositionUpdateInstallmentService.updateInstallment(storedDebtPosition, List.of(installment2operate), massive, accessToken, operatorExternalUserId).getRight();
  }
}
