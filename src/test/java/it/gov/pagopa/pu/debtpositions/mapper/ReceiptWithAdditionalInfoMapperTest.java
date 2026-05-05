package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class ReceiptWithAdditionalInfoMapperTest {

  private static final String UNKNOWN_STATION_ID = "UNKNOWN";

  @InjectMocks
  private ReceiptWithAdditionalInfoMapper receiptWithAdditionalInfoMapper;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @ParameterizedTest
  @EnumSource(ReceiptOriginType.class)
  void givenValidReceiptOriginWhenMapToDTOThenReturnDebtPosition(ReceiptOriginType receiptOrigin) {
    //given
    ReceiptWithAdditionalNodeDataDTO receiptWithAdditionalNodeDataDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    receiptWithAdditionalNodeDataDTO.setOrgFiscalCode(organization.getOrgFiscalCode());
    receiptWithAdditionalNodeDataDTO.setIud(null);
    receiptWithAdditionalNodeDataDTO.setReceiptOrigin(receiptOrigin);

    Long providedTypeOrgId = 12345L;

    //when
    DebtPositionDTO debtPositionDTO = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptWithAdditionalNodeDataDTO, organization, providedTypeOrgId, null);

    //verify
    DebtPositionOrigin debtPositionOrigin = switch (receiptOrigin) {
      case ReceiptOriginType.RECEIPT_PAGOPA -> DebtPositionOrigin.RECEIPT_PAGOPA;
      case ReceiptOriginType.RECEIPT_FILE -> DebtPositionOrigin.RECEIPT_FILE;
      case ReceiptOriginType.PAYMENTS_REPORTING -> DebtPositionOrigin.REPORTING_PAGOPA;
    };
    InstallmentDTO installmentDTO = commonAsserts(debtPositionDTO, debtPositionOrigin, providedTypeOrgId);
    Assertions.assertEquals(UNKNOWN_STATION_ID, debtPositionDTO.getStationId());
    Assertions.assertEquals(receiptWithAdditionalNodeDataDTO.getTransfers().size(), installmentDTO.getTransfers().size());
  }

  private static InstallmentDTO commonAsserts(DebtPositionDTO debtPositionDTO, DebtPositionOrigin debtPositionOrigin, Long expectedTypeOrgId) {
    Assertions.assertNotNull(debtPositionDTO);
    TestUtils.checkNotNullFields(debtPositionDTO, "debtPositionId", "validityDate", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    Assertions.assertEquals(debtPositionOrigin, debtPositionDTO.getDebtPositionOrigin());
    Assertions.assertEquals(expectedTypeOrgId, debtPositionDTO.getDebtPositionTypeOrgId());
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().getFirst();
    TestUtils.checkNotNullFields(paymentOptionDTO, "paymentOptionId", "debtPositionId", "dueDate", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    InstallmentDTO installmentDTO = paymentOptionDTO.getInstallments().getFirst();
    TestUtils.checkNotNullFields(installmentDTO, "paymentOptionId", "installmentId", "dueDate",
      "syncStatus", "iuf", "iur", "iun", "paymentTypeCode", "balance", "legacyPaymentMetadata", "notificationFeeCents", "notificationDate",
      "ingestionFlowFileId", "ingestionFlowFileLineNumber", "ingestionFlowFileAction", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId",
      "noPII", "originalRemittanceInformation");
    TestUtils.checkNotNullFields(installmentDTO.getDebtor());
    installmentDTO.getTransfers().forEach(transferDTO -> TestUtils.checkNotNullFields(transferDTO, "transferId", "installmentId", "postalIban", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId", "flagOwner"));
    return installmentDTO;
  }

  @Test
  void givenSecondaryOrgWhenMapToDTOThenKeepJustItsTransfers() {
    // Given
    Organization secondaryOrganization = podamFactory.manufacturePojo(Organization.class);
    ReceiptWithAdditionalNodeDataDTO receiptWithAdditionalNodeDataDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    ReceiptTransferDTO rt1 = podamFactory.manufacturePojo(ReceiptTransferDTO.class);
    rt1.setFiscalCodePA("PRIMARYORGFC");
    rt1.setIdTransfer(1);
    ReceiptTransferDTO rt2 = podamFactory.manufacturePojo(ReceiptTransferDTO.class);
    rt2.setFiscalCodePA(secondaryOrganization.getOrgFiscalCode());
    rt2.setIdTransfer(2);
    receiptWithAdditionalNodeDataDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    receiptWithAdditionalNodeDataDTO.setOrgFiscalCode(rt1.getFiscalCodePA());
    receiptWithAdditionalNodeDataDTO.setTransfers(List.of(rt1, rt2));

    Long providedTypeOrgId = 67890L;

    // When
    DebtPositionDTO debtPositionDTO = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptWithAdditionalNodeDataDTO, secondaryOrganization, providedTypeOrgId, null);

    // Then
    InstallmentDTO installmentDTO = commonAsserts(debtPositionDTO, DebtPositionOrigin.SECONDARY_ORG, providedTypeOrgId);

    Assertions.assertEquals(UNKNOWN_STATION_ID, debtPositionDTO.getStationId());
    Assertions.assertEquals(1, installmentDTO.getTransfers().size());
    Assertions.assertEquals(2, installmentDTO.getTransfers().getFirst().getTransferIndex());
    Assertions.assertEquals(secondaryOrganization.getOrgFiscalCode(), installmentDTO.getTransfers().getFirst().getOrgFiscalCode());
  }

  @Test
  void givenExistingDebtPositionWhenMapToDTOThenUseExistingIupdAndIud() {
    ReceiptWithAdditionalNodeDataDTO receiptDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    receiptDTO.setOrgFiscalCode(organization.getOrgFiscalCode());
    receiptDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
    Long providedTypeOrgId = 1L;

    String expectedIupdPagopa = "EXISTING_IUPD";
    String expectedIud = "EXISTING_IUD";

    InstallmentNoPII existingInstallment = podamFactory.manufacturePojo(InstallmentNoPII.class);
    existingInstallment.setIupdPagopa(expectedIupdPagopa);
    existingInstallment.setIud(expectedIud);

   PaymentOption existingPaymentOption = podamFactory.manufacturePojo(PaymentOption.class);
    existingPaymentOption.setInstallments(new TreeSet<>(List.of(existingInstallment)));

    DebtPosition existingDp = podamFactory.manufacturePojo(DebtPosition.class);
    existingDp.setPaymentOptions(new TreeSet<>(List.of(existingPaymentOption)));

    DebtPositionDTO result = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptDTO, organization, providedTypeOrgId, existingDp);

    InstallmentDTO mappedInstallment = commonAsserts(result, DebtPositionOrigin.RECEIPT_PAGOPA, providedTypeOrgId);

    Assertions.assertEquals(existingDp.getStationId(), result.getStationId());
    Assertions.assertEquals(expectedIupdPagopa, mappedInstallment.getIupdPagopa());
    Assertions.assertEquals(expectedIud, mappedInstallment.getIud());
  }
}
