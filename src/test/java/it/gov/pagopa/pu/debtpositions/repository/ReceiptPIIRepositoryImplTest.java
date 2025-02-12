package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptPIIRepositoryImplTest {

  @Mock(answer = Answers.RETURNS_MOCKS)
  private ReceiptNoPIIRepository receiptNoPIIRepositoryMock;
  @Mock
  private PersonalDataService personalDataServiceMock;
  @Mock
  private ReceiptPIIMapper receiptPIIMapperMock;

  private ReceiptPIIRepository receiptPIIRepository;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    receiptPIIRepository = new ReceiptPIIRepositoryImpl(receiptPIIMapperMock, personalDataServiceMock, receiptNoPIIRepositoryMock);
  }

  @Test
  void givenValidReceiptWhenSaveThenOk() {
    // Given
    Receipt receipt = podamFactory.manufacturePojo(Receipt.class);
    receipt.setReceiptId(null);
    receipt.setNoPII(null);
    ReceiptNoPII returned = podamFactory.manufacturePojo(ReceiptNoPII.class);
    Pair<ReceiptNoPII, ReceiptPIIDTO> pair = podamFactory.manufacturePojo(Pair.class, ReceiptNoPII.class, ReceiptPIIDTO.class);
    Mockito.when(receiptPIIMapperMock.map(receipt)).thenReturn(pair);
    long piiId = -1L;
    Mockito.when(personalDataServiceMock.insert(pair.getSecond(), PersonalDataType.RECEIPT)).thenReturn(piiId);
    Mockito.when(receiptNoPIIRepositoryMock.save(pair.getFirst())).thenReturn(returned);

    // When
    long result = receiptPIIRepository.save(receipt);

    // Then
    Assertions.assertEquals(returned.getReceiptId(), result);
    Assertions.assertEquals(returned.getReceiptId(), receipt.getReceiptId());
    TestUtils.reflectionEqualsByName(pair.getFirst(), receipt.getNoPII(), "receiptId");
    Assertions.assertEquals(returned.getReceiptId(), receipt.getNoPII().getReceiptId());
    Mockito.verify(receiptPIIMapperMock, Mockito.times(1)).map(receipt);
    Mockito.verify(personalDataServiceMock, Mockito.times(1)).insert(pair.getSecond(), PersonalDataType.RECEIPT);
    Mockito.verify(receiptNoPIIRepositoryMock, Mockito.times(1)).save(pair.getFirst());
  }

  @Test
  void givenExistingReceiptWhenFindReceiptThenOk() {
    // Given
    Long receiptId = 1L;
    String orgFiscalCode = "orgFiscalCode";
    ReceiptNoPII receiptNoPII = podamFactory.manufacturePojo(ReceiptNoPII.class);
    ReceiptDTO receiptDto = podamFactory.manufacturePojo(ReceiptDTO.class);

    Mockito.when(receiptNoPIIRepositoryMock.findByReceiptIdAndOrgFiscalCode(receiptId,orgFiscalCode)).thenReturn(
      Optional.of(receiptNoPII));
    Mockito.when(receiptPIIMapperMock.mapToReceiptDTO(receiptNoPII)).thenReturn(receiptDto);

    // When
    ReceiptDTO result = receiptPIIRepository.getReceiptDetail(receiptId,orgFiscalCode);

    // Then
    Assertions.assertEquals(receiptDto, result);
    Mockito.verify(receiptNoPIIRepositoryMock).findByReceiptIdAndOrgFiscalCode(receiptId,orgFiscalCode);
    Mockito.verify(receiptPIIMapperMock).mapToReceiptDTO(receiptNoPII);
  }

  @Test
  void givenNonExistingReceiptWhenFindReceiptThenNotFoundException() {
    // Given
    Long receiptId = 1L;
    String orgFiscalCode = "orgFiscalCode";

    Mockito.when(receiptNoPIIRepositoryMock.findByReceiptIdAndOrgFiscalCode(receiptId,orgFiscalCode)).thenReturn(
      Optional.empty());

    // When
    Assertions.assertThrows(NotFoundException.class,()->receiptPIIRepository.getReceiptDetail(receiptId,orgFiscalCode));

    Mockito.verify(receiptNoPIIRepositoryMock).findByReceiptIdAndOrgFiscalCode(receiptId,orgFiscalCode);
    Mockito.verifyNoInteractions(receiptPIIMapperMock);
  }
}
