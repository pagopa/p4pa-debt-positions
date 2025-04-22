package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.UnknownDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptWithAdditionalInfoMapperTest {

  @Mock
  private UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverServiceMock;

  @InjectMocks
  private ReceiptWithAdditionalInfoMapper receiptWithAdditionalInfoMapper;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @ParameterizedTest
  @ValueSource(strings = {"RECEIPT_PAGOPA", "SECONDARY_ORG", "RECEIPT_FILE", "PAYMENTS_REPORTING"})
  void test(String receiptOrigin){
    //given
    ReceiptWithAdditionalNodeDataDTO receiptWithAdditionalNodeDataDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    receiptWithAdditionalNodeDataDTO.setOrgFiscalCode(organization.getOrgFiscalCode());
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    if(receiptOrigin.equals("SECONDARY_ORG")){
      receiptWithAdditionalNodeDataDTO.setReceiptOrigin(ReceiptOriginType.RECEIPT_PAGOPA);
      receiptWithAdditionalNodeDataDTO.setOrgFiscalCode(receiptWithAdditionalNodeDataDTO.getOrgFiscalCode()+"secondary");
    } else {
      receiptWithAdditionalNodeDataDTO.setReceiptOrigin(ReceiptOriginType.valueOf(receiptOrigin));
    }
    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(organization.getOrganizationId())).thenReturn(debtPositionTypeOrg);

    //when
    DebtPositionDTO debtPositionDTO = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptWithAdditionalNodeDataDTO, organization);

    //verify
    DebtPositionOrigin debtPositionOrigin = switch (receiptOrigin){
      case "RECEIPT_PAGOPA" -> DebtPositionOrigin.RECEIPT_PAGOPA;
      case "SECONDARY_ORG" -> DebtPositionOrigin.SECONDARY_ORG;
      case "RECEIPT_FILE" -> DebtPositionOrigin.RECEIPT_FILE;
      case "PAYMENTS_REPORTING" -> DebtPositionOrigin.REPORTING_PAGOPA;
      default -> null;
    };
    Assertions.assertNotNull(debtPositionDTO);
    TestUtils.checkNotNullFields(debtPositionDTO, "debtPositionId", "validityDate", "creationDate", "updateDate", "updateOperatorExternalId");
    Assertions.assertEquals(debtPositionOrigin, debtPositionDTO.getDebtPositionOrigin());
    Assertions.assertEquals(debtPositionTypeOrg.getDebtPositionTypeOrgId(), debtPositionDTO.getDebtPositionTypeOrgId());
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().getFirst();
    TestUtils.checkNotNullFields(paymentOptionDTO, "paymentOptionId", "debtPositionId", "dueDate", "creationDate", "updateDate", "updateOperatorExternalId");
    InstallmentDTO installmentDTO = paymentOptionDTO.getInstallments().getFirst();
    TestUtils.checkNotNullFields(installmentDTO, "paymentOptionId", "installmentId", "dueDate",
      "syncStatus", "iuf", "iur", "iun", "paymentTypeCode", "balance", "legacyPaymentMetadata", "notificationDate",
      "ingestionFlowFileId", "ingestionFlowFileLineNumber", "creationDate", "updateDate", "updateOperatorExternalId");
    TestUtils.checkNotNullFields(installmentDTO.getDebtor());
    installmentDTO.getTransfers().forEach(transferDTO -> TestUtils.checkNotNullFields(transferDTO,"transferId","installmentId", "postalIban", "creationDate", "updateDate", "updateOperatorExternalId"));
    Mockito.verify(unknownDebtPositionTypeOrgRetrieverServiceMock, Mockito.times(1)).getUnknownDebtPositionTypeOrg(organization.getOrganizationId());
  }

}
