package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class ReceiptPIIRepositoryImpl implements ReceiptPIIRepository {

  private final ReceiptPIIMapper receiptPIIMapper;
  private final PersonalDataService personalDataService;
  private final ReceiptNoPIIRepository receiptNoPIIRepository;
  private final ReceiptMapper receiptMapper;

  public ReceiptPIIRepositoryImpl(ReceiptPIIMapper receiptPIIMapper, PersonalDataService personalDataService, ReceiptNoPIIRepository receiptNoPIIRepository,
    ReceiptMapper receiptMapper) {
    this.receiptPIIMapper = receiptPIIMapper;
    this.personalDataService = personalDataService;
    this.receiptNoPIIRepository = receiptNoPIIRepository;
    this.receiptMapper = receiptMapper;
  }

  @Override
  public long save(Receipt receipt) {
    Pair<ReceiptNoPII, ReceiptPIIDTO> p = receiptPIIMapper.map(receipt);
    long personalDataId = personalDataService.insert(p.getSecond(), PersonalDataType.RECEIPT);
    p.getFirst().setPersonalDataId(personalDataId);
    receipt.setNoPII(p.getFirst());
    long newId = receiptNoPIIRepository.save(p.getFirst()).getReceiptId();
    receipt.setReceiptId(newId);
    receipt.getNoPII().setReceiptId(newId);
    return newId;
  }

  @Override
  public ReceiptDTO getReceiptDetail(Long receiptId, String orgFiscalCode) {
    ReceiptNoPII receiptNoPII = receiptNoPIIRepository.findByReceiptIdAndOrgFiscalCode(receiptId,orgFiscalCode)
      .orElseThrow(() -> new NotFoundException(
        "ReceiptNoPII having receiptId %d and orgFiscalCode %s not found".formatted(
          receiptId, orgFiscalCode)));
    return receiptMapper.mapToDto(receiptPIIMapper.map(receiptNoPII));
  }
}
