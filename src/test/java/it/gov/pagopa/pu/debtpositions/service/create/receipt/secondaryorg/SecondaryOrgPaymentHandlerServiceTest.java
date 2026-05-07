package it.gov.pagopa.pu.debtpositions.service.create.receipt.secondaryorg;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptTransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.ReceiptBasedTechnicalDpHandlerService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SecondaryOrgPaymentHandlerServiceTest {

  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private ReceiptBasedTechnicalDpHandlerService receiptBasedTechnicalDpHandlerServiceMock;

  private SecondaryOrgPaymentHandlerService service;

  @BeforeEach
  void init(){
    service = new SecondaryOrgPaymentHandlerService(
      organizationServiceMock,
      debtPositionRepositoryMock,
      receiptBasedTechnicalDpHandlerServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      organizationServiceMock,
      debtPositionRepositoryMock,
      receiptBasedTechnicalDpHandlerServiceMock);
  }

  @Test
  void givenSecondaryOrgNotHandledByPuWhenHandleThenDoNothing(){
    // Given
    String accessToken = "ACCESSTOKEN";
    ReceiptTransferDTO rt1 = new ReceiptTransferDTO();
    rt1.setIdTransfer(1);
    rt1.setFiscalCodePA("PRIMARYORGFC");
    ReceiptTransferDTO rt2 = new ReceiptTransferDTO();
    rt2.setIdTransfer(2);
    rt2.setFiscalCodePA("SECONDARYORGFC");
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setOrgFiscalCode(rt1.getFiscalCodePA());
    receiptDTO.setTransfers(List.of(rt1, rt2));

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(rt2.getFiscalCodePA(), accessToken))
      .thenReturn(Optional.empty());

    // When
    service.handle(receiptDTO, accessToken);

    // Then
    Mockito.verifyNoInteractions(debtPositionRepositoryMock, receiptBasedTechnicalDpHandlerServiceMock);
  }

  @Test
  void givenExistingSecondaryOrgDpWhenHandleThenUpdateId(){
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = -1L;
    Long receiptId = 2L;

    ReceiptTransferDTO rt1 = new ReceiptTransferDTO();
    rt1.setIdTransfer(1);
    rt1.setFiscalCodePA("PRIMARYORGFC");
    ReceiptTransferDTO rt2 = new ReceiptTransferDTO();
    rt2.setIdTransfer(2);
    rt2.setFiscalCodePA("SECONDARYORGFC");
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setReceiptId(receiptId);
    receiptDTO.setOrgFiscalCode(rt1.getFiscalCodePA());
    receiptDTO.setTransfers(List.of(rt1, rt2));
    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode(rt2.getFiscalCodePA());
    DebtPosition secondaryOrgDp = new DebtPosition();

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(rt2.getFiscalCodePA(), accessToken))
      .thenReturn(Optional.of(organization));

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndReceiptId(organizationId, receiptId, List.of(DebtPositionOrigin.SECONDARY_ORG)))
      .thenReturn(List.of(secondaryOrgDp, new DebtPosition()));

    // When
    service.handle(receiptDTO, accessToken);

    // Then
    Mockito.verify(receiptBasedTechnicalDpHandlerServiceMock)
      .updateAndPublishTechDp(Mockito.same(organization), Mockito.same(secondaryOrgDp), Mockito.same(receiptDTO), Mockito.same(accessToken));
  }

  @Test
  void givenNotExistingSecondaryOrgDpWhenHandleThenUpdateId(){
    // Given
    String accessToken = "ACCESSTOKEN";
    Long organizationId = -1L;
    Long receiptId = 2L;

    ReceiptTransferDTO rt1 = new ReceiptTransferDTO();
    rt1.setIdTransfer(1);
    rt1.setFiscalCodePA("PRIMARYORGFC");
    ReceiptTransferDTO rt2 = new ReceiptTransferDTO();
    rt2.setIdTransfer(2);
    rt2.setFiscalCodePA("SECONDARYORGFC");
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setReceiptId(receiptId);
    receiptDTO.setOrgFiscalCode(rt1.getFiscalCodePA());
    receiptDTO.setTransfers(List.of(rt1, rt2));
    Organization organization = new Organization();
    organization.setOrganizationId(organizationId);
    organization.setOrgFiscalCode(rt2.getFiscalCodePA());

    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(rt2.getFiscalCodePA(), accessToken))
      .thenReturn(Optional.of(organization));

    Mockito.when(debtPositionRepositoryMock.findEntityGraphByOrganizationIdAndReceiptId(organizationId, receiptId, List.of(DebtPositionOrigin.SECONDARY_ORG)))
      .thenReturn(List.of());

    // When
    service.handle(receiptDTO, accessToken);

    // Then
    Mockito.verify(receiptBasedTechnicalDpHandlerServiceMock)
      .createAndPublishTechDp(Mockito.same(organization), Mockito.same(receiptDTO), Mockito.same(accessToken));
  }
}
