package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.PIIDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InstallmentPaidViewDTO extends InstallmentPaidViewNoPIIDTO implements PIIDTO {

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
