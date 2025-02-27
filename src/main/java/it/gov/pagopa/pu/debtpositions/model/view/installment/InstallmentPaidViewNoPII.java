package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "installment")
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPaidViewNoPII implements Serializable {

  @Id
  private Long installmentId;
  private String iuf;
  @NotNull
  private String codIud;
  @NotNull
  private String codIuv;
  @NotNull
  private String identificativoDominio;
  @NotNull
  private String identificativoMessaggioRicevuta;
  private OffsetDateTime dataOraMessaggioRicevuta;
  @NotNull
  private String riferimentoMessaggioRichiesta;
  private OffsetDateTime riferimentoDataRichiesta;
  @NotNull
  private String codiceIdentificativoUnivoco;
  @NotNull
  private String denominazioneAttestante;
  @NotNull
  private PersonEntityType soggPagTipoIdentificativoUnivoco;
  @NotNull
  private Long importoTotalePagato;
  @NotNull
  private String identificativoUnivocoVersamento;
  @NotNull
  private String codiceContestoPagamento;
  @NotNull
  private Long singoloImportoPagato;
  private OffsetDateTime dataEsitoSingoloPagamento;
  @NotNull
  private String identificativoUnivocoRiscoss;
  @Min(1)
  @Max(140)
  @NotNull
  private String causaleVersamento;
  @Min(5)
  @Max(140)
  @NotNull
  private String datiSpecificiRiscossione;
  @NotBlank
  @Min(1)
  @Max(1024)
  private String tipoDovuto;
  @NotNull
  private Integer indiceDatiSingoloPagamento;
  private Long numRtDatiPagDatiSingPagCommissioniApplicatePsp;
  private String bilancio;
  @NotNull
  private String cod_fiscale_pa1;
  private String de_nome_pa1;
  @NotNull
  private String cod_tassonomico_dovuto_pa1;
  @NotNull
  private Long personalDataId;
}
