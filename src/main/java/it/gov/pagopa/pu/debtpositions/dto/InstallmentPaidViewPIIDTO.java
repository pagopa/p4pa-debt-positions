package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPaidViewPIIDTO implements PIIDTO {

  private PersonDTO payer;
  private PersonDTO debtor;

}
