package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.DebtPositionTypeOrgSecondaryOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptWithAdditionalInfoMapperTest {

  @Mock
  private DebtPositionTypeOrgSecondaryOrgRetrieverService debtPositionTypeOrgSecondaryOrgRetrieverServiceMock;

  @InjectMocks
  private ReceiptWithAdditionalInfoMapper receiptWithAdditionalInfoMapper;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void test(){
    //given
    ReceiptWithAdditionalNodeDataDTO receiptWithAdditionalNodeDataDTO = podamFactory.manufacturePojo(ReceiptWithAdditionalNodeDataDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(debtPositionTypeOrgSecondaryOrgRetrieverServiceMock.getSecondaryOrgDebtPositionTypeOrg(organization.getOrganizationId())).thenReturn(debtPositionTypeOrg);

    //when
    DebtPositionDTO debtPositionDTO = receiptWithAdditionalInfoMapper.mapToDebtPosition(receiptWithAdditionalNodeDataDTO, organization);

    //verify
    Assertions.assertNotNull(debtPositionDTO);
    TestUtils.checkNotNullFields(debtPositionDTO, "debtPositionId");
    Assertions.assertEquals(debtPositionTypeOrg.getDebtPositionTypeOrgId(), debtPositionDTO.getDebtPositionTypeOrgId());
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().getFirst();
    TestUtils.checkNotNullFields(paymentOptionDTO, "paymentOptionId", "debtPositionId", "dueDate");
    InstallmentDTO installmentDTO = paymentOptionDTO.getInstallments().getFirst();
    TestUtils.checkNotNullFields(installmentDTO, "paymentOptionId", "installmentId", "dueDate",
      "syncStatus", "iuf", "iur", "paymentTypeCode", "balance", "legacyPaymentMetadata", "notificationDate",
      "ingestionFlowFileId", "ingestionFlowFileLineNumber");
    TestUtils.checkNotNullFields(installmentDTO.getDebtor());
    installmentDTO.getTransfers().forEach(transferDTO -> TestUtils.checkNotNullFields(transferDTO,"installmentId", "postalIban"));
    Mockito.verify(debtPositionTypeOrgSecondaryOrgRetrieverServiceMock, Mockito.times(1)).getSecondaryOrgDebtPositionTypeOrg(organization.getOrganizationId());
  }

}
