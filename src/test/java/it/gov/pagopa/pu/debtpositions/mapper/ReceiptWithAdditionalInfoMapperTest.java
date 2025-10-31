package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.UnknownDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReceiptWithAdditionalInfoMapperTest {

  @Mock
  private UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

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
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    receiptWithAdditionalNodeDataDTO.setIud(null);
    receiptWithAdditionalNodeDataDTO.setReceiptOrigin(receiptOrigin);
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(organization.getOrganizationId(), receiptWithAdditionalNodeDataDTO.getDebtPositionTypeOrgCode()))
      .thenReturn(Optional.empty());
    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(organization.getOrganizationId()))
      .thenReturn(debtPositionTypeOrg);

    //when
    DebtPositionDTO debtPositionDTO = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptWithAdditionalNodeDataDTO, organization);

    //verify
    DebtPositionOrigin debtPositionOrigin = switch (receiptOrigin) {
      case ReceiptOriginType.RECEIPT_PAGOPA -> DebtPositionOrigin.RECEIPT_PAGOPA;
      case ReceiptOriginType.RECEIPT_FILE -> DebtPositionOrigin.RECEIPT_FILE;
      case ReceiptOriginType.PAYMENTS_REPORTING -> DebtPositionOrigin.REPORTING_PAGOPA;
    };
    InstallmentDTO installmentDTO = commonAsserts(debtPositionDTO, debtPositionOrigin, debtPositionTypeOrg);
    Assertions.assertEquals(receiptWithAdditionalNodeDataDTO.getTransfers().size(), installmentDTO.getTransfers().size());
  }

  private static InstallmentDTO commonAsserts(DebtPositionDTO debtPositionDTO, DebtPositionOrigin debtPositionOrigin, DebtPositionTypeOrg debtPositionTypeOrg) {
    Assertions.assertNotNull(debtPositionDTO);
    TestUtils.checkNotNullFields(debtPositionDTO, "debtPositionId", "validityDate", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    Assertions.assertEquals(debtPositionOrigin, debtPositionDTO.getDebtPositionOrigin());
    Assertions.assertEquals(debtPositionTypeOrg.getDebtPositionTypeOrgId(), debtPositionDTO.getDebtPositionTypeOrgId());
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().getFirst();
    TestUtils.checkNotNullFields(paymentOptionDTO, "paymentOptionId", "debtPositionId", "dueDate", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    InstallmentDTO installmentDTO = paymentOptionDTO.getInstallments().getFirst();
    TestUtils.checkNotNullFields(installmentDTO, "paymentOptionId", "installmentId", "dueDate",
      "syncStatus", "iuf", "iur", "iun", "paymentTypeCode", "balance", "legacyPaymentMetadata", "notificationFeeCents", "notificationDate",
      "ingestionFlowFileId", "ingestionFlowFileLineNumber", "ingestionFlowFileAction", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId",
      "noPII");
    TestUtils.checkNotNullFields(installmentDTO.getDebtor());
    installmentDTO.getTransfers().forEach(transferDTO -> TestUtils.checkNotNullFields(transferDTO, "transferId", "installmentId", "postalIban", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId"));
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
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(secondaryOrganization.getOrganizationId(), receiptWithAdditionalNodeDataDTO.getDebtPositionTypeOrgCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    // When
    DebtPositionDTO debtPositionDTO = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptWithAdditionalNodeDataDTO, secondaryOrganization);

    // Then
    InstallmentDTO installmentDTO = commonAsserts(debtPositionDTO, DebtPositionOrigin.SECONDARY_ORG, debtPositionTypeOrg);

    Assertions.assertEquals(1, installmentDTO.getTransfers().size());
    Assertions.assertEquals(2, installmentDTO.getTransfers().getFirst().getTransferIndex());
    Assertions.assertEquals(secondaryOrganization.getOrgFiscalCode(), installmentDTO.getTransfers().getFirst().getOrgFiscalCode());
  }
}
