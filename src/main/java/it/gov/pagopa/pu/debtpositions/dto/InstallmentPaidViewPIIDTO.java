package it.gov.pagopa.pu.debtpositions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPaidViewPIIDTO implements PIIDTO {

  private String anagraficaVersante;
  private String indirizzoVersante;
  private String civicoVersante;
  private String capVersante;
  private String localitaVersante;
  private String provinciaVersante;
  private String nazioneVersante;
  private String emailVersante;

  private String anagraficaPagatore;
  private String indirizzoPagatore;
  private String civicoPagatore;
  private String capPagatore;
  private String localitaPagatore;
  private String provinciaPagatore;
  private String nazionePagatore;
  private String emailPagatore;

}
