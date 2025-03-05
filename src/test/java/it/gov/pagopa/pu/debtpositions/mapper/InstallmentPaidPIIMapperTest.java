package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOutcomeCode;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.enums.UniqueIdentifierType;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentPaidViewFaker;
import it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker;
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
  private PersonMapper personMapperMock;

  @BeforeEach
  void setUp() {
    installmentPaidPIIMapper = new InstallmentPaidPIIMapper(personalDataServiceMock, personMapperMock);
  }

  @Test
  void givenValidInstallmentPaidViewNoPIIAndVersionTrack1_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    InstallmentPIIDTO installmentPIIDTO = InstallmentFaker.buildInstallmentPIIDTO();
    PersonDTO personDTO = PersonFaker.buildPersonDTO();

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personMapperMock.mapToDto(installmentPIIDTO.getDebtor())).thenReturn(personDTO);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1F, installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assert1(installmentPaidViewNoPII, result, personDTO);

    TestUtils.checkNotNullFields(result,
      "objectVersion",
      "requestingStationIdentifier",
      "attestingUnitOperCode",
      "attestingUnitOperName",
      "attestingAddress",
      "attestingStreetNumber",
      "attestingPostalCode",
      "attestingCity",
      "attestingProvince",
      "attestingCountry",
      "beneficiaryUnitOperCode",
      "beneficiaryUnitOperName",
      "beneficiaryAddress",
      "beneficiaryStreetNumber",
      "beneficiaryPostalCode",
      "beneficiaryCity",
      "beneficiaryProvince",
      "beneficiaryCountry",
      "payerEntityType",
      "payerUniqueIdentifierCode",
      "payer",
      "paymentReason",
      "collectionSpecificData",
      "dueType",
      "signatureType",
      "rt",
      "singlePaymentDataIndex",
      "pspAppliedFeesCents",
      "receiptAttachmentType",
      "receiptAttachmentTest",
      "balance",
      "orgFiscalCode",
      "orgName",
      "dueTaxonomicCode"
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
    assertNull(result.getReceiptAttachmentType());
    assertNull(result.getReceiptAttachmentTest());
  }

  @Test
  void givenValidInstallmentPaidViewNoPIIAndVersionTrack1_1_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    InstallmentPIIDTO installmentPIIDTO = InstallmentFaker.buildInstallmentPIIDTO();

    PersonDTO personDTO = PersonFaker.buildPersonDTO();

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personMapperMock.mapToDto(installmentPIIDTO.getDebtor())).thenReturn(personDTO);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1.1F, installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assert1(installmentPaidViewNoPII, result, personDTO);
    assert1_1(installmentPaidViewNoPII,result);

    TestUtils.checkNotNullFields(result,
      "objectVersion",
      "requestingStationIdentifier",
      "attestingUnitOperCode",
      "attestingUnitOperName",
      "attestingAddress",
      "attestingStreetNumber",
      "attestingPostalCode",
      "attestingCity",
      "attestingProvince",
      "attestingCountry",
      "beneficiaryUnitOperCode",
      "beneficiaryUnitOperName",
      "beneficiaryAddress",
      "beneficiaryStreetNumber",
      "beneficiaryPostalCode",
      "beneficiaryCity",
      "beneficiaryProvince",
      "beneficiaryCountry",
      "payerEntityType",
      "payerUniqueIdentifierCode",
      "payer",
      "signatureType",
      "rt",
      "receiptAttachmentTest",
      "balance",
      "orgFiscalCode",
      "orgName",
      "dueTaxonomicCode"
    );
  }

  @Test
  void givenValidInstallmentPaidViewNoPIIAndVersionTrack1_2_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    InstallmentPIIDTO installmentPIIDTO = InstallmentFaker.buildInstallmentPIIDTO();

    PersonDTO personDTO = PersonFaker.buildPersonDTO();

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personMapperMock.mapToDto(installmentPIIDTO.getDebtor())).thenReturn(personDTO);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1.2F, installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assert1(installmentPaidViewNoPII, result, personDTO);
    assert1_1(installmentPaidViewNoPII,result);
    assert1_2(installmentPaidViewNoPII,result);

    TestUtils.checkNotNullFields(result,
      "objectVersion",
      "requestingStationIdentifier",
      "attestingUnitOperCode",
      "attestingUnitOperName",
      "attestingAddress",
      "attestingStreetNumber",
      "attestingPostalCode",
      "attestingCity",
      "attestingProvince",
      "attestingCountry",
      "beneficiaryUnitOperCode",
      "beneficiaryUnitOperName",
      "beneficiaryAddress",
      "beneficiaryStreetNumber",
      "beneficiaryPostalCode",
      "beneficiaryCity",
      "beneficiaryProvince",
      "beneficiaryCountry",
      "payerEntityType",
      "payerUniqueIdentifierCode",
      "payer",
      "signatureType",
      "rt",
      "receiptAttachmentTest",
      "orgFiscalCode",
      "orgName",
      "dueTaxonomicCode"
    );
  }

  @Test
  void givenValidInstallmentPaidViewNoPIIAndVersionTrack1_3_whenMapToInstallmentPaidViewDTO_thenReturnInstallmentPaidViewDTO(){
    //given
    InstallmentPaidViewNoPII installmentPaidViewNoPII = InstallmentPaidViewFaker.mockInstanceInstallmentPaidViewNoPII();
    InstallmentPIIDTO installmentPIIDTO = InstallmentFaker.buildInstallmentPIIDTO();

    PersonDTO personDTO = PersonFaker.buildPersonDTO();

    Mockito.when(personalDataServiceMock.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);
    Mockito.when(personMapperMock.mapToDto(installmentPIIDTO.getDebtor())).thenReturn(personDTO);
    //when
    InstallmentPaidViewDTO result = installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(1.3F, installmentPaidViewNoPII);

    //then
    assertNotNull(result);
    assert1(installmentPaidViewNoPII, result, personDTO);
    assert1_1(installmentPaidViewNoPII,result);
    assert1_2(installmentPaidViewNoPII,result);
    assert1_3(installmentPaidViewNoPII, result);

    TestUtils.checkNotNullFields(result,
      "objectVersion",
      "requestingStationIdentifier",
      "attestingUnitOperCode",
      "attestingUnitOperName",
      "attestingAddress",
      "attestingStreetNumber",
      "attestingPostalCode",
      "attestingCity",
      "attestingProvince",
      "attestingCountry",
      "beneficiaryUnitOperCode",
      "beneficiaryUnitOperName",
      "beneficiaryAddress",
      "beneficiaryStreetNumber",
      "beneficiaryPostalCode",
      "beneficiaryCity",
      "beneficiaryProvince",
      "beneficiaryCountry",
      "payerEntityType",
      "payerUniqueIdentifierCode",
      "payer",
      "signatureType",
      "rt",
      "receiptAttachmentTest"
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

  private void assert1(InstallmentPaidViewNoPII installmentPaidViewNoPII, InstallmentPaidViewDTO result, PersonDTO person) {
    assertEquals(installmentPaidViewNoPII.getIuf(), result.getIuf());
    assertEquals(1, result.getFlowRowNumber());
    assertEquals(installmentPaidViewNoPII.getIud(), result.getIud());
    assertEquals(installmentPaidViewNoPII.getNoticeNumber(), result.getIuv());
    assertNull(result.getObjectVersion());
    assertEquals(installmentPaidViewNoPII.getOrgFiscalCode(), result.getDomainIdentifier());
    assertNull(result.getRequestingStationIdentifier());
    assertEquals(installmentPaidViewNoPII.getPaymentReceiptId(), result.getReceiptMessageIdentifier());
    assertEquals(installmentPaidViewNoPII.getPaymentDateTime(), result.getReceiptMessageDateTime());
    assertEquals(installmentPaidViewNoPII.getPaymentReceiptId(), result.getRequestMessageReference());
    assertEquals(installmentPaidViewNoPII.getPaymentDateTime(), result.getRequestDateTimeReference());
    assertEquals(UniqueIdentifierType.B, result.getUniqueIdentifierType());
    assertEquals(installmentPaidViewNoPII.getIdPsp(), result.getUniqueIdentifierCode());
    assertEquals(installmentPaidViewNoPII.getPspCompanyName(), result.getAttestingName());
    assertNull(result.getAttestingUnitOperCode());
    assertNull(result.getAttestingUnitOperName());
    assertNull(result.getAttestingAddress());
    assertNull(result.getAttestingStreetNumber());
    assertNull(result.getAttestingPostalCode());
    assertNull(result.getAttestingCity());
    assertNull(result.getAttestingProvince());
    assertNull(result.getAttestingCountry());
    assertEquals(PersonEntityType.G, result.getBeneficiaryEntityType());
    assertEquals(installmentPaidViewNoPII.getOrgFiscalCode(), result.getBeneficiaryUniqueIdentifierCode());
    assertEquals(installmentPaidViewNoPII.getCompanyName(), result.getBeneficiaryName());
    assertNull(result.getBeneficiaryUnitOperCode());
    assertNull(result.getBeneficiaryUnitOperName());
    assertNull(result.getBeneficiaryAddress());
    assertNull(result.getBeneficiaryStreetNumber());
    assertNull(result.getBeneficiaryPostalCode());
    assertNull(result.getBeneficiaryCity());
    assertNull(result.getBeneficiaryProvince());
    assertNull(result.getBeneficiaryCountry());
    assertNull(result.getPayerEntityType());
    assertNull(result.getPayerUniqueIdentifierCode());
    assertNull(result.getPayer());
    assertEquals(installmentPaidViewNoPII.getDebtorEntityType(), result.getSubjectPayingEntityType());
    assertEquals(person, result.getDebtor());
    assertEquals(PaymentOutcomeCode.PAYMENT_EXECUTED.getCode(), result.getPaymentOutcomeCode());
    assertEquals(installmentPaidViewNoPII.getPaymentAmountCents(), result.getTotalAmountPaidCents());
    assertEquals(installmentPaidViewNoPII.getCreditorReferenceId(), result.getUniquePaymentIdentifier());
    assertEquals(installmentPaidViewNoPII.getPaymentReceiptId(), result.getPaymentContextCode());
    assertEquals(installmentPaidViewNoPII.getAmountCents(), result.getSingleAmountPaidCents());
    assertEquals("0", result.getSinglePaymentOutcome());
    assertEquals(installmentPaidViewNoPII.getPaymentDateTime(), result.getSinglePaymentOutcomeDateTime());
    assertEquals(installmentPaidViewNoPII.getPaymentReceiptId(), result.getUniqueCollectionIdentifier());
  }

  private void assert1_1(InstallmentPaidViewNoPII installmentPaidViewNoPII, InstallmentPaidViewDTO result){
    assertEquals(installmentPaidViewNoPII.getRemittanceInformation(), result.getPaymentReason());
    assertEquals("9/category", result.getCollectionSpecificData());
    assertEquals(installmentPaidViewNoPII.getCode(), result.getDueType());
    assertNull(result.getSignatureType());
    assertNull(result.getRt());
    assertEquals(installmentPaidViewNoPII.getTransferIndex(), result.getSinglePaymentDataIndex());
    assertEquals(installmentPaidViewNoPII.getFeeCents(), result.getPspAppliedFeesCents());
    assertEquals("BD", result.getReceiptAttachmentType());
    assertNull(result.getReceiptAttachmentTest());
  }

  private void assert1_2(InstallmentPaidViewNoPII installmentPaidViewNoPII, InstallmentPaidViewDTO result){
    assertEquals(installmentPaidViewNoPII.getBalance(), result.getBalance());
  }
  private void assert1_3(InstallmentPaidViewNoPII installmentPaidViewNoPII, InstallmentPaidViewDTO result){
    assertEquals(installmentPaidViewNoPII.getOrgFiscalCode(), result.getOrgFiscalCode());
    assertEquals(installmentPaidViewNoPII.getCompanyName(), result.getOrgName());
    assertEquals(installmentPaidViewNoPII.getCategory(), result.getDueTaxonomicCode());
  }

}
