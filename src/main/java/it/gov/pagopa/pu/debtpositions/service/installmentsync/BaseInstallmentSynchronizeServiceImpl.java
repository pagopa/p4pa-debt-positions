package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BaseInstallmentSynchronizeServiceImpl implements BaseInstallmentSynchronizeService {

  @Override
  public InstallmentDTO findInstallment(DebtPositionDTO debtPositionDTO, String iud, Integer paymentOptionIndex) {
    return debtPositionDTO.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionIndex().equals(paymentOptionIndex))
      .flatMap(po -> po.getInstallments().stream())
      .filter(inst -> inst.getIud().equals(iud))
      .findFirst()
      .orElseThrow(() -> new NotFoundException(
        String.format("The installment with iud %s in payment option with index %s not found", iud, paymentOptionIndex)));
  }

}
