package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOutcomeCode;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.enums.UniqueIdentifierType;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.Utilities;

import java.time.OffsetDateTime;

public class InstallmentPaidViewFaker {

  private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();

  public static InstallmentPaidViewNoPII mockInstanceInstallmentPaidViewNoPII(){
    return InstallmentPaidViewNoPII.builder()
      .installmentId(1L)
      .iuf("iuf")
      .iud("iud")
      .noticeNumber("123456")
      .orgFiscalCode("orgFiscalCode")
      .paymentReceiptId("paymentReceiptId")
      .paymentDateTime(OFFSET_DATE_TIME)
      .idPsp("1")
      .pspCompanyName("pspCompanyName")
      .debtorEntityType(PersonEntityType.G)
      .paymentAmountCents(125L)
      .creditorReferenceId("creditorReferenceId")
      .amountCents(100L)
      .remittanceInformation("info")
      .category("category")
      .code("MARCA_BOLLO")
      .transferIndex(1)
      .feeCents(10L)
      .balance("balance")
      .companyName("company")
      .personalDataId(123L)
      .build();
  }

  public static InstallmentPaidViewDTO mockInstanceInstallmentPaidViewDTO1(){
    return InstallmentPaidViewDTO.builder()
      .iuf("iuf")
      .numRigaFlusso(1)
      .codIud("iud")
      .codIuv("123456")
      .versioneOggetto(null)
      .identificativoDominio("orgFiscalCode")
      .identificativoStazioneRichiedente(null)
      .identificativoMessaggioRicevuta("paymentReceiptId")
      .dataOraMessaggioRicevuta(OFFSET_DATE_TIME)
      .riferimentoMessaggioRichiesta("paymentReceiptId")
      .riferimentoDataRichiesta(OFFSET_DATE_TIME)
      .tipoIdentificativoUnivoco(UniqueIdentifierType.B)
      .codiceIdentificativoUnivoco("1")
      .denominazioneAttestante("pspCompanyName")
      .codiceUnitOperAttestante(null)
      .denomUnitOperAttestante(null)
      .indirizzoAttestante(null)
      .civicoAttestante(null)
      .capAttestante(null)
      .localitaAttestante(null)
      .provinciaAttestante(null)
      .nazioneAttestante(null)
      .enteBenefTipoIdentificativoUnivoco(PersonEntityType.G)
      .enteBenefCodiceIdentificativoUnivoco("orgFiscalCode")
      .denominazioneBeneficiario("company")
      .codiceUnitOperBeneficiario(null)
      .denomUnitOperBeneficiario(null)
      .indirizzoBeneficiario(null)
      .civicoBeneficiario(null)
      .capBeneficiario(null)
      .localitaBeneficiario(null)
      .provinciaBeneficiario(null)
      .nazioneBeneficiario(null)
      .soggVersTipoIdentificativoUnivoco(null)
      .soggVersCodiceIdentificativoUnivoco(null)
      .versante(null)
      .soggPagTipoIdentificativoUnivoco(PersonEntityType.G)
      .pagatore(null)
      .codiceEsitoPagamento(PaymentOutcomeCode.PAYMENT_EXECUTED.getCode())
      .importoTotalePagato(Utilities.centsToEuro(125L))
      .identificativoUnivocoVersamento("creditorReferenceId")
      .codiceContestoPagamento("paymentReceiptId")
      .singoloImportoPagato(Utilities.centsToEuro(100L))
      .esitoSingoloPagamento("0")
      .dataEsitoSingoloPagamento(OFFSET_DATE_TIME)
      .identificativoUnivocoRiscoss("paymentReceiptId")
      .build();
  }

  public static InstallmentPaidViewDTO mockInstanceInstallmentPaidViewDTO1_1(){
    InstallmentPaidViewDTO installmentPaidViewDTO = mockInstanceInstallmentPaidViewDTO1();

    return installmentPaidViewDTO.toBuilder()
      .causaleVersamento("info")
      .datiSpecificiRiscossione("9/category")
      .tipoDovuto("code")
      .tipoFirma(null)
      .rt(null)
      .indiceDatiSingoloPagamento(1)
      .numRtDatiPagDatiSingPagCommissioniApplicatePsp(Utilities.centsToEuro(10L))
      .codRtDatiPagDatiSingPagAllegatoRicevutaTipo("BD")
      .blbRtDatiPagDatiSingPagAllegatoRicevutaTest(null)
      .build();
  }

  public static InstallmentPaidViewDTO mockInstanceInstallmentPaidViewDTO1_2(){
    InstallmentPaidViewDTO installmentPaidViewDTO = mockInstanceInstallmentPaidViewDTO1_1();

    return installmentPaidViewDTO.toBuilder()
      .bilancio("balance")
      .build();
  }

  public static InstallmentPaidViewDTO mockInstanceInstallmentPaidViewDTO1_3(){
    InstallmentPaidViewDTO installmentPaidViewDTO = mockInstanceInstallmentPaidViewDTO1_2();

    return installmentPaidViewDTO.toBuilder()
      .cod_fiscale_pa1("orgFiscalCode")
      .de_nome_pa1("companyName")
      .cod_tassonomico_dovuto_pa1("category")
      .build();
  }

}
