package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.dto.Persona;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOutcomeCode;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.enums.UniqueIdentifierType;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentPaidViewFaker;
import it.gov.pagopa.pu.debtpositions.util.faker.PersonaFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class InstallmentPaidPIIMapperTest {

  private InstallmentPaidPIIMapper installmentPaidPIIMapper;

  @Mock
  private PersonalDataService personalDataServiceMock;
  @Mock
  private PersonaMapper personaMapperMock;

  @BeforeEach
  void setUp() {
    installmentPaidPIIMapper = new InstallmentPaidPIIMapper(personalDataServiceMock, personaMapperMock);
  }

  @Test
  void givenValidInstallmentPaidViewNoPIIAndVersionTrack1_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    InstallmentPIIDTO installmentPIIDTO = InstallmentFaker.buildInstallmentPIIDTO();
    Persona persona = PersonaFaker.buildPersona();

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personaMapperMock.mapToPersona(installmentPIIDTO.getDebtor())).thenReturn(persona);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1F, installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assert1(installmentPaidViewNoPII, result, persona);

    TestUtils.checkNotNullFields(result,
      "versioneOggetto",
      "identificativoStazioneRichiedente",
      "codiceUnitOperAttestante",
      "denomUnitOperAttestante",
      "indirizzoAttestante",
      "civicoAttestante",
      "capAttestante",
      "localitaAttestante",
      "provinciaAttestante",
      "nazioneAttestante",
      "codiceUnitOperBeneficiario",
      "denomUnitOperBeneficiario",
      "indirizzoBeneficiario",
      "civicoBeneficiario",
      "capBeneficiario",
      "localitaBeneficiario",
      "provinciaBeneficiario",
      "nazioneBeneficiario",
      "soggVersTipoIdentificativoUnivoco",
      "soggVersCodiceIdentificativoUnivoco",
      "versante",
      "causaleVersamento",
      "datiSpecificiRiscossione",
      "tipoDovuto",
      "tipoFirma",
      "rt",
      "indiceDatiSingoloPagamento",
      "numRtDatiPagDatiSingPagCommissioniApplicatePsp",
      "codRtDatiPagDatiSingPagAllegatoRicevutaTipo",
      "blbRtDatiPagDatiSingPagAllegatoRicevutaTest",
      "bilancio",
      "cod_fiscale_pa1",
      "de_nome_pa1",
      "cod_tassonomico_dovuto_pa1"
      );
  }

  @Test
  void givenValidInstallmentPaidViewWithNullDebtorNoPIIAndVersionTrack1_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    installmentPaidViewNoPII.setCode("code");

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(new InstallmentPIIDTO());

    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1F, installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assertNull(result.getCodRtDatiPagDatiSingPagAllegatoRicevutaTipo());
    assertNull(result.getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest());
  }

  @Test
  void givenValidInstallmentPaidViewNoPIIAndVersionTrack1_1_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    InstallmentPIIDTO installmentPIIDTO = InstallmentFaker.buildInstallmentPIIDTO();

    Persona persona = PersonaFaker.buildPersona();

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personaMapperMock.mapToPersona(installmentPIIDTO.getDebtor())).thenReturn(persona);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1.1F, installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assert1(installmentPaidViewNoPII, result, persona);
    assert1_1(installmentPaidViewNoPII,result);

    TestUtils.checkNotNullFields(result,
      "versioneOggetto",
      "identificativoStazioneRichiedente",
      "codiceUnitOperAttestante",
      "denomUnitOperAttestante",
      "indirizzoAttestante",
      "civicoAttestante",
      "capAttestante",
      "localitaAttestante",
      "provinciaAttestante",
      "nazioneAttestante",
      "codiceUnitOperBeneficiario",
      "denomUnitOperBeneficiario",
      "indirizzoBeneficiario",
      "civicoBeneficiario",
      "capBeneficiario",
      "localitaBeneficiario",
      "provinciaBeneficiario",
      "nazioneBeneficiario",
      "soggVersTipoIdentificativoUnivoco",
      "soggVersCodiceIdentificativoUnivoco",
      "versante",
      "tipoFirma",
      "rt",
      "blbRtDatiPagDatiSingPagAllegatoRicevutaTest",
      "bilancio",
      "cod_fiscale_pa1",
      "de_nome_pa1",
      "cod_tassonomico_dovuto_pa1"
    );
  }

  @Test
  void givenValidInstallmentPaidViewNoPIIAndVersionTrack1_2_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    InstallmentPIIDTO installmentPIIDTO = InstallmentFaker.buildInstallmentPIIDTO();

    Persona persona = PersonaFaker.buildPersona();

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personaMapperMock.mapToPersona(installmentPIIDTO.getDebtor())).thenReturn(persona);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1.2F, installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assert1(installmentPaidViewNoPII, result, persona);
    assert1_1(installmentPaidViewNoPII,result);
    assert1_2(installmentPaidViewNoPII,result);

    TestUtils.checkNotNullFields(result,
      "versioneOggetto",
      "identificativoStazioneRichiedente",
      "codiceUnitOperAttestante",
      "denomUnitOperAttestante",
      "indirizzoAttestante",
      "civicoAttestante",
      "capAttestante",
      "localitaAttestante",
      "provinciaAttestante",
      "nazioneAttestante",
      "codiceUnitOperBeneficiario",
      "denomUnitOperBeneficiario",
      "indirizzoBeneficiario",
      "civicoBeneficiario",
      "capBeneficiario",
      "localitaBeneficiario",
      "provinciaBeneficiario",
      "nazioneBeneficiario",
      "soggVersTipoIdentificativoUnivoco",
      "soggVersCodiceIdentificativoUnivoco",
      "versante",
      "tipoFirma",
      "rt",
      "blbRtDatiPagDatiSingPagAllegatoRicevutaTest",
      "cod_fiscale_pa1",
      "de_nome_pa1",
      "cod_tassonomico_dovuto_pa1"
    );
  }

  @Test
  void givenValidInstallmentPaidViewNoPIIAndVersionTrack1_3_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    InstallmentPIIDTO installmentPIIDTO = InstallmentFaker.buildInstallmentPIIDTO();

    Persona persona = PersonaFaker.buildPersona();

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personaMapperMock.mapToPersona(installmentPIIDTO.getDebtor())).thenReturn(persona);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1.3F, installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assert1(installmentPaidViewNoPII, result, persona);
    assert1_1(installmentPaidViewNoPII,result);
    assert1_2(installmentPaidViewNoPII,result);
    assert1_3(installmentPaidViewNoPII, result);

    TestUtils.checkNotNullFields(result,
      "versioneOggetto",
      "identificativoStazioneRichiedente",
      "codiceUnitOperAttestante",
      "denomUnitOperAttestante",
      "indirizzoAttestante",
      "civicoAttestante",
      "capAttestante",
      "localitaAttestante",
      "provinciaAttestante",
      "nazioneAttestante",
      "codiceUnitOperBeneficiario",
      "denomUnitOperBeneficiario",
      "indirizzoBeneficiario",
      "civicoBeneficiario",
      "capBeneficiario",
      "localitaBeneficiario",
      "provinciaBeneficiario",
      "nazioneBeneficiario",
      "soggVersTipoIdentificativoUnivoco",
      "soggVersCodiceIdentificativoUnivoco",
      "versante",
      "tipoFirma",
      "rt",
      "blbRtDatiPagDatiSingPagAllegatoRicevutaTest"
    );
  }

  @Test
  void givenValidInstallmentPaidViewNoPIIAndWrongVersionTrack_whenMapToInstallmentPaidViewDTO_thenThrowException(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();

    //when
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1.4F, installmentPaidViewNoPII));
    //then
    assertEquals("Unexpected versionTrack 1.4", ex.getMessage());
  }

  private void assert1(InstallmentPaidViewNoPII installmentPaidViewNoPII, InstallmentPaidViewDTO result, Persona persona) {
    assertEquals(installmentPaidViewNoPII.getIuf(), result.getIuf());
    assertEquals(1, result.getNumRigaFlusso());
    assertEquals(installmentPaidViewNoPII.getIud(), result.getCodIud());
    assertEquals(installmentPaidViewNoPII.getNoticeNumber(), result.getCodIuv());
    assertNull(result.getVersioneOggetto());
    assertEquals(installmentPaidViewNoPII.getOrgFiscalCode(), result.getIdentificativoDominio());
    assertNull(result.getIdentificativoStazioneRichiedente());
    assertEquals(installmentPaidViewNoPII.getPaymentReceiptId(), result.getIdentificativoMessaggioRicevuta());
    assertEquals(installmentPaidViewNoPII.getPaymentDateTime(), result.getDataOraMessaggioRicevuta());
    assertEquals(installmentPaidViewNoPII.getPaymentReceiptId(), result.getRiferimentoMessaggioRichiesta());
    assertEquals(installmentPaidViewNoPII.getPaymentDateTime(), result.getRiferimentoDataRichiesta());
    assertEquals(UniqueIdentifierType.B, result.getTipoIdentificativoUnivoco());
    assertEquals(installmentPaidViewNoPII.getIdPsp(), result.getCodiceIdentificativoUnivoco());
    assertEquals(installmentPaidViewNoPII.getPspCompanyName(), result.getDenominazioneAttestante());
    assertNull(result.getCodiceUnitOperAttestante());
    assertNull(result.getDenomUnitOperAttestante());
    assertNull(result.getIndirizzoAttestante());
    assertNull(result.getCivicoAttestante());
    assertNull(result.getCapAttestante());
    assertNull(result.getLocalitaAttestante());
    assertNull(result.getProvinciaAttestante());
    assertNull(result.getNazioneAttestante());
    assertEquals(PersonEntityType.G, result.getEnteBenefTipoIdentificativoUnivoco());
    assertEquals(installmentPaidViewNoPII.getOrgFiscalCode(), result.getEnteBenefCodiceIdentificativoUnivoco());
    assertEquals(installmentPaidViewNoPII.getCompanyName(), result.getDenominazioneBeneficiario());
    assertNull(result.getCodiceUnitOperBeneficiario());
    assertNull(result.getDenomUnitOperBeneficiario());
    assertNull(result.getIndirizzoBeneficiario());
    assertNull(result.getCivicoBeneficiario());
    assertNull(result.getCapBeneficiario());
    assertNull(result.getLocalitaBeneficiario());
    assertNull(result.getProvinciaBeneficiario());
    assertNull(result.getNazioneBeneficiario());
    assertNull(result.getSoggVersTipoIdentificativoUnivoco());
    assertNull(result.getSoggVersCodiceIdentificativoUnivoco());
    assertNull(result.getVersante());
    assertEquals(installmentPaidViewNoPII.getDebtorEntityType(), result.getSoggPagTipoIdentificativoUnivoco());
    assertEquals(persona, result.getPagatore());
    assertEquals(PaymentOutcomeCode.PAYMENT_EXECUTED.getCode(), result.getCodiceEsitoPagamento());
    assertEquals(Utilities.centsToEuro(installmentPaidViewNoPII.getPaymentAmountCents()), result.getImportoTotalePagato());
    assertEquals(installmentPaidViewNoPII.getCreditorReferenceId(), result.getIdentificativoUnivocoVersamento());
    assertEquals(installmentPaidViewNoPII.getPaymentReceiptId(), result.getCodiceContestoPagamento());
    assertEquals(Utilities.centsToEuro(installmentPaidViewNoPII.getAmountCents()), result.getSingoloImportoPagato());
    assertEquals("0", result.getEsitoSingoloPagamento());
    assertEquals(installmentPaidViewNoPII.getPaymentDateTime(), result.getDataEsitoSingoloPagamento());
    assertEquals(installmentPaidViewNoPII.getPaymentReceiptId(), result.getIdentificativoUnivocoRiscoss());
  }

  private void assert1_1(InstallmentPaidViewNoPII installmentPaidViewNoPII, InstallmentPaidViewDTO result){
    assertEquals(installmentPaidViewNoPII.getRemittanceInformation(), result.getCausaleVersamento());
    assertEquals("9/category", result.getDatiSpecificiRiscossione());
    assertEquals(installmentPaidViewNoPII.getCode(), result.getTipoDovuto());
    assertNull(result.getTipoFirma());
    assertNull(result.getRt());
    assertEquals(installmentPaidViewNoPII.getTransferIndex(), result.getIndiceDatiSingoloPagamento());
    assertEquals(Utilities.centsToEuro(installmentPaidViewNoPII.getFeeCents()), result.getNumRtDatiPagDatiSingPagCommissioniApplicatePsp());
    assertEquals("BD", result.getCodRtDatiPagDatiSingPagAllegatoRicevutaTipo());
    assertNull(result.getBlbRtDatiPagDatiSingPagAllegatoRicevutaTest());
  }

  private void assert1_2(InstallmentPaidViewNoPII installmentPaidViewNoPII, InstallmentPaidViewDTO result){
    assertEquals(installmentPaidViewNoPII.getBalance(), result.getBilancio());
  }
  private void assert1_3(InstallmentPaidViewNoPII installmentPaidViewNoPII, InstallmentPaidViewDTO result){
    assertEquals(installmentPaidViewNoPII.getOrgFiscalCode(), result.getCod_fiscale_pa1());
    assertEquals(installmentPaidViewNoPII.getCompanyName(), result.getDe_nome_pa1());
    assertEquals(installmentPaidViewNoPII.getCategory(), result.getCod_tassonomico_dovuto_pa1());
  }

}
