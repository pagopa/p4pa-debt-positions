package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InstallmentSynchronizeInsertServiceImpl {

  public String syncInstallment(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO debtPositionDTO, DebtPositionOrigin debtPositionOrigin, Boolean massive, String accessToken, String operatorExternalUserId) {
    return null;
  }
}
