package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptArchivingPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static it.gov.pagopa.pu.debtpositions.util.SecurityUtils.SYSTEM_USERID_PREFIX;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceImplTest {

  @Mock
  private ReceiptPIIRepository receiptPIIRepositoryMock;
  @Mock
  private ReceiptDetailPIIViewRepository receiptDetailPIIViewRepositoryMock;
  @Mock
  private ReceiptArchivingPIIViewRepository receiptArchivingPIIViewRepositoryMock;

  @InjectMocks
  private ReceiptServiceImpl receiptService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @AfterEach
  void setUp() {
    Mockito.verifyNoMoreInteractions(
      receiptPIIRepositoryMock,
      receiptDetailPIIViewRepositoryMock,
      receiptArchivingPIIViewRepositoryMock);
  }

  @Test
  void whenGetReceiptThenOk() {
    //given
    Long receiptId = 1L;
    ReceiptDTO receipt = podamFactory.manufacturePojo(ReceiptDTO.class);

    Mockito.when(receiptPIIRepositoryMock.findById(receiptId)).thenReturn(receipt);

    //when
    ReceiptDTO response = receiptService.getReceipt(receiptId);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receipt, response);

    Mockito.verify(receiptPIIRepositoryMock).findById(receiptId);
  }

  @Test
  void whenGetReceiptDetailThenOk() {
    //given
    Long organizationId = 1L;
    Long receiptId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    String iud = "iud";
    ReceiptDetailDTO receipt = podamFactory.manufacturePojo(ReceiptDetailDTO.class);

    Mockito.when(receiptDetailPIIViewRepositoryMock.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud)).thenReturn(receipt);

    //when
    ReceiptDetailDTO response = receiptService.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receipt, response);

    Mockito.verify(receiptDetailPIIViewRepositoryMock).getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud);
  }

  @Test
  void givenNoOperatorExternalUserIdwhenGetReceiptDetailThenOk() {
    //given
    Long organizationId = 1L;
    Long receiptId = 1L;
    String iud = "iud";
    ReceiptDetailDTO receipt = podamFactory.manufacturePojo(ReceiptDetailDTO.class);

    Mockito.when(receiptDetailPIIViewRepositoryMock.getReceiptDetail(receiptId, organizationId, iud)).thenReturn(receipt);

    //when
    ReceiptDetailDTO response = receiptService.getReceiptDetail(receiptId, null, organizationId, iud);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receipt, response);
  }

  @Test
  void givenSystemUserPrefixWhenGetReceiptDetailThenOk() {
    //given
    Long organizationId = 1L;
    Long receiptId = 1L;
    String iud = "iud";
    String operatorExternalUserId = SYSTEM_USERID_PREFIX+"test";
    ReceiptDetailDTO receipt = podamFactory.manufacturePojo(ReceiptDetailDTO.class);

    Mockito.when(receiptDetailPIIViewRepositoryMock.getReceiptDetail(receiptId, organizationId, iud)).thenReturn(receipt);

    //when
    ReceiptDetailDTO response = receiptService.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud);

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(receipt, response);
  }

  @Test
  void whenGetReceiptArchivingThenOk(){
    //given
    Long organizationId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    OffsetDateTime paymentDateFrom = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    OffsetDateTime paymentDateTo = OffsetDateTime.now().plusMonths(1).withOffsetSameInstant(ZoneOffset.UTC);

    PagedReceiptsArchivingView expectedResponse = podamFactory.manufacturePojo(PagedReceiptsArchivingView.class);

    Mockito.when(receiptArchivingPIIViewRepositoryMock.getPagedReceiptsArchivingView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, Pageable.ofSize(1))).thenReturn(expectedResponse);
    //when
    PagedReceiptsArchivingView result = receiptService.getPagedReceiptArchivingView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, Pageable.ofSize(1));
    //then
    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedResponse, result);

    Mockito.verifyNoMoreInteractions(receiptArchivingPIIViewRepositoryMock);
  }
}
