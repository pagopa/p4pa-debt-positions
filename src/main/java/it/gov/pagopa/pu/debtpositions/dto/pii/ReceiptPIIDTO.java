package it.gov.pagopa.pu.debtpositions.dto.pii;

import it.gov.pagopa.pu.common.pii.dto.PIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptPIIDTO implements PIIDTO {
  private PersonDTO debtor;
  private PersonDTO payer;
}
