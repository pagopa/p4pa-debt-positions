package it.gov.pagopa.pu.debtpositions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPaidViewPIIDTO implements PIIDTO {

  private Persona versante;
  private Persona pagatore;

}
