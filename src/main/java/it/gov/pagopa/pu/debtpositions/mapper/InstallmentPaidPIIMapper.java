package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOutcomeCode;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.enums.UniqueIdentifierType;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.stereotype.Service;

@Service
public class InstallmentPaidPIIMapper {

  private final PersonalDataService personalDataService;
  private final PersonMapper personMapper;

  public InstallmentPaidPIIMapper(PersonalDataService personalDataService, PersonMapper personMapper) {
    this.personalDataService = personalDataService;
    this.personMapper = personMapper;
  }

  public InstallmentPaidViewDTO mapToInstallmentPaidViewDTO(Float versionTrack, InstallmentPaidViewNoPII installmentPaidViewNoPII){
      switch (versionTrack.toString()){
        case "1.0" -> {
          return mapToInstallmentPaidViewDTO1(installmentPaidViewNoPII);
        }
        case "1.1" -> {
          return mapToInstallmentPaidViewDTO1_1(installmentPaidViewNoPII);
        }
        case "1.2" -> {
          return mapToInstallmentPaidViewDTO1_2(installmentPaidViewNoPII);
        }
        case "1.3" -> {
          return mapToInstallmentPaidViewDTO1_3(installmentPaidViewNoPII);
        }
        default -> throw new IllegalArgumentException("Unexpected versionTrack " + versionTrack);
      }
  }

  private InstallmentPaidViewDTO mapToInstallmentPaidViewDTO1(InstallmentPaidViewNoPII installmentPaidViewNoPII){
    InstallmentPIIDTO installmentPii = personalDataService.get(installmentPaidViewNoPII.getPersonalDataId(), InstallmentPIIDTO.class);

    return InstallmentPaidViewDTO.builder()
      .iuf(installmentPaidViewNoPII.getIuf())
      .flowRowNumber(1)
      .iud(installmentPaidViewNoPII.getIud())
      .iuv(installmentPaidViewNoPII.getNoticeNumber())
      .domainIdentifier(installmentPaidViewNoPII.getOrgFiscalCode())
      .receiptMessageIdentifier(installmentPaidViewNoPII.getPaymentReceiptId())
      .receiptMessageDateTime(installmentPaidViewNoPII.getPaymentDateTime())
      .requestMessageReference(installmentPaidViewNoPII.getPaymentReceiptId())
      .requestDateTimeReference(installmentPaidViewNoPII.getPaymentDateTime())
      .uniqueIdentifierType(UniqueIdentifierType.B)
      .uniqueIdentifierCode(installmentPaidViewNoPII.getIdPsp())
      .attestingName(installmentPaidViewNoPII.getPspCompanyName())
      .beneficiaryEntityType(PersonEntityType.G)
      .beneficiaryUniqueIdentifierCode(installmentPaidViewNoPII.getOrgFiscalCode())
      .beneficiaryName(installmentPaidViewNoPII.getCompanyName())
      .subjectPayingEntityType(installmentPaidViewNoPII.getDebtorEntityType())
      .debtor(installmentPii.getDebtor() != null ? personMapper.mapToDto(installmentPii.getDebtor()) : null)
      .paymentOutcomeCode(PaymentOutcomeCode.PAYMENT_EXECUTED.getCode())
      .totalAmountPaidCents(installmentPaidViewNoPII.getPaymentAmountCents())
      .uniquePaymentIdentifier(installmentPaidViewNoPII.getCreditorReferenceId())
      .paymentContextCode(installmentPaidViewNoPII.getPaymentReceiptId())
      .singleAmountPaidCents(installmentPaidViewNoPII.getAmountCents())
      .singlePaymentOutcome("0")
      .singlePaymentOutcomeDateTime(installmentPaidViewNoPII.getPaymentDateTime())
      .uniqueCollectionIdentifier(installmentPaidViewNoPII.getPaymentReceiptId())
      .build();
  }

  private InstallmentPaidViewDTO mapToInstallmentPaidViewDTO1_1(InstallmentPaidViewNoPII installmentPaidViewNoPII){
    InstallmentPaidViewDTO.InstallmentPaidViewDTOBuilder<?, ?> installmentPaidViewDTOBuilder = mapToInstallmentPaidViewDTO1(installmentPaidViewNoPII).toBuilder();

    installmentPaidViewDTOBuilder
      .paymentReason(installmentPaidViewNoPII.getRemittanceInformation())
      .collectionSpecificData( "9/".concat(installmentPaidViewNoPII.getCategory()))
      .dueType(installmentPaidViewNoPII.getCode())
      .rt(null)  //TODO field rt depends on task https://pagopa.atlassian.net/browse/P4ADEV-2306
      .singlePaymentDataIndex(installmentPaidViewNoPII.getTransferIndex())
      .pspAppliedFeesCents(installmentPaidViewNoPII.getFeeCents());

    if (installmentPaidViewNoPII.getCode().equals("MARCA_BOLLO")){
      installmentPaidViewDTOBuilder.receiptAttachmentType("BD");
      installmentPaidViewDTOBuilder.receiptAttachmentTest(null); //TODO field blbRtDatiPagDatiSingPagAllegatoRicevutaTest depends on task https://pagopa.atlassian.net/browse/P4ADEV-2306
    }

    return installmentPaidViewDTOBuilder.build();
  }

  private InstallmentPaidViewDTO mapToInstallmentPaidViewDTO1_2(InstallmentPaidViewNoPII installmentPaidViewNoPII){

    return mapToInstallmentPaidViewDTO1_1(installmentPaidViewNoPII).toBuilder()
      .balance(installmentPaidViewNoPII.getBalance())
      .build();
  }

  private InstallmentPaidViewDTO mapToInstallmentPaidViewDTO1_3(InstallmentPaidViewNoPII installmentPaidViewNoPII){

    return mapToInstallmentPaidViewDTO1_2(installmentPaidViewNoPII).toBuilder()
      .orgFiscalCode(installmentPaidViewNoPII.getOrgFiscalCode())
      .orgName(installmentPaidViewNoPII.getCompanyName())
      .dueTaxonomicCode(installmentPaidViewNoPII.getCategory())
      .build();
  }
}
