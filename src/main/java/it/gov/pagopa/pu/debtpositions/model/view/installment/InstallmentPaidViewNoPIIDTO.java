package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.enums.PaymentOutcomeCode;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.enums.SignatureType;
import it.gov.pagopa.pu.debtpositions.enums.UniqueIdentifierType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class InstallmentPaidViewNoPIIDTO {

  private String iuf;
  private Integer numRigaFlusso;
  @NotNull
  private String codIud;
  @NotNull
  private String codIuv;
  private Integer versioneOggetto;
  private String identificativoDominio;
  private String identificativoStazioneRichiedente;
  @NotNull
  private String identificativoMessaggioRicevuta;
  private OffsetDateTime dataOraMessaggioRicevuta;
  private String riferimentoMessaggioRichiesta;
  private OffsetDateTime riferimentoDataRichiesta;
  private UniqueIdentifierType tipoIdentificativoUnivoco;
  @NotNull
  private String codiceIdentificativoUnivoco;
  @NotNull
  private String denominazioneAttestante;
  private String codiceUnitOperAttestante;
  private String denomUnitOperAttestante;
  private String indirizzoAttestante;
  private String civicoAttestante;
  private String capAttestante;
  private String localitaAttestante;
  private String provinciaAttestante;
  private String nazioneAttestante;
  private PersonEntityType enteBenefTipoIdentificativoUnivoco;
  private String enteBenefCodiceIdentificativoUnivoco;
  private String denominazioneBeneficiario;
  private String codiceUnitOperBeneficiario;
  private String denomUnitOperBeneficiario;
  private String indirizzoBeneficiario;
  private String civicoBeneficiario;
  private String capBeneficiario;
  private String localitaBeneficiario;
  private String provinciaBeneficiario;
  private String nazioneBeneficiario;
  @NotNull
  private PersonEntityType soggVersTipoIdentificativoUnivoco;
  private PersonEntityType soggVersCodiceIdentificativoUnivoco;
  private PersonEntityType soggPagTipoIdentificativoUnivoco;
  private PaymentOutcomeCode codiceEsitoPagamento;
  @NotNull
  private Double importoTotalePagato;
  @NotNull
  private String identificativoUnivocoVersamento;
  @NotNull
  private String codiceContestoPagamento;
  @NotNull
  private Double singoloImportoPagato;
  private String esitoSingoloPagamento;
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

  @Min(1)
  @Max(15)
  private SignatureType tipoFirma;

  private String rt;
  @NotNull
  private Integer indiceDatiSingoloPagamento;
  @NotNull
  private Double numRtDatiPagDatiSingPagCommissioniApplicatePsp;
  private String codRtDatiPagDatiSingPagAllegatoRicevutaTipo;
  private String blbRtDatiPagDatiSingPagAllegatoRicevutaTest;
  private String bilancio;
  private String cod_fiscale_pa1;
  private String de_nome_pa1;
  @NotNull
  private String cod_tassonomico_dovuto_pa1;

}
