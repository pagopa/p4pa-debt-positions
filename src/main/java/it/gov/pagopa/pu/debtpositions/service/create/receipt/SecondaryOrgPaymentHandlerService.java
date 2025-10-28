package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecondaryOrgPaymentHandlerService {
  void handle(Optional<DebtPosition> primaryOrgDp, ReceiptWithAdditionalNodeDataDTO receipt, String accessToken) {
    // TODO
  }
}
