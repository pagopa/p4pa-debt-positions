package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReceiptBasedTechnicalDpHandlerServiceTest {

  @Mock
  private ReceiptWithAdditionalInfoMapper receiptMapperMock;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private PaymentsProducerService paymentsProducerServiceMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  private ReceiptBasedTechnicalDpHandlerService service;

  @BeforeEach
  void init(){
    service = new ReceiptBasedTechnicalDpHandlerService(
      receiptMapperMock,
      debtPositionServiceMock,
      paymentsProducerServiceMock,
      debtPositionMapperMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      receiptMapperMock,
      debtPositionServiceMock,
      paymentsProducerServiceMock,
      debtPositionMapperMock
    );
  }

  @Test
  void whenCreateAndPublishTechDpThenOk(){
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    Organization organization = new Organization();
    DebtPositionDTO dpDto = new DebtPositionDTO();
    DebtPosition expectedResult = new DebtPosition();

    service = Mockito.spy(service);

    Mockito.when(receiptMapperMock.mapToDebtPosition(Mockito.same(receiptDTO), Mockito.same(organization)))
      .thenReturn(dpDto);

    Mockito.doReturn(expectedResult)
      .when(service)
      .publishTechDp(Mockito.same(dpDto), Mockito.same(receiptDTO));

    // When
    DebtPosition result = service.createAndPublishTechDp(organization, receiptDTO);

    // Then
    Assertions.assertSame(expectedResult, result);

    Mockito.verify(debtPositionServiceMock)
      .saveDebtPosition(Mockito.same(dpDto));
  }

  @Test
  void whenPublishTechDpThenOk(){
    // Given
    ReceiptWithAdditionalNodeDataDTO receiptDTO = new ReceiptWithAdditionalNodeDataDTO();
    receiptDTO.setReceiptId(-1L);
    DebtPositionDTO dpDto = new DebtPositionDTO();
    DebtPosition expectedResult = new DebtPosition();

    Mockito.when(debtPositionMapperMock.mapToModel(Mockito.same(dpDto)))
      .thenReturn(expectedResult);

    // When
    DebtPosition result = service.publishTechDp(dpDto, receiptDTO);

    // Then
    Assertions.assertSame(expectedResult, result);

    Mockito.verify(paymentsProducerServiceMock)
      .notifyPaymentsEvent(Mockito.same(dpDto), Mockito.eq(PaymentEventType.RT_RECEIVED), Mockito.eq("receiptId:-1"));
  }
}
