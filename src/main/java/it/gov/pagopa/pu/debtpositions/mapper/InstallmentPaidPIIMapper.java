package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOutcomeCode;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.enums.UniqueIdentifierType;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import org.springframework.stereotype.Service;

@Service
public class InstallmentPaidPIIMapper {

  private final PersonalDataService personalDataService;
  private final PersonaMapper personaMapper;

  public InstallmentPaidPIIMapper(PersonalDataService personalDataService, PersonaMapper personaMapper) {
    this.personalDataService = personalDataService;
    this.personaMapper = personaMapper;
  }

  public InstallmentPaidViewDTO mapToInstallmentPaidViewDTO(Float versionTrack, InstallmentPaidViewNoPII installmentPaidViewNoPII){
      switch (versionTrack.toString()){
        case "1.0" -> {
          return mapToInstallmentPaidViewDTO1(installmentPaidViewNoPII);
        }
        case "1.1" -> {
          return mapToInstallmentPaidViewDTO11(installmentPaidViewNoPII);
        }
        case "1.2" -> {
          return mapToInstallmentPaidViewDTO12(installmentPaidViewNoPII);
        }
        case "1.3" -> {
          return mapToInstallmentPaidViewDTO13(installmentPaidViewNoPII);
        }
        default -> throw new IllegalArgumentException("Unexpected versionTrack " + versionTrack);
      }
  }

  private InstallmentPaidViewDTO mapToInstallmentPaidViewDTO1(InstallmentPaidViewNoPII installmentPaidViewNoPII){
    InstallmentPIIDTO installmentPii = personalDataService.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class);

    return InstallmentPaidViewDTO.builder()
      .iuf(installmentPaidViewNoPII.getIuf())
      .numRigaFlusso(1)
      .codIud(installmentPaidViewNoPII.getIud())
      .codIuv(installmentPaidViewNoPII.getNoticeNumber())
      .identificativoDominio(installmentPaidViewNoPII.getOrgFiscalCode())
      .identificativoMessaggioRicevuta(installmentPaidViewNoPII.getPaymentReceiptId())
      .dataOraMessaggioRicevuta(installmentPaidViewNoPII.getPaymentDateTime())
      .riferimentoMessaggioRichiesta(installmentPaidViewNoPII.getPaymentReceiptId())
      .riferimentoDataRichiesta(installmentPaidViewNoPII.getPaymentDateTime())
      .tipoIdentificativoUnivoco(UniqueIdentifierType.B)
      .codiceIdentificativoUnivoco(installmentPaidViewNoPII.getIdPsp())
      .denominazioneAttestante(installmentPaidViewNoPII.getPspCompanyName())
      .enteBenefTipoIdentificativoUnivoco(PersonEntityType.G)
      .enteBenefCodiceIdentificativoUnivoco(installmentPaidViewNoPII.getOrgFiscalCode())
      .denominazioneBeneficiario(installmentPaidViewNoPII.getCompanyName())
      .soggPagTipoIdentificativoUnivoco(installmentPaidViewNoPII.getDebtorEntityType())
      .pagatore(installmentPii.getDebtor() != null ? personaMapper.mapToPersona(installmentPii.getDebtor()) : null)
      .codiceEsitoPagamento(PaymentOutcomeCode.PAYMENT_EXECUTED.getCode())
      .importoTotalePagato(Utilities.centsToEuro(installmentPaidViewNoPII.getPaymentAmountCents()))
      .identificativoUnivocoVersamento(installmentPaidViewNoPII.getCreditorReferenceId())
      .codiceContestoPagamento(installmentPaidViewNoPII.getPaymentReceiptId())
      .singoloImportoPagato(Utilities.centsToEuro(installmentPaidViewNoPII.getAmountCents()))
      .esitoSingoloPagamento("0")
      .dataEsitoSingoloPagamento(installmentPaidViewNoPII.getPaymentDateTime())
      .identificativoUnivocoRiscoss(installmentPaidViewNoPII.getPaymentReceiptId())
      .build();
  }

  private InstallmentPaidViewDTO mapToInstallmentPaidViewDTO11(InstallmentPaidViewNoPII installmentPaidViewNoPII){
    InstallmentPaidViewDTO.InstallmentPaidViewDTOBuilder<?, ?> installmentPaidViewDTOBuilder = mapToInstallmentPaidViewDTO1(installmentPaidViewNoPII).toBuilder();

    installmentPaidViewDTOBuilder
      .causaleVersamento(installmentPaidViewNoPII.getRemittanceInformation())
      .datiSpecificiRiscossione( "9/".concat(installmentPaidViewNoPII.getCategory()))
      .tipoDovuto(installmentPaidViewNoPII.getCode())
      .rt(null)  //TODO field rt depends on task https://pagopa.atlassian.net/browse/P4ADEV-2306
      .indiceDatiSingoloPagamento(installmentPaidViewNoPII.getTransferIndex())
      .numRtDatiPagDatiSingPagCommissioniApplicatePsp(Utilities.centsToEuro(installmentPaidViewNoPII.getFeeCents()));

    if (installmentPaidViewNoPII.getCode().equals("MARCA_BOLLO")){
      installmentPaidViewDTOBuilder.codRtDatiPagDatiSingPagAllegatoRicevutaTipo("BD");
      installmentPaidViewDTOBuilder.blbRtDatiPagDatiSingPagAllegatoRicevutaTest(null); //TODO field blbRtDatiPagDatiSingPagAllegatoRicevutaTest depends on task https://pagopa.atlassian.net/browse/P4ADEV-2306
    }

    return installmentPaidViewDTOBuilder.build();
  }

  private InstallmentPaidViewDTO mapToInstallmentPaidViewDTO12(InstallmentPaidViewNoPII installmentPaidViewNoPII){

    return mapToInstallmentPaidViewDTO11(installmentPaidViewNoPII).toBuilder()
      .bilancio(installmentPaidViewNoPII.getBalance())
      .build();
  }

  private InstallmentPaidViewDTO mapToInstallmentPaidViewDTO13(InstallmentPaidViewNoPII installmentPaidViewNoPII){

    return mapToInstallmentPaidViewDTO12(installmentPaidViewNoPII).toBuilder()
      .cod_fiscale_pa1(installmentPaidViewNoPII.getOrgFiscalCode())
      .de_nome_pa1(installmentPaidViewNoPII.getCompanyName())
      .cod_tassonomico_dovuto_pa1(installmentPaidViewNoPII.getCategory())
      .build();
  }
}
