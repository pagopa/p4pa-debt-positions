package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionInnerStatusAlignerServiceImpl extends DebtPositionStatusChecker implements DebtPositionInnerStatusAlignerService {

  public DebtPositionInnerStatusAlignerServiceImpl(DebtPositionRepository debtPositionRepository) {
    super(debtPositionRepository);
  }

  @Override
  public void updateDebtPositionStatus(BaseDebtPosition debtPosition) {
    updateEntityStatus(debtPosition);
  }
}
