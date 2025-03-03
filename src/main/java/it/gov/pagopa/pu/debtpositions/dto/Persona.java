package it.gov.pagopa.pu.debtpositions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Persona {

  private String codiceIdentificativoUnivoco;
  private String anagrafica;
  private String indirizzo;
  private String civico;
  private String cap;
  private String localita;
  private String provincia;
  private String nazione;
  private String email;
}
